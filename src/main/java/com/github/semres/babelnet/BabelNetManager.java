package com.github.semres.babelnet;

import com.github.semres.*;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.util.Language;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.io.File;
import java.io.IOException;
import java.util.ArrayList;
import java.util.List;

public class BabelNetManager extends Source {
    private static Language language = Language.EN;

    public static void loadConfiguration(String path) {
        Configuration jltConf = Configuration.getInstance();
        jltConf.setConfigurationFile(new File(path + "config/jlt.properties"));

        BabelNetConfiguration babelnetConf = BabelNetConfiguration.getInstance();
        babelnetConf.setConfigurationFile(new File(path + "config/babelnet.properties"));
        babelnetConf.setBasePath(path);
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
