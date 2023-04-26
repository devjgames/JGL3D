package org.j3d.demo;

import org.j3d.IO;
import org.j3d.NodeComponent;
import org.j3d.Texture;

public class Info extends NodeComponent {

    public boolean visible = true;
    private Texture font;

    @Override
    public void init() throws Exception {
        font = game().assets().load(IO.file("assets/font.png"));
    }
    
    @Override
    public void renderSprites() throws Exception {
        if(visible) {
            game().blit(
                font, 
                "F=" + game().frameRate() + "\nT=" + scene().getTrianglesRendered() + 
                "\nC=" + scene().getCollidableTriangles(), 
                10, 12, 16, 5, 10, 10, 2, 0, 1, 0, 1
            );
        }
    }
}
