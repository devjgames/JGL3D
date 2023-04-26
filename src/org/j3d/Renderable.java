package org.j3d;

import java.io.File;

public abstract class Renderable {

    public abstract File file();

    public abstract AABB getBounds(Node node, Camera camera, AABB bounds);

    public abstract int triangleCount();

    public abstract Triangle triangleAt(Node node, Camera camera, int i, Triangle triangle);

    public abstract void buffer(Node node, Camera camera);

    public abstract int render(Node node, Camera camera);

    public abstract void update(Game game);

    public abstract Renderable newInstance();
}
