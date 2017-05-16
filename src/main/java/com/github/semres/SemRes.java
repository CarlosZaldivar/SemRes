package com.github.semres;

import com.github.semres.gui.Main;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javafx.application.Application;


public class SemRes {
    public static String baseIri = "https://github.com/CarlosZaldivar/SemRes/";

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    public static String getBaseDirectory() {
        String path;
        try {
            path = URLDecoder.decode(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unsupported");
        }

        // Leaves only the directory
        path = path.substring(0, path.lastIndexOf('/') + 1);
        return path;
    }
}
