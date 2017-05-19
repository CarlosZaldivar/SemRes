package com.github.semres;

import com.github.semres.gui.Main;
import java.io.UnsupportedEncodingException;
import java.net.URLDecoder;
import javafx.application.Application;
import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.impl.SimpleValueFactory;


public class SemRes {
    public static final String baseIri = "https://github.com/CarlosZaldivar/SemRes/";

    // Classes
    public static final IRI SYNSET;
    public static final IRI EDGE;
    public static final IRI HOLONYM;
    public static final IRI HYPERNYM;
    public static final IRI HYPONYM;
    public static final IRI MERONYM;

    // Properties
    public static final IRI ID;
    public static final IRI RELATION_TYPE;
    public static final IRI REMOVED_RELATION;

    public static final IRI WEIGHT;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        SYNSET = factory.createIRI(SemRes.baseIri + "Synset");
        EDGE = factory.createIRI(SemRes.baseIri + "Edge");
        ID = factory.createIRI(SemRes.baseIri + "Id");
        REMOVED_RELATION = factory.createIRI(SemRes.baseIri + "RemovedRelation");
        RELATION_TYPE = factory.createIRI(SemRes.baseIri + "RelationType");
        HOLONYM = factory.createIRI(SemRes.baseIri + "Holonym");
        HYPERNYM = factory.createIRI(SemRes.baseIri + "Hypernym");
        HYPONYM = factory.createIRI(SemRes.baseIri + "Hyponym");
        MERONYM = factory.createIRI(SemRes.baseIri + "Meronym");

        WEIGHT = factory.createIRI(SemRes.baseIri + "Weight");
    }

    public static void main(String[] args) {
        Application.launch(Main.class, args);
    }

    public static String getBaseDirectory() {
        String path;
        try {
            path = URLDecoder.decode(Settings.class.getProtectionDomain().getCodeSource().getLocation().getPath(), "UTF-8");
        } catch (UnsupportedEncodingException e) {
            throw new AssertionError("UTF-8 is unsupported");
        }

        // Leaves only the directory
        path = path.substring(0, path.lastIndexOf('/') + 1);
        return path;
    }
}
