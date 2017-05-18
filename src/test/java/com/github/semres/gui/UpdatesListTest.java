package com.github.semres.gui;

import org.junit.Test;

public class UpdatesListTest extends TestFXBase {

    @Test
    public void updateTest() throws Exception {
        clickOn("#fileMenu");
        clickOn("#databasesMenuItem");
        doubleClickOn("Test database");
        clickOn("#babelNetMenu");
        clickOn("#updateMenuItem");

        // Sleep to give some additional time to the thread that loads updates.
        sleep(1000);
        clickOn("#applyButton");
    }
}
