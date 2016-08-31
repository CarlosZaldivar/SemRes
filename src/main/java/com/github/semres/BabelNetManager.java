package com.github.semres;

import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.IOException;
import java.net.URLDecoder;
import java.util.List;

public class BabelNetManager {
    private static BabelNetManager instance;
    private BabelNet bn;

    public BabelNetManager() throws IOException {
        Configuration jltConf = Configuration.getInstance();
        jltConf.setConfigurationFile(new File("babelnet-config/config/jlt.properties"));

        BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
        String jarPath = URLDecoder.decode(getClass().getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        String path = jarPath.substring(0, jarPath.lastIndexOf('/'));
        babelnetConf.setConfigurationFile(new File(path + "/babelnet-config/config/babelnet.properties"));
        babelnetConf.setBasePath(path + "/babelnet-config/");
        bn =  BabelNet.getInstance();
    }

    public static BabelNetManager getInstance() throws IOException {
        if (instance == null) {
            instance = new BabelNetManager();
        }
        return instance;
    }

    List<BabelSynset> getSynsets(String str) throws java.io.IOException {
        return bn.getSynsets(str, Language.EN);
    }

    BabelSynset getSynset(String id) throws InvalidBabelSynsetIDException, java.io.IOException {
        return bn.getSynset(new BabelSynsetID(id));
    }
}
