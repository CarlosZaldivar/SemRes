package com.github.semres;

import com.esotericsoftware.yamlbeans.YamlException;
import com.github.semres.babelnet.BabelNetManager;

import java.util.Map;

class SourcesInitializer {
    static Source initialize(String sourceName, Map parameters) throws YamlException {
        switch (sourceName) {
            case "BabelNet": {
                String basePath = SemRes.getBaseDirectory();
                String confDirectory = (String) parameters.get("directory");
                if (confDirectory == null) {
                    throw new YamlException();
                }
                BabelNetManager.setConfigurationPath(basePath + confDirectory);
                BabelNetManager.loadConfiguration();

                String language = (String) parameters.get("language");
                if (language == null) {
                    throw new YamlException();
                }
                BabelNetManager.setLanguage(language);
                return new BabelNetManager();
            }
        }
        return null;
    }
}
