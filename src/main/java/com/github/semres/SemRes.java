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
    public static final IRI RELATION_TYPE_CLASS;

    // Database base IRI
    public static final IRI BASE_IRI;

    // Properties
    public static final IRI ID;
    public static final IRI RELATION_TYPE_PROPERTY;
    public static final IRI WEIGHT;
    public static final IRI SOURCE;
    public static final IRI LAST_EDITED;

    static {
        SimpleValueFactory factory = SimpleValueFactory.getInstance();
        BASE_IRI = factory.createIRI(baseIri + "classes/BaseIri");

        SYNSET = factory.createIRI(baseIri + "classes/Synset");
        EDGE = factory.createIRI(baseIri + "classes/Edge");
        RELATION_TYPE_CLASS = factory.createIRI(baseIri + "classes/RelationType");
        ID = factory.createIRI(baseIri + "properties/Id");
        RELATION_TYPE_PROPERTY = factory.createIRI(SemRes.baseIri + "properties/RelationType");
        WEIGHT = factory.createIRI(baseIri + "properties/Weight");
        SOURCE = factory.createIRI(baseIri + "properties/Source");
        LAST_EDITED = factory.createIRI(baseIri + "properties/LastEdited");
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
