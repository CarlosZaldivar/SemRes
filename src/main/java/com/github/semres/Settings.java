package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Settings {
    private static Settings instance = new Settings();

    static {
        try {
            instance = new Settings();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    private List<Database> databases = new ArrayList<>();

    static void load() throws FileNotFoundException, YamlException, UnsupportedEncodingException {
        YamlReader reader = new YamlReader(new FileReader(getBaseDirectory() + "conf.yaml"));

        Map settingsMap = (Map) reader.read();
        ((Map) settingsMap.get("sources")).forEach((key, value) -> SourcesInitializer.initialize((String) key, (Map) value));

        ((Map) settingsMap.get("databases")).forEach((key, value) -> instance.databases.add(new Database((String) key, (String) value)));
    }

    public static Settings getInstance() {
        return instance;
    }

    public static String getBaseDirectory() {
        String path = null;
        try {
            path = URLDecoder.decode(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unsupported");
        }

        // Leaves only the directory
        path = path.substring(0, path.lastIndexOf('/') + 1);
        return path;
    }

    public List<Database> getDatabases() {
        return new ArrayList<Database>(databases);
    }

    public void setDatabases(List<Database> newDatabases) {
        databases = (newDatabases == null) ? new ArrayList<>() : new ArrayList<>(newDatabases);
    }
}

class Database {
    String name;
    String path;

    Database(String name, String path) {
        this.name = name;
        this.path = path;
    }
}