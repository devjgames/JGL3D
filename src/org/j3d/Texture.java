package org.j3d;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

public class Texture implements Resource {

    public final int w;
    public final int h;
    public final File file;
    public final int id;
    public final IntBuffer buf;

    public Texture(int w, int h, int[] pixels, File file) {
        buf = BufferUtils.createIntBuffer(w * h);
        this.w = w;
        this.h = h;
        this.file = file;
        if(pixels != null) {
            buf.put(pixels);
            buf.flip();
        }
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, (pixels != null) ? buf : null);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        System.out.println("allocted texture - " + id);
    }

    public void buffer() throws Exception {
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static Texture load(File file) throws Exception {
        BufferedImage image = ImageIO.read(file.getAbsoluteFile());
        int w = image.getWidth();
        int h = image.getHeight();
        int[] pixels = new int[w * h];


        image.getRGB(0, 0, w, h, pixels, 0, w);

        for(int i = 0; i != pixels.length; i++) {
            int p = pixels[i];

            pixels[i] = (p & 0xFF000000) | (((p & 0xFF) << 16) & 0xFF0000) | (p & 0xFF00) | ((p >> 16) & 0xFF);
        }

        Texture texture = new Texture(w, h, pixels, file);

        texture.buffer();

        return texture;
    }

    @Override
    public String toString() {
        return file.getPath();
    }

    @Override
    public void destroy() throws Exception {
        if(id != 0) {
            System.out.println("destroying texture - " + id);
            GL11.glDeleteTextures(id);
        }
    }
}
