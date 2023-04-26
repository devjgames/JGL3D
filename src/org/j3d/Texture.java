package org.j3d;

import javax.imageio.ImageIO;

import org.lwjgl.BufferUtils;
import org.lwjgl.opengl.GL11;

import java.awt.image.BufferedImage;
import java.io.File;
import java.nio.IntBuffer;

public class Texture implements Resource {

    public final int[] pixels;
    public final int w;
    public final int h;
    public final File file;
    public final int id;
    public final IntBuffer buf;

    public Texture(int w, int h, File file) {
        pixels = new int[w * h];
        buf = BufferUtils.createIntBuffer(w * h);
        this.w = w;
        this.h = h;
        this.file = file;
        for (int i = 0; i != pixels.length; i++) {
            pixels[i] = 0xFFFFFFFF;
        }
        buf.put(pixels);
        buf.flip();
        id = GL11.glGenTextures();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MIN_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_MAG_FILTER, GL11.GL_NEAREST);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_S, GL11.GL_REPEAT);
        GL11.glTexParameteri(GL11.GL_TEXTURE_2D, GL11.GL_TEXTURE_WRAP_T, GL11.GL_REPEAT);
        GL11.glTexImage2D(GL11.GL_TEXTURE_2D, 0, GL11.GL_RGBA, w, h, 0, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);

        System.out.println("allocted texture - " + id);
    }

    public void buffer() throws Exception {
        buf.limit(buf.capacity());
        buf.position(0);
        buf.put(pixels);
        buf.flip();
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, id);
        GL11.glTexSubImage2D(GL11.GL_TEXTURE_2D, 0, 0, 0, w, h, GL11.GL_RGBA, GL11.GL_UNSIGNED_BYTE, buf);
        GL11.glBindTexture(GL11.GL_TEXTURE_2D, 0);
    }

    public static Texture load(File file) throws Exception {
        BufferedImage image = ImageIO.read(file.getAbsoluteFile());
        Texture texture = new Texture(image.getWidth(), image.getHeight(), file);

        image.getRGB(0, 0, texture.w, texture.h, texture.pixels, 0, texture.w);

        for(int i = 0; i != texture.pixels.length; i++) {
            int p = texture.pixels[i];

            texture.pixels[i] = (p & 0xFF000000) | (((p & 0xFF) << 16) & 0xFF0000) | (p & 0xFF00) | ((p >> 16) & 0xFF);
        }

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
