package com.github.semres;

import org.apache.commons.io.FileUtils;
import org.eclipse.rdf4j.model.Model;
import org.eclipse.rdf4j.model.Statement;
import org.eclipse.rdf4j.model.vocabulary.RDF;
import org.eclipse.rdf4j.model.vocabulary.RDFS;
import org.eclipse.rdf4j.query.QueryResults;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.RepositoryConnection;
import org.eclipse.rdf4j.repository.RepositoryResult;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class SemResTest {

    @Test
    public void addRepository() throws Exception {
        try {
            Settings settings = new Settings("/tmp/semres-tests", new ArrayList<>());
            SemRes semRes = new SemRes(settings);
            assertTrue(semRes.getRepositoryIDs().size() == 1);

            semRes.addRepository("new-repo");
            assertTrue(semRes.getRepositoryIDs().size() == 2);

            Repository repository = semRes.getRepository("new-repo");
            RepositoryConnection conn = repository.getConnection();

            // Check if there are two classes 'synset' and 'synsetEdge' already in the repository.
            try (RepositoryResult<Statement> statements = conn.getStatements(null, RDF.TYPE, RDFS.CLASS)) {
                Model model = QueryResults.asModel(statements);
                assertTrue(model.size() == 2);
            }
        } finally {
            FileUtils.deleteDirectory(new File("/tmp/semres-tests"));
        }
    }
}