package com.github.semres.babelnet;

import com.github.semres.Source;
import it.uniroma1.lcl.babelnet.BabelNet;
import it.uniroma1.lcl.babelnet.BabelNetConfiguration;
import it.uniroma1.lcl.babelnet.BabelSynset;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.util.Language;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BabelNetManager extends Source {
    public static BabelNetManager getInstance() {
        return instance;
    }

    private static final BabelNetManager instance = new BabelNetManager();
    private Language language = Language.EN;

    public void loadConfiguration(String path) {
        Configuration jltConf = Configuration.getInstance();
        jltConf.setConfigurationFile(new File(path + "config/jlt.properties"));

        BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
        babelnetConf.setConfigurationFile(new File(path + "config/babelnet.properties"));
        babelnetConf.setBasePath(path);
    }

    public String getLanguage() {
        return language.toString();
    }

    public Language getJltLanguage() {
        return language;
    }

    public void setLanguage(String languageCode) {
        language = Language.valueOf(languageCode);
    }

    public List<BabelNetSynset> getSynsets(String word) throws IOException {
        List<BabelSynset> babelSynsets = BabelNet.getInstance().getSynsets(word, language);
        List<BabelNetSynset> returnedSynsets = new ArrayList<>();

        for (BabelSynset babelSynset: babelSynsets) {
            returnedSynsets.add(new BabelNetSynset(babelSynset));
        }

        return returnedSynsets;
    }

    @Override
    public Class getSerializerClass() {
        return BabelNetSynsetSerializer.class;
    }

    @Override
    public Class getSynsetClass() {
        return BabelNetSynset.class;
    }
}
