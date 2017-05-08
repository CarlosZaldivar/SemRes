package com.github.semres;

import com.github.semres.babelnet.BabelNetManager;

import java.util.Map;

class SourcesInitializer {
    static Source initialize(String sourceName, Map parameters) {
        switch (sourceName) {
            case "BabelNet": {
                String basePath = SemRes.getBaseDirectory();
                String confDirectory = (String) parameters.get("directory");
                BabelNetManager.loadConfiguration(basePath + confDirectory);
                BabelNetManager.setLanguage((String) parameters.get("language"));
                return new BabelNetManager();
            }
        }
        return null;
    }
}
