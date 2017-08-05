package com.github.semres.babelnet;

import com.github.semres.*;
import com.github.semres.gui.EdgesAlreadyLoadedException;
import it.uniroma1.lcl.babelnet.*;
import it.uniroma1.lcl.babelnet.data.BabelGloss;
import it.uniroma1.lcl.babelnet.data.BabelPointer;
import it.uniroma1.lcl.jlt.Configuration;
import it.uniroma1.lcl.jlt.util.Language;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;

import java.io.*;
import java.util.*;

public class BabelNetManager extends Source {
    private Language language = Language.EN;
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

    public String getLanguage() {
        return language.toString();
    }

    public Language getJltLanguage() {
        return language;
    }

    public void setLanguage(String languageCode) {
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

    public List<BabelNetSynset> searchSynsets(String searchPhrase) throws IOException {
        List<BabelSynset> babelSynsets = BabelNet.getInstance().getSynsets(searchPhrase, language);
        List<BabelNetSynset> returnedSynsets = new ArrayList<>();

        for (BabelSynset babelSynset: babelSynsets) {
            returnedSynsets.add(createBabelNetSynset(babelSynset));
        }
        return returnedSynsets;
    }

    public BabelNetSynset getSynset(String id) throws IOException {
        try {
            return createBabelNetSynset(BabelNet.getInstance().getSynset(getLanguagesFilter(), new BabelSynsetID(id)));
        } catch (InvalidBabelSynsetIDException e) {
            throw new Error(e);
        }
    }

    private BabelNetSynset createBabelNetSynset(BabelSynset babelSynset) throws IOException {
        String id = babelSynset.getId().getID();

        BabelSense representationSense = babelSynset.getMainSense(language);
        String representation = representationSense != null ? representationSense.getSenseString() : "No representation";

        BabelGloss descriptionGloss = babelSynset.getMainGloss(language);
        String description = descriptionGloss != null ? descriptionGloss.getGloss() : null;

        return new BabelNetSynset(representation, id, description);
    }

    public void loadEdges(BabelNetSynset synset) throws IOException {
        if (synset.isDownloadedWithEdges()) {
            throw new EdgesAlreadyLoadedException();
        }

        BabelSynset babelSynset;
        try {
            babelSynset = BabelNet.getInstance().getSynset(getLanguagesFilter(), new BabelSynsetID(synset.getId()));
        } catch (InvalidBabelSynsetIDException e) {
            throw new Error(e);
        }

        List<BabelSynsetIDRelation> babelEdges = babelSynset.getEdges();
        babelEdges.sort(Comparator.comparing(BabelSynsetIDRelation::getWeight).reversed());

        // Download all edges with weight > 0 and if there's less than 10 of them, download edges with weight = 0 too.
        List<Edge> loadedEdges = new ArrayList<>();
        int counter = 10;
        for (BabelSynsetIDRelation edge: babelEdges) {
            if (edgeNotRemoved(synset, edge)) {
                loadedEdges.add(createBabelNetEdge(synset, edge));
            }
            --counter;
            if (counter <= 0 && edge.getWeight() == 0) {
                break;
            }
        }

        Map<String, Edge> currentEdges = synset.getOutgoingEdges();
        for (Edge edge : loadedEdges) {
            currentEdges.put(edge.getId(), edge);
        }
        synset.setOutgoingEdges(currentEdges);
        synset.downloadedWithEdges = true;
    }

    private BabelNetEdge createBabelNetEdge(BabelNetSynset synset, BabelSynsetIDRelation edge) {
        BabelPointer babelPointer = edge.getPointer();
        RelationType relationType = new RelationType(babelPointer.getRelationGroup().toString(), "BabelNet");
        return new BabelNetEdge(edge.getBabelSynsetIDTarget().getID(), synset.getId(), babelPointer.getName(), relationType, edge.getWeight());
    }

    private List<Language> getLanguagesFilter() {
        return Arrays.asList(language);
    }

    private boolean edgeNotRemoved(BabelNetSynset synset, BabelSynsetIDRelation edge) {
        return !synset.getRemovedRelations().contains(edge.getBabelSynsetIDTarget().getID());
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
