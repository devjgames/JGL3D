package org.j3d;

import java.io.File;

import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;

public class Mesh extends Renderable {

    private Vertex[] vertices = null;
    private int[] indices = null;
    private int[][] polygons = null;
    private AABB bounds = new AABB();

    public Mesh(Vertex[] vertices, int[] indices, int[][] polygons) {
        this.vertices = vertices;
        this.indices = indices;
        this.polygons = polygons;

        calcBounds();
    }

    public Mesh(Mesh mesh) {
        vertices = new Vertex[mesh.vertices.length];
        indices = mesh.indices;
        polygons = mesh.polygons;

        for(int i = 0; i != vertices.length; i++) {
            vertices[i] = new Vertex(mesh.vertices[i]);
        }
        calcBounds();
    }

    @Override
    public File file() {
        return null;
    }

    public int vertexCount() {
        return vertices.length;
    }

    public int indexCount() {
        return indices.length;
    }

    public int polygonCount() {
        return polygons.length;
    }

    public Vertex vertexAt(int i) {
        return vertices[i];
    }

    public int indexAt(int i) {
        return indices[i];
    }

    public int polygonIndexCount(int i) {
        return polygons[i].length;
    }

    public int polygonIndexAt(int i, int j) {
        return polygons[i][j];
    }

    public void calcBounds() {
        bounds.clear();
        for(Vertex v : vertices) {
            bounds.add(v.position.x, v.position.y, v.position.z);
        }
    }

    @Override
    public AABB getBounds(Node node, Camera camera, AABB bounds) {
        return bounds.set(this.bounds);
    }

    @Override
    public int triangleCount() {
        return indices.length / 3;
    }

    @Override
    public Triangle triangleAt(Node node, Camera camera, int i, Triangle triangle) {
        Vec4 p1, p2, p3;
        i *= 3;
        p1 = vertices[indices[i++]].position;
        p2 = vertices[indices[i++]].position;
        p3 = vertices[indices[i++]].position;
        triangle.set(p1.x, p1.y, p1.z, p2.x, p2.y, p2.z, p3.x, p3.y, p3.z);
        return triangle;
    }

    @Override
    public void buffer(Node node, Camera camera) {
    }


    @Override
    public int render(Node node, Camera camera) {
        GL11.glBegin(GL11.GL_TRIANGLES);
        for(int i = 0; i != indices.length; i++) {
            Vertex v = vertices[indices[i]];

            GL11.glNormal3f(v.normal.x, v.normal.y, v.normal.z);
            GL11.glColor4f(v.color.x, v.color.y, v.color.z, v.color.w);
            GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, v.textureCoordinate.x, v.textureCoordinate.y);
            GL13.glMultiTexCoord2f(GL13.GL_TEXTURE1, v.textureCoordinate2.x, v.textureCoordinate2.y);
            GL11.glVertex3f(v.position.x, v.position.y, v.position.z);
        }
        GL11.glEnd();
        return triangleCount();
    }

    @Override
    public void update(Game game) {
    }

    @Override
    public Renderable newInstance() {
        return new Mesh(this);
    }
}
