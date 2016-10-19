package com.github.semres;

import org.apache.commons.io.FileUtils;
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
        } finally {
            FileUtils.deleteDirectory(new File("/tmp/semres-tests"));
        }
    }
}