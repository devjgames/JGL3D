package org.j3d.demo;

import org.j3d.GameEditor;
import org.j3d.Utils;

public class App {

    public static void main(String[] args) throws Exception {
        Utils.setNimbusLookAndFeel();

       new GameEditor(
            800, 600, 1, true,
            Info.class.getName(),
            Warp.class.getName(),
            Sky.class.getName(),
            FireLight.class.getName(),
            Player.class.getName()
        );
    }
}