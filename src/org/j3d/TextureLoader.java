package org.j3d;

import java.io.File;

public class TextureLoader implements AssetLoader {

    @Override
    public Object load(File file, AssetManager assets) throws Exception {
        return Texture.load(file);
    }
}
