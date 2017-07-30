package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.impl.LinkedHashModel;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.rio.RDFFormat;
import org.eclipse.rdf4j.rio.Rio;

import java.io.StringWriter;
import java.lang.reflect.InvocationTargetException;
import java.math.BigInteger;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Random;

public class Database {
    private final List<SynsetSerializer> synsetSerializers;
    private final List<EdgeSerializer> edgeSerializers;
    private final Repository repository;
    private final String baseIri;

    Database(List<Class<? extends SynsetSerializer>> synsetSerializerClasses, List<Class<? extends EdgeSerializer>> edgeSerializerClasses,
             Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        this.repository = repository;
        this.repository.initialize();

        this.baseIri = getBaseIri();

        List<SynsetSerializer> synsetSerializers = new ArrayList<>();
        for (Class<? extends SynsetSerializer> serializerClass: synsetSerializerClasses) {
            SynsetSerializer loadedSynsetSerializer = serializerClass.getConstructor(String.class).newInstance(baseIri);
            synsetSerializers.add(loadedSynsetSerializer);
        }
        this.synsetSerializers = synsetSerializers;

        List<EdgeSerializer> edgeSerializers = new ArrayList<>();
        for (Class<? extends EdgeSerializer> serializerClass: edgeSerializerClasses) {
            EdgeSerializer loadedEdgeSerializer = serializerClass.getConstructor(String.class).newInstance(baseIri);
            edgeSerializers.add(loadedEdgeSerializer);
        }
        this.edgeSerializers = edgeSerializers;
    }

