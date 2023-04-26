package org.j3d;

import javax.imageio.ImageIO;
import javax.swing.*;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.AWTGLCanvas;
import org.lwjgl.opengl.GL11;
import org.lwjgl.opengl.GL13;
import org.lwjgl.opengl.PixelFormat;

import java.awt.*;
import java.awt.event.*;
import java.awt.image.BufferedImage;
import java.io.*;
import java.nio.IntBuffer;
import java.text.SimpleDateFormat;
import java.util.Date;

public final class Game {

    public static interface GameLoop {
        void resize(Game game) throws Exception;

        void init(Game game) throws Exception;

        void render(Game game) throws Exception;
    }

    public static class PlayLoop implements GameLoop {

        private File file;
        private Scene scene;

        public PlayLoop(File file) {
            this.file = file;
        }

        @Override
        public void resize(Game game) throws Exception {
        }
    
        @Override
        public void init(Game game) throws Exception {
            scene = Serializer.deserialize(game, false, file);
        }
    
        @Override
        public void render(Game game) throws Exception {
            scene.render(game);

            File f = scene.getLoadFile();

            if(f != null) {
                scene = null;
                game.assets().clear();
                scene = Serializer.deserialize(game, false, file);
            }
        }
    }

    public static void play(int w, int h, int scale, boolean resizable, File file) throws Exception {
        JFrame frame = new JFrame("J3D");

        frame.setDefaultCloseOperation(JFrame.EXIT_ON_CLOSE);
        frame.setResizable(resizable);
        frame.setLayout(new BorderLayout());

        Game game = new Game(scale, new PlayLoop(file));

        game.getCanvas().setPreferredSize(new Dimension(w * scale, h * scale));

        frame.add(game.getCanvas(), BorderLayout.CENTER);
        frame.pack();
        frame.setVisible(true);
    }

    private static boolean hasError = false;

    public static void checkError(String tag) {
        if(!hasError) {
            int err = GL11.glGetError();

            if(err != 0) {
                hasError = true;
                System.out.println(tag + ":" + err);
            }
        }
    }

    private class Listener implements MouseListener, MouseMotionListener, KeyListener {

        @Override
        public void keyTyped(KeyEvent e) {

        }

        @Override
        public void keyPressed(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = true;
            }
        }

        @Override
        public void keyReleased(KeyEvent e) {
            int c = e.getKeyCode();
            if (c >= 0 && c < keyState.length) {
                keyState[c] = false;
            }
        }

        @Override
        public void mouseClicked(MouseEvent e) {

        }

