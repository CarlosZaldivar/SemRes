package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.security.SecureRandom;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;
import java.util.concurrent.ThreadLocalRandom;

class Database {
    private List<SynsetSerializer> synsetSerializers;
    private List<EdgeSerializer> edgeSerializers;
    private Repository repository;

    Database(List<Class<? extends SynsetSerializer>> synsetSerializerClasses, List<Class<? extends EdgeSerializer>> edgeSerializerClasses,
             Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<SynsetSerializer> synsetSerializers = new ArrayList<>();
        String baseIri = "http://example.org/";
        for (Class<? extends SynsetSerializer> serializerClass: synsetSerializerClasses) {
            SynsetSerializer loadedSynsetSerializer = serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            synsetSerializers.add(loadedSynsetSerializer);
        }
        this.synsetSerializers = synsetSerializers;

        List<EdgeSerializer> edgeSerializers = new ArrayList<>();
        for (Class<? extends EdgeSerializer> serializerClass: edgeSerializerClasses) {
            EdgeSerializer loadedEdgeSerializer = serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            edgeSerializers.add(loadedEdgeSerializer);
        }
        this.edgeSerializers = edgeSerializers;

        this.repository = repository;
        this.repository.initialize();
    }

    void addSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForSynset(synset).synsetToRdf(synset));
        }
    }

    void addEdge(Edge edge) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForEdge(edge).edgeToRdf(edge));
        }
    }

    void editSynset(Synset edited, Synset original) {
        if (!edited.getId().equals(original.getId())) {
            throw new IllegalArgumentException("Original and edited synsets have different IDs.");
        }

        try (RepositoryConnection conn = repository.getConnection()) {
            conn.remove(getSerializerForSynset(original).synsetToRdf(original));
            conn.add(getSerializerForSynset(edited).synsetToRdf(edited));
        }
    }

    boolean hasSynset(String id) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = String.format("ASK  { ?synset <%s> ?type . ?type <%s> <%s> . ?synset <%s> %s }",
                    RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET, SR.ID, conn.getValueFactory().createLiteral(id));
            BooleanQuery query = conn.prepareBooleanQuery(queryString);
            return query.evaluate();
        }
    }

    void removeSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {
            // Remove edges. It could be optimized by checking if the edges are not already loaded.
            List<Edge> outgoingEdges = getOutgoingEdges(synset);
            List<Edge> pointingEdges = getPointingEdges(synset);

            outgoingEdges.forEach(this::removeEdge);
            pointingEdges.forEach(this::removeEdge);

            // Remove synset
            conn.remove(getSerializerForSynset(synset).synsetToRdf(synset));
        }
    }

    void removeEdge(Edge edge) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.remove(getSerializerForEdge(edge).edgeToRdf(edge));
        }
    }

    private Synset getSynset(IRI synsetIri, String type) {
        return getSerializerForSynset(type).rdfToSynset(synsetIri);
    }

    private Edge getEdge(IRI edgeIri, String type, Synset originSynset, Synset pointedSynset) {
        return getSerializerForEdge(type).rdfToEdge(edgeIri, pointedSynset, originSynset);
    }

    List<Synset> getSynsets() {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> }", RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET);
        return getSynsets(queryString);
    }

    List<Synset> searchSynsets(String searchPhrase) {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> . ?synset <%s> ?label ." +
                        " filter contains(lcase(str(?label)), lcase(str(\"%s\"))) }",
                RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET, RDFS.LABEL, searchPhrase);
        return getSynsets(queryString);
    }

    private List<Synset> getSynsets(String queryString) {
        List<Synset> synsets = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String type = bindingSet.getValue("type").stringValue();
                    IRI synsetIri = factory.createIRI(bindingSet.getValue("synset").stringValue());

                    synsets.add(getSynset(synsetIri, type));
                }
            }
        }
        return synsets;
    }

    Model getAllStatements() {
        try (RepositoryConnection conn = repository.getConnection()) {
            GraphQueryResult graphResult = conn.prepareGraphQuery("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o }").evaluate();
            return QueryResults.asModel(graphResult);
        }
    }

    List<Edge> getOutgoingEdges(Synset originSynset) {
        List<Edge> edges = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edgeType ?edge ?pointedSynset ?pointedSynsetType" +
                " WHERE { ?originSynset <%s> %s . ?originSynset ?edge ?pointedSynset . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> ." +
                         "?pointedSynset <%s> ?pointedSynsetType }",
                SR.ID, factory.createLiteral(originSynset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SR.EDGE, RDF.TYPE);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String edgeType = bindingSet.getValue("edgeType").stringValue();
                    IRI edgeIri = factory.createIRI(bindingSet.getValue("edge").stringValue());
                    IRI pointedSynsetIri = factory.createIRI(bindingSet.getValue("pointedSynset").stringValue());
                    String pointedSynsetType = bindingSet.getValue("pointedSynsetType").stringValue();

                    Synset pointedSynset = getSynset(pointedSynsetIri, pointedSynsetType);
                    edges.add(getEdge(edgeIri, edgeType, originSynset, pointedSynset));
                }
            }
        }
        return edges;
    }

    List<Edge> getPointingEdges(Synset pointedSynset) {
        List<Edge> edges = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edgeType ?edge ?originSynset ?originSynsetType" +
                        " WHERE { ?pointedSynset <%s> %s . ?originSynset ?edge ?pointedSynset . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> ." +
                        "?originSynset <%s> ?originSynsetType }",
                SR.ID, factory.createLiteral(pointedSynset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SR.EDGE, RDF.TYPE);

        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String edgeType = bindingSet.getValue("edgeType").stringValue();
                    IRI edgeIri = factory.createIRI(bindingSet.getValue("edge").stringValue());
                    IRI originSynsetIri = factory.createIRI(bindingSet.getValue("originSynset").stringValue());
                    String originSynsetType = bindingSet.getValue("originSynsetType").stringValue();

                    Synset originSynset = getSynset(originSynsetIri, originSynsetType);
                    edges.add(getEdge(edgeIri, edgeType, originSynset, pointedSynset));
                }
            }
        }
        return edges;
    }

    private SynsetSerializer getSerializerForSynset(Synset synset) {
        SynsetSerializer serializer;
        serializer = synsetSerializers
                .stream()
                .filter(x -> x.getSynsetClass().equals(synset.getClass().getCanonicalName()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
        return serializer;
    }

    private SynsetSerializer getSerializerForSynset(String type) {
        SynsetSerializer serializer;
        serializer = synsetSerializers
                .stream()
                .filter(x -> x.getSynsetClassIri().stringValue().equals(type))
                .findFirst().orElseThrow(IllegalArgumentException::new);
        return serializer;
    }

    private EdgeSerializer getSerializerForEdge(Edge edge) {
        EdgeSerializer serializer;
        serializer = edgeSerializers
                .stream()
                .filter(x -> x.getEdgeClass().equals(edge.getClass().getCanonicalName()))
                .findFirst().orElseThrow(IllegalArgumentException::new);
        return serializer;
    }

    private EdgeSerializer getSerializerForEdge(String type) {
        EdgeSerializer serializer;
        serializer = edgeSerializers
                .stream()
                .filter(x -> x.getEdgeClassIri().stringValue().equals(type))
                .findFirst().orElseThrow(IllegalArgumentException::new);
        return serializer;
    }

    /**
     * Generate unique synset id.
     * @return Unique synset id.
     */
    public String generateNewSynsetId() {
        Random random = new Random();
        String id;
        while (true) {
            id = new BigInteger(130, random).toString(32);
            if (!hasSynset(id)) {
                break;
            }
        }
        return id;
    }
}
