package org.j3d;

import java.io.File;
import java.nio.FloatBuffer;
import java.util.Vector;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public final class Scene  {

    private static final Vector<Node> renderables = new Vector<>(100000);
    private static final Vector<Node> lights = new Vector<>(1000);

    public final File file;
    public final Camera camera = new Camera();
    public final Node root = new Node();
    public final Vec3 backgroundColor = new Vec3(0.15f, 0.15f, 0.15f);
    public boolean drawLights = true;
    public int snap = 1;
    public int lightMapWidth = 128;
    public int lightMapHeight = 128;

    private int trianglesRendered = 0;
    private int collidableTriangles = 0;
    private Node ui = null;
    private File loadFile = null;
    private FloatBuffer m = BufferUtils.createFloatBuffer(16);
    private FloatBuffer v = BufferUtils.createFloatBuffer(4);

    public Scene(File file, boolean inDesign, Game game) throws Exception {
        this.file = file;
        if(inDesign) {
            ui = game.assets().load(IO.file("assets/ui/cube.obj"));
            ui = ui.childAt(0);
            ui.renderable = ui.getMesh().newInstance();

            Mesh mesh = ui.getMesh();

            for(int i = 0; i != mesh.vertexCount(); i++) {
                Vertex v = mesh.vertexAt(i);

                if(Math.abs(v.normal.x) > 0.5) {
                    v.color.set(1, 0, 0, 1);
                } else if(Math.abs(v.normal.y) > 0.5) {
                    v.color.set(0, 1, 0, 1);
                } else {
                    v.color.set(0, 0, 1, 1);
                }
            }
        }
    }

    public boolean inDesign() {
        return ui != null;
    }

    public int getTrianglesRendered() {
        return trianglesRendered;
    }

    public int getCollidableTriangles() {
        return collidableTriangles;
    }

    public File getLoadFile() {
        return loadFile;
    }

    public void render(Game game) throws Exception {
        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        trianglesRendered = 0;
        collidableTriangles = 0;

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                if(!n.componentAt(i).setup()) {
                    n.componentAt(i).init();
                }
            }
            return true;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                if(!n.componentAt(i).setup()) {
                    n.componentAt(i).start();
                }
            }
            return true;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        root.traverse((n) -> {
            for(int i = 0; i != n.componentCount(); i++) {
                n.componentAt(i).complete();
            }
            return true;
        });

        root.traverse((n) -> {
            if(n.visible) {
                if(n.renderable != null) {
                    n.renderable.update(game);
                    n.renderable.buffer(n, camera);
                }
                for(int i = 0; i != n.componentCount(); i++) {
                    n.componentAt(i).update();
                }
                return true;
            }
            return false;
        });

        camera.calcTransforms(game.aspectRatio());
        root.calcBoundsAndTransform(camera);

        root.traverse((n) -> {
            if (n.visible) {
                if (n.renderable != null) {
                    renderables.add(n);
                }
                if(n.isLight) {
                    lights.add(n);
                }
                return true;
            }
            return false;
        });
        renderables.sort((a, b) -> {
            if (a == b) {
                return 0;
            } else if (a.zOrder == b.zOrder) {
                float da = a.absolutePosition.distance(camera.eye);
                float db = b.absolutePosition.distance(camera.eye);
    
                return Float.compare(db, da);
            } else {
                return Integer.compare(a.zOrder, b.zOrder);
            }
        });
        lights.sort((a, b) -> {
            float d1 = a.absolutePosition.distance(camera.target);
            float d2 = b.absolutePosition.distance(camera.target);

            return Float.compare(d1, d2);
        });

        GL11.glEnable(GL11.GL_DEPTH_TEST);
        GL11.glEnable(GL11.GL_CULL_FACE);
        GL11.glDisable(GL11.GL_BLEND);
        GL11.glCullFace(GL11.GL_BACK);
        GL11.glFrontFace(GL11.GL_CCW);
        GL11.glDepthMask(true);
        GL11.glClearColor(backgroundColor.x, backgroundColor.y, backgroundColor.z, 1);
        GL11.glClear(GL11.GL_COLOR_BUFFER_BIT | GL11.GL_DEPTH_BUFFER_BIT);
        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glMultMatrix(camera.projection.put(m));
        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();
        GL11.glMultMatrix(camera.view.put(m));

        GL11.glEnable(GL11.GL_LIGHTING);

        for(int i = 0; i != 8; i++) {

            if(i < lights.size()) {
                Node light = lights.get(i);

                GL11.glEnable(GL11.GL_LIGHT0 + i);

                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_QUADRATIC_ATTENUATION, 0);
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_LINEAR_ATTENUATION, light.lightAttenuation);
                GL11.glLightf(GL11.GL_LIGHT0 + i, GL11.GL_CONSTANT_ATTENUATION, 1);

                v.put(0); v.put(0); v.put(0); v.put(1); v.flip();
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_SPECULAR, v);

                v.put(1); v.put(1); v.put(1); v.put(1); v.flip();
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_AMBIENT, v);

                v.put(light.lightColor.x); v.put(light.lightColor.y); v.put(light.lightColor.z); v.put(light.lightColor.w); v.flip();
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_DIFFUSE, v);

                v.put(light.position.x); v.put(light.position.y); v.put(light.position.z); v.put(1); v.flip();
                GL11.glLight(GL11.GL_LIGHT0 + i, GL11.GL_POSITION, v);
            } else {
                GL11.glDisable(GL11.GL_LIGHT0 + i);
            }
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_LIGHTING);

        if(ui != null) {
            if(drawLights) {
                for(Node light : lights) {
                    GL11.glPushMatrix();
                    GL11.glTranslatef(light.absolutePosition.x, light.absolutePosition.y, light.absolutePosition.z);
                    GL11.glScalef(0.5f, 0.5f, 0.5f);
                    ui.getMesh().render(ui, camera);
                    GL11.glPopMatrix();
                }
            }
            GL11.glPushMatrix();
            GL11.glTranslatef(camera.target.x, camera.target.y, camera.target.z);
            GL11.glScalef(0.5f, 0.5f, 0.5f);
            ui.getMesh().render(ui, camera);
            GL11.glPopMatrix();
        }
        for (Node renderable : renderables) {
            GL11.glPushMatrix();
            GL11.glMultMatrix(renderable.model.put(m));

            if(renderable.lightingEnabled) {
                GL11.glEnable(GL11.GL_LIGHTING);

                v.put(0); v.put(0); v.put(0); v.put(1); v.flip();
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_SPECULAR, v);

                v.put(renderable.ambientColor.x); v.put(renderable.ambientColor.y); v.put(renderable.ambientColor.z); v.put(renderable.ambientColor.w); v.flip();
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_AMBIENT, v);

                v.put(renderable.diffuseColor.x); v.put(renderable.diffuseColor.y); v.put(renderable.diffuseColor.z); v.put(renderable.diffuseColor.w); v.flip();
                GL11.glMaterial(GL11.GL_FRONT_AND_BACK, GL11.GL_DIFFUSE, v);
            } else {
                GL11.glDisable(GL11.GL_LIGHTING);

                Mesh mesh = renderable.getMesh();

                if(mesh != null) {
                    for(int i = 0; i != mesh.vertexCount(); i++) {
                        mesh.vertexAt(i).color.set(renderable.color);
                    }
                }
            }

            GL11.glDepthMask((renderable.depthWriteEnabled) ? true : false);
            if(renderable.depthTestEnabled) {
                GL11.glEnable(GL11.GL_DEPTH_TEST);
            } else {
                GL11.glDisable(GL11.GL_DEPTH_TEST);
            }
            if(renderable.blendEnabled) {
                GL11.glEnable(GL11.GL_BLEND);
            } else {
                GL11.glDisable(GL11.GL_BLEND);
            }
            if(renderable.additiveBlend) {
                GL11.glBlendFunc(GL11.GL_ONE, GL11.GL_ONE);
            } else {
                GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
            }
            if(renderable.cullState == CullState.NONE) {
                GL11.glDisable(GL11.GL_CULL_FACE);
            } else {
                GL11.glEnable(GL11.GL_CULL_FACE);
                if(renderable.cullState == CullState.BACK) {
                    GL11.glCullFace(GL11.GL_BACK);
                } else {
                    GL11.glCullFace(GL11.GL_FRONT);
                }
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE0);
            if(renderable.texture != null) {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderable.texture.id);
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }
            GL13.glActiveTexture(GL13.GL_TEXTURE1);
            if(renderable.texture2 != null) {
                GL11.glEnable(GL11.GL_TEXTURE_2D);
                GL11.glBindTexture(GL11.GL_TEXTURE_2D, renderable.texture2.id);
            } else {
                GL11.glDisable(GL11.GL_TEXTURE_2D);
            }
            int n = renderable.renderable.render(renderable, camera);
            if(renderable.collidable) {
                collidableTriangles += n;
            }
            trianglesRendered += n;
            GL11.glPopMatrix();
        }

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glDisable(GL11.GL_TEXTURE_2D);
        GL13.glActiveTexture(GL13.GL_TEXTURE1);
        GL11.glDisable(GL11.GL_TEXTURE_2D);

        GL11.glDisable(GL11.GL_LIGHTING);

        GL11.glDisable(GL11.GL_DEPTH_TEST);
        GL11.glDepthMask(false);
        GL11.glEnable(GL11.GL_BLEND);
        GL11.glBlendFunc(GL11.GL_SRC_ALPHA, GL11.GL_ONE_MINUS_SRC_ALPHA);
        GL11.glDisable(GL11.GL_CULL_FACE);

        GL11.glMatrixMode(GL11.GL_MODELVIEW);
        GL11.glLoadIdentity();

        GL11.glMatrixMode(GL11.GL_PROJECTION);
        GL11.glLoadIdentity();
        GL11.glOrtho(0, game.w(), game.h(), 0, -1, 1);

        root.traverse((n) -> {
            if(n.visible) {
                for(int i = 0; i != n.componentCount(); i++) {
                    n.componentAt(i).renderSprites();
                }
                return true;
            }
            return false;
        });

        if(!inDesign()) {
            root.traverse((n) -> {
                if(n.visible && loadFile == null) {
                    for(int i = 0; i != n.componentCount(); i++) {
                        File f = n.componentAt(i).loadFile();

                        if(f != null) {
                            loadFile = f;
                            return false;
                        }
                    }
                    return true;
                }
                return false;
            });
        }
        
        renderables.clear();
        lights.clear();
    }
}
