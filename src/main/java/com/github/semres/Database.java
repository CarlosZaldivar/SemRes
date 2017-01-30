package com.github.semres;

import org.eclipse.rdf4j.model.IRI;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.ValueFactory;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.*;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;

import java.lang.reflect.InvocationTargetException;
import java.util.ArrayList;
import java.util.List;
import java.util.NoSuchElementException;

public class Database {
    private List<SynsetSerializer> synsetSerializers;
    private List<EdgeSerializer> edgeSerializers;
    private Repository repository;
    private String baseIri = "http://example.org/";

    public Database(List<Class> synsetSerializerClasses, List<Class> edgeSerializerClasses, Repository repository) throws NoSuchMethodException, IllegalAccessException, InvocationTargetException, InstantiationException {
        List<SynsetSerializer> synsetSerializers = new ArrayList<>();
        for (Class serializerClass: synsetSerializerClasses) {
            SynsetSerializer loadedSynsetSerializer = (SynsetSerializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            synsetSerializers.add(loadedSynsetSerializer);
        }
        this.synsetSerializers = synsetSerializers;

        List<EdgeSerializer> edgeSerializers = new ArrayList<>();
        for (Class serializerClass: edgeSerializerClasses) {
            EdgeSerializer loadedEdgeSerializer = (EdgeSerializer) serializerClass.getConstructor(Repository.class, String.class).newInstance(repository, baseIri);
            edgeSerializers.add(loadedEdgeSerializer);
        }
        this.edgeSerializers = edgeSerializers;

        this.repository = repository;
        this.repository.initialize();
    }

    public void addSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForSynset(synset).synsetToRdf(synset));
        }
    }

    public void addEdge(Edge edge) {
        try (RepositoryConnection conn = repository.getConnection()) {
            conn.add(getSerializerForEdge(edge).edgeToRdf(edge));
        }
    }

    public void removeSynset(Synset synset) {
        try (RepositoryConnection conn = repository.getConnection()) {

            ValueFactory factory = conn.getValueFactory();
            String queryString;

            // Remove edges
            List<IRI> edgesIris = getEdgesIris(synset);
            for (IRI edgeIri : edgesIris) {
                queryString = String.format("CONSTRUCT { ?originSynset <%s> ?pointedSynset }" +
                        "WHERE { ?originSynset <%s> ?pointedSynset }", edgeIri, edgeIri);
                GraphQueryResult result = conn.prepareGraphQuery(queryString).evaluate();
                Statement edgeStatement = result.next();

                queryString = String.format("CONSTRUCT { <%s> ?p ?o } WHERE { <%s> ?p ?o }", edgeIri, edgeIri);
                result = conn.prepareGraphQuery(queryString).evaluate();

                while (result.hasNext()) {
                    Statement statement = result.next();
                    conn.remove(statement);
                }
                conn.remove(edgeStatement);
            }

            // Remove synset
            queryString = String.format("CONSTRUCT { ?s ?p ?o } WHERE { ?s <%s> %s . ?s ?p ?o }", SR.ID, factory.createLiteral(synset.getId()));
            GraphQueryResult result = conn.prepareGraphQuery(queryString).evaluate();
            while (result.hasNext()) {
                Statement statement = result.next();
                conn.remove(statement);
            }
        }
    }

    public void removeEdge(Edge edge) {
        try (RepositoryConnection conn = repository.getConnection()) {
            ValueFactory factory = conn.getValueFactory();
            String queryString = String.format("CONSTRUCT { ?originSynset ?edge ?pointedSynset }" +
                            "WHERE { ?originSynset <%s> %s . ?pointedSynset <%s> %s . ?originSynset ?edge ?pointedSynset }",
                    SR.ID, factory.createLiteral(edge.getOriginSynset().getId()), SR.ID, factory.createLiteral(edge.getPointedSynset().getId()));
            GraphQueryResult result = conn.prepareGraphQuery(queryString).evaluate();
            if (result.hasNext()) {
                Statement edgeStatement = result.next();
                IRI edgeIri = edgeStatement.getPredicate();
                conn.remove(edgeStatement);

                queryString = String.format("CONSTRUCT { <%s> ?p ?o } WHERE { <%s> ?p ?o }", edgeIri, edgeIri);
                result = conn.prepareGraphQuery(queryString).evaluate();

                while (result.hasNext()) {
                    Statement statement = result.next();
                    conn.remove(statement);
                }
            }
        }
    }

    private Synset getSynset(IRI synsetIri, String type) {
        return getSerializerForSynset(type).rdfToSynset(synsetIri);
    }

    private Edge getEdge(IRI edgeIri, String type, Synset originSynset, Synset pointedSynset) {
        return getSerializerForEdge(type).rdfToEdge(edgeIri, pointedSynset, originSynset);
    }

    public List<Synset> getSynsets() {
        String queryString = String.format("SELECT ?type ?synset WHERE { ?synset <%s> ?type . ?type <%s> <%s> }", RDF.TYPE, RDFS.SUBCLASSOF, SR.SYNSET);
        return getSynsets(queryString);
    }

    public List<Synset> searchSynsets(String searchPhrase) {
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

    public List<Edge> getOutgoingEdges(Synset originSynset) {
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

    public List<Edge> getPointingEdges(Synset pointedSynset) {
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

    /**
     * Get IRI's of all outgoingEdges that are connected with given synset, both starting and ending on it.
     * @param synset
     * @return List of all synset's outgoingEdges IRI's
     */
    private List<IRI> getEdgesIris(Synset synset) {
        List<IRI> iris = new ArrayList<>();
        ValueFactory factory = repository.getValueFactory();
        String queryString = String.format("SELECT ?edge" +
                        " WHERE { ?synset <%s> %s . { ?s ?edge ?synset } UNION { ?synset ?edge ?o } . ?edge <%s> ?edgeType . ?edgeType <%s> <%s> }",
                SR.ID, factory.createLiteral(synset.getId()), RDF.TYPE, RDFS.SUBCLASSOF, SR.EDGE);
        try (RepositoryConnection conn = repository.getConnection()) {
            TupleQuery tupleQuery = conn.prepareTupleQuery(QueryLanguage.SPARQL, queryString);

            try (TupleQueryResult result = tupleQuery.evaluate()) {
                while (result.hasNext()) {
                    BindingSet bindingSet = result.next();

                    String edge = bindingSet.getValue("edge").stringValue();
                    iris.add(factory.createIRI(edge));
                }
            }
        }
        return iris;
    }

    private SynsetSerializer getSerializerForSynset(Synset synset) {
        SynsetSerializer serializer;
        try {
            serializer = synsetSerializers
                    .stream()
                    .filter(x -> x.getSynsetClass().equals(synset.getClass().getCanonicalName()))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
        return serializer;
    }

    private SynsetSerializer getSerializerForSynset(String type) {
        SynsetSerializer serializer;
        try {
            serializer = synsetSerializers
                    .stream()
                    .filter(x -> x.getSynsetClassIri().stringValue().equals(type))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        }
        return serializer;
    }

    private EdgeSerializer getSerializerForEdge(Edge edge) {
        EdgeSerializer serializer;
        try {
            serializer = edgeSerializers
                    .stream()
                    .filter(x -> x.getEdgeClass().equals(edge.getClass().getCanonicalName()))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException(e);
        }
        return serializer;
    }

    private EdgeSerializer getSerializerForEdge(String type) {
        EdgeSerializer serializer;
        try {
            serializer = edgeSerializers
                    .stream()
                    .filter(x -> x.getEdgeClassIri().stringValue().equals(type))
                    .findFirst().get();
        } catch (NoSuchElementException e) {
            throw new IllegalArgumentException();
        }
        return serializer;
    }
}
