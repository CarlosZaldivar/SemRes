package com.github.semres;

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
                Configuration jltConf = Configuration.getInstance();
                jltConf.setConfigurationFile(new File(basePath + confDirectory + "config/jlt.properties"));

                BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
                babelnetConf.setConfigurationFile(new File(basePath + confDirectory + "config/babelnet.properties"));
                babelnetConf.setBasePath(basePath + confDirectory);
            }
        }
    }
}