        @Override
        public void mousePressed(MouseEvent e) {
            lastX = mouseX = e.getX();
            lastY = mouseY = e.getY();
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = true;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = true;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = true;
            }
        }

        @Override
        public void mouseReleased(MouseEvent e) {
            if (e.getButton() == MouseEvent.BUTTON1) {
                buttonState[0] = false;
            } else if (e.getButton() == MouseEvent.BUTTON2) {
                buttonState[1] = false;
            } else if (e.getButton() == MouseEvent.BUTTON3) {
                buttonState[2] = false;
            }
        }

        @Override
        public void mouseEntered(MouseEvent e) {

        }

        @Override
        public void mouseExited(MouseEvent e) {

        }

        @Override
        public void mouseDragged(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }

        @Override
        public void mouseMoved(MouseEvent e) {
            mouseX = e.getX();
            mouseY = e.getY();
            dX = lastX - mouseX;
            dY = mouseY - lastY;
            lastX = mouseX;
            lastY = mouseY;
        }
    }

    private AssetManager assets = new AssetManager();
    private int mouseX = 0;
    private int mouseY = 0;
    private int dX = 0;
    private int dY = 0;
    private int lastX = 0;
    private int lastY = 0;
    private boolean[] buttonState = new boolean[]{false, false, false};
    private boolean[] keyState = new boolean[256];
    private double lastTime = 0;
    private double totalTime = 0;
    private double elapsedTime = 0;
    private double seconds = 0;
    private int frames = 0;
    private int fps = 0;
    private Game me;
    private boolean takeSnapShot = false;
    private AWTGLCanvas canvas;
    private IntBuffer pixels = null;
    private int lw = 0;
    private int lh = 0;
    private int scale;

    public Game(int scale, GameLoop loop) throws Exception {
        me = this;
        this.scale = scale;
        for (int i = 0; i != keyState.length; i++) {
            keyState[i] = false;
        }
        canvas = new AWTGLCanvas(new PixelFormat(32, 8, 32, 0, 0)) {

            @Override
            protected void initGL() {
                try {
                    loop.init(me);
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            }

            @Override
            protected void paintGL() {
                try {
                    
                    int w = getWidth();
                    int h = getHeight();

                    if(w > 20 && h > 20 && (lw != w || lh != h)) {
                        System.out.println("creating pixel buffer ... " + w / scale + " x " + h /scale);
                        lw = w;
                        lh = h;
                        pixels = BufferUtils.createIntBuffer((w / scale) * (h / scale));
                    }

                    makeCurrent();
                    GL11.glViewport(0, 0, w / scale, h / scale);
                    loop.render(me);
                    if(scale > 1 || takeSnapShot) {
                        pixels.limit(pixels.capacity());
                        pixels.position(0);
                        GL11.glRasterPos2i(0, 0);
                        GL11.glReadPixels(0, 0, w / scale, h / scale, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
                        pixels.limit(pixels.capacity());
                        pixels.position(0);
                        GL11.glViewport(0, 0, w, h);
                        GL11.glDisable(GL11.GL_LIGHTING);
                        GL13.glActiveTexture(GL13.GL_TEXTURE1);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL13.glActiveTexture(GL13.GL_TEXTURE0);
                        GL11.glDisable(GL11.GL_TEXTURE_2D);
                        GL11.glDisable(GL11.GL_DEPTH_TEST);
                        GL11.glDepthMask(false);
                        GL11.glDisable(GL11.GL_CULL_FACE);
                        GL11.glDisable(GL11.GL_BLEND);
                        GL11.glMatrixMode(GL11.GL_MODELVIEW);
                        GL11.glLoadIdentity();
                        GL11.glMatrixMode(GL11.GL_PROJECTION);
                        GL11.glLoadIdentity();
                        GL11.glOrtho(0, w, 0, h, -1, 1);
                        GL11.glColor4f(1, 1, 1, 1);
                        GL11.glPixelStorei(GL11.GL_UNPACK_ALIGNMENT, 4);
                        GL11.glRasterPos2i(0, 0);
                        GL11.glPixelZoom(scale, scale);
                        GL11.glDrawPixels(w / scale, h / scale, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, pixels);
                    }
                    checkError("paintGL");
                    GL11.glFlush();
                    swapBuffers();

                    if(takeSnapShot) {
                        takeSnapShot = false;

                        System.out.println("taking snap shot ...");

                        BufferedImage snapShot = new BufferedImage(w / scale, h / scale, BufferedImage.TYPE_INT_ARGB);
                        
                        try {
                            SimpleDateFormat format = new SimpleDateFormat("yyyy-MM-dd-HH-mm-ss");
                            File file = IO.file("screen-shots/" + format.format(new Date()) + ".png");
                            int[] pls = new int[(w / scale) * (h / scale)];

                            for(int i = 0; i != pls.length; i++) {
                                int p = pixels.get(i);

                                pls[i] = (p & 0xFF000000) | (((p & 0xFF) << 16) & 0xFF0000) | (p & 0xFF00) | ((p >> 16) & 0xFF);
                            }
                            snapShot.setRGB(0, 0, w, h, pls, 0, w);

                            BufferedImage flip = new BufferedImage(w, h, BufferedImage.TYPE_INT_ARGB);
                            Graphics g = null;

                            try {
                                g = flip.createGraphics();
                                g.drawImage(snapShot, 0, h, w, -h, null);
                            } finally {
                                if(g != null) {
                                    g.dispose();
                                }
                            }
        
                            file.getParentFile().mkdirs();
                            ImageIO.write(flip, "PNG", file);
                        } catch(Exception ex) {
                            ex.printStackTrace(System.out);
                        }
                    }

                    tick();
                    repaint();
                } catch(Exception ex) {
                    ex.printStackTrace(System.out);
                }
            };
        };
        canvas.addMouseListener(new Listener());
        canvas.addMouseMotionListener(new Listener());
        canvas.addKeyListener(new Listener());
    }

    public AWTGLCanvas getCanvas() {
        return canvas;
    }

    public AssetManager assets() { return assets; }

    public int mouseX() {
        return mouseX / scale;
    }

    public int mouseY() {
        return mouseY / scale;
    }

    public int dX() {
        return dX;
    }

    public int dY() {
        return dY;
    }

    public boolean buttonDown(int i) {
        return buttonState[i];
    }

    public boolean keyDown(int i) {
        return keyState[i];
    }

    public int w() {
        return canvas.getWidth() / scale;
    }

    public int h() {
        return canvas.getHeight() / scale;
    }

    public float aspectRatio() {
        return w() / (float) h();
    }

    public float totalTime() {
        return (float) totalTime;
    }

    public float elapsedTime() {
        return (float) elapsedTime;
    }

    public int frameRate() {
        return fps;
    }

    public void resetTimer() {
        lastTime = System.nanoTime() / 1000000000.0;
        totalTime = 0;
        elapsedTime = 0;
        seconds = 0;
        frames = 0;
        fps = 0;
    }

    public void blit(Texture texture, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh, float r, float g, float b, float a) {
        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, texture.id);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        blit(texture, sx, sy, sw, sh, dx, dy, dw, dh);
        GL11.glEnd();
    }

    private void blit(Texture texture, int sx, int sy, int sw, int sh, int dx, int dy, int dw, int dh) {
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, sx / (float)texture.w, sy / (float)texture.h);
        GL11.glVertex2f(dx, dy);
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, sx / (float)texture.w, (sy + sh) / (float)texture.h);
        GL11.glVertex2f(dx, dy + dh);
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, (sx + sw) / (float)texture.w, (sy + sh) / (float)texture.h);
        GL11.glVertex2f(dx + dw, dy + dh);
        GL13.glMultiTexCoord2f(GL13.GL_TEXTURE0, (sx + sw) / (float)texture.w, sy / (float)texture.h);
        GL11.glVertex2f(dx + dw, dy);
    }

    public void blit(Texture font, String text, int cw, int ch, int cols, int ls, int x, int y, int scale, float r, float g, float b, float a) {
        int sx = x;

        GL13.glActiveTexture(GL13.GL_TEXTURE0);
        GL11.glEnable(GL11.GL_TEXTURE_2D);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, font.id);
        GL11.glColor4f(r, g, b, a);
        GL11.glBegin(GL11.GL_QUADS);
        for(int i = 0; i != text.length(); i++) {
            char c = text.charAt(i);

            if(c == '\n') {
                x = sx;
                y += ls + ch * scale;
            } else {
                int j = (int)c - (int)' ';

                if(j >= 0 && j < 100) {
                    int col = j % cols;
                    int row = j / cols;

                    blit(font, col * cw, row * ch, cw, ch, x, y, cw * scale, ch * scale);
                    x += cw * scale;
                }
            }
        }
        GL11.glEnd();
    }

    void tick() {
        dX = 0;
        dY = 0;

        double nowTime = System.nanoTime() / 1000000000.0;
        elapsedTime = nowTime - lastTime;
        lastTime = nowTime;
        seconds += elapsedTime;
        totalTime += elapsedTime;
        frames++;
        if (seconds >= 1) {
            fps = frames;
            frames = 0;
            seconds = 0;
        }
    }

    public void takeSnapShot() {
        takeSnapShot = true;
    }
}
