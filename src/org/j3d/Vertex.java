package org.j3d;

public class Vertex {

    public final Vec4 position = new Vec4();
    public final Vec2 textureCoordinate = new Vec2();
    public final Vec2 textureCoordinate2 = new Vec2();
    public final Vec4 color = new Vec4(1, 1, 1, 1);
    public final Vec3 normal = new Vec3();

    public Vertex() {
    }

    public Vertex(float x, float y, float z, float w, float s, float t, float u, float v, float r, float g, float b, float a, float nx, float ny, float nz) {
        set(x, y, z, w, s, t, u, v, r, g, b, a, nx, ny, nz);
    }

    public Vertex(Vec4 position, Vec2 textureCoordinate, Vec2 textureCoordinate2, Vec4 color, Vec3 normal) {
        set(position, textureCoordinate, textureCoordinate2, color, normal);
    }

    public Vertex(Vertex v) {
        set(v);
    }

    public void set(float x, float y, float z, float w, float s, float t, float u, float v, float r, float g, float b, float a, float nx, float ny, float nz) {
        position.set(x, y, z, w);
        textureCoordinate.set(s, t);
        textureCoordinate2.set(u, v);
        color.set(r, g, b, a);
        normal.set(nx, ny, nz);
    }

    public void set(Vec4 position, Vec2 textureCoordinate, Vec2 textureCoordinate2, Vec4 color, Vec3 normal) {
        this.position.set(position);
        this.textureCoordinate.set(textureCoordinate);
        this.textureCoordinate2.set(textureCoordinate2);
        this.color.set(color);
        this.normal.set(normal);
    }

    public void set(Vertex v) {
        position.set(v.position);
        textureCoordinate.set(v.textureCoordinate);
        textureCoordinate2.set(v.textureCoordinate2);
        color.set(v.color);
        normal.set(v.normal);
    }
}
