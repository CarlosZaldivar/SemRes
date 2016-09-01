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

class Settings {
    private static Settings instance = new Settings();

    private List<Database> databases = new ArrayList<>();

    static {
        try {
            instance = new Settings();
        } catch (Exception e) {
            throw new ExceptionInInitializerError(e);
        }
    }

    static void load() throws FileNotFoundException, YamlException, UnsupportedEncodingException {
        YamlReader reader = new YamlReader(new FileReader(getBaseDirectory() + "conf.yaml"));

        Map settingsMap = (Map) reader.read();
        ((Map) settingsMap.get("sources")).forEach((key, value) -> SourcesInitializer.initialize((String) key, (Map) value));

        ((Map) settingsMap.get("databases")).forEach((key, value) -> instance.databases.add(new Database((String) key, (String) value)));
    }

    List<Database> getDatabases() {
        return new ArrayList<Database>(databases);
    }

    void setDatabases(List<Database> newDatabases) {
        databases = (newDatabases == null) ? new ArrayList<>() : new ArrayList<>(newDatabases);
    }

    static Settings getInstance() {
        return instance;
    }

    static String getBaseDirectory() {
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
}

class Database {
    String name;
    String path;

    Database(String n, String p) {
        name = n;
        p = path;
    }
}