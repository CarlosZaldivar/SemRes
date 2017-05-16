package com.github.semres;

import org.apache.commons.io.FileUtils;
import org.junit.Test;

import java.io.File;
import java.util.ArrayList;

import static org.junit.Assert.*;

public class DatabasesManagerTest {

    @Test
    public void addRepository() throws Exception {
        try {
            Settings settings = new Settings("/tmp/semres-tests", new ArrayList<>());
            DatabasesManager databasesManager = new DatabasesManager(settings);
            assertTrue(databasesManager.getRepositoryIDs().size() == 1);

            databasesManager.addRepository("new-repo");
            assertTrue(databasesManager.getRepositoryIDs().size() == 2);
        } finally {
            FileUtils.deleteDirectory(new File("/tmp/semres-tests"));
        }
    }
}