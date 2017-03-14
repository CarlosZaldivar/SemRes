package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import com.esotericsoftware.yamlbeans.YamlReader;

import java.io.FileNotFoundException;
import java.io.FileReader;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

class Settings {
    private String databasesDirectory;
    private List<Source> sources = new ArrayList<>();

    Settings() throws FileNotFoundException, YamlException {
        YamlReader reader = new YamlReader(new FileReader(SemRes.getBaseDirectory() + "conf.yaml"));

        Map settingsMap = (Map) reader.read();
        ((Map) settingsMap.get("sources")).forEach((key, value) -> sources.add(SourcesInitializer.initialize((String) key, (Map) value)));

        String databasesDirectory = (String) settingsMap.get("databases-directory");
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
