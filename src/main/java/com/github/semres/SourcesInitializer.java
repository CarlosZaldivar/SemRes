package com.github.semres;

import com.github.semres.babelnet.BabelNetManager;
import it.uniroma1.lcl.babelnet.BabelNetConfiguration;
import it.uniroma1.lcl.jlt.Configuration;

import java.io.File;
import java.util.Map;

class SourcesInitializer {
    static void initialize(String sourceName, Map parameters) {
        switch (sourceName) {
            case "BabelNet": {
                String basePath = Settings.getBaseDirectory();
                String confDirectory = (String) parameters.get("directory");
                BabelNetManager.getInstance().loadConfiguration(basePath + confDirectory);
                BabelNetManager.getInstance().setLanguage((String) parameters.get("language"));
            }
        }
    }
}
