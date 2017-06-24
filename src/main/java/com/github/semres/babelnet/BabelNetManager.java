package com.github.semres.babelnet;

import com.github.semres.*;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.util.Language;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.io.*;
import java.util.ArrayList;
import java.util.List;
import java.util.Properties;

public class BabelNetManager extends Source {
    private static Language language = Language.EN;
    private static String configurationPath;
    private static String apiKey;

    public static void setConfigurationPath(String configurationPath) {
        BabelNetManager.configurationPath = configurationPath;
    }

    public static void loadConfiguration() {
        Configuration jltConf = Configuration.getInstance();
        jltConf.setConfigurationFile(new File(configurationPath + "config/jlt.properties"));

        BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
        babelnetConf.setConfigurationFile(new File(configurationPath + "config/babelnet.properties"));
        apiKey = babelnetConf.getBabelNetKey();
    }

    public static String getLanguage() {
        return language.toString();
    }

    public static Language getJltLanguage() {
        return language;
    }

    public static void setLanguage(String languageCode) {
        language = Language.valueOf(languageCode);
    }

    public static void setApiKey(String apiKey) throws IOException {
        // https://stackoverflow.com/a/15337487/5459240
        FileInputStream in = new FileInputStream(configurationPath + "config/babelnet.var.properties");
        Properties props = new Properties();
        props.load(in);
        in.close();

        FileOutputStream out = new FileOutputStream(configurationPath + "config/babelnet.var.properties");
        props.setProperty("babelnet.key", apiKey);
        props.store(out, null);
        out.close();
        BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
        babelnetConf.setConfigurationFile(new File(configurationPath + "config/babelnet.properties"));
    }

    public static String getApiKey() {
        return apiKey;
    }

    public BabelSynset getBabelSynset(String id) throws IOException {
        try {
            return getBabelSynset(new BabelSynsetID(id));
        } catch (InvalidBabelSynsetIDException e) {
            throw new RuntimeException(e);
        }
    }

    private BabelSynset getBabelSynset(BabelSynsetID id) throws IOException {
        return BabelNet.getInstance().getSynset(id);
    }

    public List<BabelNetSynset> getSynsets(String word) throws IOException {
        List<BabelSynset> babelSynsets = BabelNet.getInstance().getSynsets(word, language);
        List<BabelNetSynset> returnedSynsets = new ArrayList<>();

        for (BabelSynset babelSynset: babelSynsets) {
            returnedSynsets.add(new BabelNetSynset(babelSynset));
        }

        return returnedSynsets;
    }

    public BabelNetSynset getSynset(String id) throws IOException {
        try {
            return new BabelNetSynset(BabelNet.getInstance().getSynset(new BabelSynsetID(id)));
        } catch (InvalidBabelSynsetIDException e) {
            throw new Error(e.getMessage());
        }
    }

    @Override
    public Class<? extends SynsetSerializer> getSynsetSerializerClass() {
        return BabelNetSynsetSerializer.class;
    }

    @Override
    public Class<? extends EdgeSerializer> getEdgeSerializerClass() {
        return BabelNetEdgeSerializer.class;
    }

    @Override
    public List<RelationType> getRelationTypes() {
        List<RelationType> relationTypes = new ArrayList<>();
        relationTypes.add(new RelationType("HOLONYM", "BabelNet"));
        relationTypes.add(new RelationType("HYPERNYM", "BabelNet"));
        relationTypes.add(new RelationType("HYPONYM", "BabelNet"));
        relationTypes.add(new RelationType("MERONYM", "BabelNet"));
        relationTypes.add(new RelationType("OTHER", "BabelNet"));
        return relationTypes;
    }

    @Override
    public Model getMetadataStatements() {
        Model model = new LinkedHashModel();

        model.add(CommonIRI.BABELNET_SYNSET, RDF.TYPE, RDFS.CLASS);
        model.add(CommonIRI.BABELNET_SYNSET, RDFS.SUBCLASSOF, SemRes.SYNSET);

        model.add(CommonIRI.BABELNET_EDGE, RDF.TYPE, RDFS.CLASS);
        model.add(CommonIRI.BABELNET_EDGE, RDFS.SUBCLASSOF, SemRes.EDGE);

        model.add(CommonIRI.EDGES_DOWNLOADED, RDF.TYPE, RDF.PROPERTY);
        return model;
    }
}
