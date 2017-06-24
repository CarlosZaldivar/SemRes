package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;
import com.github.semres.user.UserManager;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

public class Settings {
    private String databasesDirectory;
    private List<Source> sources = new ArrayList<>();

    public Settings() throws FileNotFoundException, YamlException {
        YamlReader reader = new YamlReader(new FileReader(SemRes.getBaseDirectory() + "conf.yaml"));

        Map settingsMap = (Map) reader.read();
        Map sourcesMap = (Map) settingsMap.get("sources");

        if (sourcesMap == null) {
            throw new YamlException();
        }

        for (Object key : sourcesMap.keySet()) {
            Source source = SourcesInitializer.initialize((String) key, (Map) sourcesMap.get(key));
            if (source == null) {
                throw new YamlException();
            }
            sources.add(source);
        }

        // Always add custom user synsets and edges.
        sources.add(new UserManager());

        String databasesDirectory = (String) settingsMap.get("databases-directory");
        if (databasesDirectory == null) {
            throw new YamlException();
        }
        this.databasesDirectory = databasesDirectory.startsWith("/") ? databasesDirectory : SemRes.getBaseDirectory() + databasesDirectory;
    }

    Settings(String databasesDirectory, List<Source> sources) {
        this.databasesDirectory = databasesDirectory;
        this.sources = sources;
    }

    String getDatabasesDirectory() {
        return databasesDirectory;
    }

    public void setDatabasesDirectory(String databasesDirectory) {
        this.databasesDirectory = databasesDirectory;
    }

    List<Source> getSources() {
        return new ArrayList<>(sources);
    }
}