    private String getBaseIri() {
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = String.format("SELECT ?baseIri WHERE { ?baseIri <%s> <%s> }", RDF.TYPE, SemRes.BASE_IRI);

            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);
            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    return bindingSet.getValue("baseIri").stringValue();
                } else {
                    throw new RuntimeException("No base IRI in repository.");
                }
            }
        }
    }

    public void addSynset(Synset synset) {
        synset.setLastEditedTime(LocalDateTime.now());
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForSynset(synset).synsetToRdf(synset));
        }
    }

    public void addEdge(Edge edge) {
        if (hasEdge(edge.getId())) {
            throw new RuntimeException("Edge already exists");
        }

        edge.setLastEditedTime(LocalDateTime.now());
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForEdge(edge).edgeToRdf(edge));
        }
    }

    void editSynset(Synset edited, Synset original) {
        if (!edited.getId().equals(original.getId())) {
            throw new IllegalArgumentException("Original and edited synsets have different IDs.");
        }
        edited.setLastEditedTime(LocalDateTime.now());

        try (RepositoryConnection conn = repository.getConnection()) {
            conn.remove(getSerializerForSynset(original).synsetToRdf(original));
            conn.add(getSerializerForSynset(edited).synsetToRdf(edited));
        }
    }


    void editEdge(Edge edited, Edge original) {
        if (!edited.getId().equals(original.getId())) {
            throw new IllegalArgumentException("Original and edited edges have different IDs.");
        }

        edited.setLastEditedTime(LocalDateTime.now());
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.remove(getSerializerForEdge(original).edgeToRdf(original));
            conn.add(getSerializerForEdge(edited).edgeToRdf(edited));
        }
    }

    boolean hasSynset(String id) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = String.format("ASK  { ?synset <%s> ?type . ?type <%s> <%s> . ?synset <%s> %s }",
                    RDF.TYPE, RDFS.SUBCLASSOF, SemRes.SYNSET, SemRes.ID, conn.getValueFactory().createLiteral(id));
            BooleanQuery query = conn.prepareBooleanQuery(queryString);
            return query.evaluate();
        }
    }

    boolean hasEdge(String id) {
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = String.format("ASK  { ?edge <%s> ?type . ?type <%s> <%s> . ?edge <%s> %s }",
                    RDF.TYPE, RDFS.SUBCLASSOF, SemRes.EDGE, SemRes.ID, conn.getValueFactory().createLiteral(id));
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

    public Synset getSynset(String id) {
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?synset ?synsetType WHERE { ?synset <%s> %s . ?synset <%s> ?synsetType }",
                SemRes.ID, factory.createLiteral(id), RDF.TYPE);
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                if (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    IRI synsetIri = factory.createIRI(bindingSet.getValue("synset").stringValue());
                    String synsetType = bindingSet.getValue("synsetType").stringValue();

                    return getSynset(synsetIri, synsetType);
                } else {
                    return null;
                }
            }
        }
    }

    private Synset getSynset(IRI synsetIri, String type) {
        return getSerializerForSynset(type).rdfToSynset(synsetIri, repository);
    }

    private Edge getEdge(IRI edgeIri, String type) {
        return getSerializerForEdge(type).rdfToEdge(edgeIri, repository);
    }

    List<Synset> getSynsets() {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> }", RDF.TYPE, RDFS.SUBCLASSOF, SemRes.SYNSET);
        return getSynsets(queryString);
    }

    List<Synset> getSynsets(IRI typeIri) {
        String queryString = String.format("SELECT ?synset WHERE { ?synset <%s> <%s> }", RDF.TYPE, typeIri.stringValue());
        List<Synset> synsets = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();
                    IRI synsetIri = factory.createIRI(bindingSet.getValue("synset").stringValue());
                    synsets.add(getSynset(synsetIri, typeIri.stringValue()));
                }
            }
        }
        return synsets;
    }

    List<Synset> searchSynsets(String searchPhrase) {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> . ?synset <%s> ?label ." +
                        " filter contains(lcase(str(?label)), lcase(str(\"%s\"))) }",
                RDF.TYPE, RDFS.SUBCLASSOF, SemRes.SYNSET, RDFS.LABEL, searchPhrase);
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

    List<Edge> getOutgoingEdges(Synset originSynset) {
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edgeType ?edge" +
                " WHERE { ?originSynset <%s> %s . ?originSynset ?edge ?pointedSynset . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> }",
                SemRes.ID, factory.createLiteral(originSynset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SemRes.EDGE);

        return getEdges(queryString);
    }

    List<Edge> getPointingEdges(Synset pointedSynset) {
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edgeType ?edge" +
                        " WHERE { ?pointedSynset <%s> %s . ?originSynset ?edge ?pointedSynset . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> }",
                SemRes.ID, factory.createLiteral(pointedSynset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SemRes.EDGE);
        return getEdges(queryString);
    }


    public void addRelationType(RelationType relationType) {
        try (RepositoryConnection conn = repository.getConnection()) {
            // Check if relation type with this name exists
            ValueFactory factory = conn.getValueFactory();
            String queryString = String.format("ASK { ?relationType <%s> <%s> . ?relationType <%s> %s }",
                    RDF.TYPE, SemRes.RELATION_TYPE_CLASS, RDFS.LABEL, factory.createLiteral(relationType.getType()));
            if (conn.prepareBooleanQuery(queryString).evaluate()) {
                throw new RelationTypeAlreadyExists();
            }
            conn.add(relationTypeToRdf(relationType));
        }
    }


    public void removeRelationType(RelationType relationType) {
        if (relationTypeInUse(relationType)) {
            throw new RelationTypeInUseException();
        }
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.remove(relationTypeToRdf(relationType));
        }
    }

    boolean relationTypeInUse(RelationType relationType) {
        try (RepositoryConnection conn = repository.getConnection()) {
            ValueFactory factory = conn.getValueFactory();
            String queryString = String.format("ASK { ?relationType <%s> <%s> . ?relationType <%s> %s . ?edge <%s> ?relationType }",
                    RDF.TYPE, SemRes.RELATION_TYPE_CLASS, RDFS.LABEL, factory.createLiteral(relationType.getType()), SemRes.RELATION_TYPE_PROPERTY);
            return conn.prepareBooleanQuery(queryString).evaluate();
        }
    }

    List<RelationType> getRelationTypes() {
        List<RelationType> relationTypes = new ArrayList<>();
        try (RepositoryConnection conn = repository.getConnection()) {
            String queryString = String.format("SELECT ?relationTypeName ?relationTypeSource" +
                    " WHERE { ?relationType <%s> <%s> . ?relationType <%s> ?relationTypeName . ?relationType <%s> ?relationTypeSource }",
                    RDF.TYPE, SemRes.RELATION_TYPE_CLASS, RDFS.LABEL, SemRes.SOURCE);
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String name = bindingSet.getValue("relationTypeName").stringValue();
                    String source = bindingSet.getValue("relationTypeSource").stringValue();
                    relationTypes.add(new RelationType(name, source));
                }
            }
        }
        return relationTypes;
    }

    private List<Edge> getEdges(String queryString) {
        List<Edge> edges = new ArrayList<>();
        try (RepositoryConnection conn = repository.getConnection()) {
            ValueFactory factory = conn.getValueFactory();
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String edgeType = bindingSet.getValue("edgeType").stringValue();
                    IRI edgeIri = factory.createIRI(bindingSet.getValue("edge").stringValue());

                    edges.add(getEdge(edgeIri, edgeType));
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

    private Model relationTypeToRdf(RelationType relationType) {
        Model model = new LinkedHashModel();
        ValueFactory factory = repository.getValueFactory();

        IRI relationIri = factory.createIRI(baseIri + "relationTypes/" + relationType.getType());

        model.add(relationIri, RDF.TYPE, SemRes.RELATION_TYPE_CLASS);
        model.add(relationIri, RDFS.LABEL, factory.createLiteral(relationType.getType()));
        model.add(relationIri, SemRes.SOURCE, factory.createLiteral(relationType.getSource()));
        return model;
    }

    /**
     * Generate unique synset id.
     * @return Unique synset id.
     */
    String generateNewSynsetId() {
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

    String export(RDFFormat format) {
        Model model = getAllStatements();
        StringWriter buffer = new StringWriter();
        Rio.write(model, buffer, format);
        return buffer.toString();
    }

    Model getAllStatements() {
        try (RepositoryConnection conn = repository.getConnection()) {
            GraphQueryResult graphResult = conn.prepareGraphQuery("CONSTRUCT { ?s ?p ?o } WHERE {?s ?p ?o }").evaluate();
            return QueryResults.asModel(graphResult);
        }
    }
}
