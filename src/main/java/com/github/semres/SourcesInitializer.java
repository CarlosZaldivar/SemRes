package com.github.semres;

import com.github.semres.babelnet.BabelNetManager;

import java.util.Map;

class SourcesInitializer {
    static Source initialize(String sourceName, Map parameters) {
        switch (sourceName) {
            case "BabelNet": {
                String basePath = SemRes.getBaseDirectory();
                String confDirectory = (String) parameters.get("directory");
                BabelNetManager.getInstance().loadConfiguration(basePath + confDirectory);
                BabelNetManager.getInstance().setLanguage((String) parameters.get("language"));
                return BabelNetManager.getInstance();
            }
        }
        return null;
    }
}
