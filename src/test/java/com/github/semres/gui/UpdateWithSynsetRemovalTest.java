package com.github.semres.gui;

import javafx.scene.input.KeyCode;
import javafx.scene.input.MouseButton;
import javafx.stage.Stage;
import org.junit.After;
import org.junit.Before;
import org.junit.Test;
import org.testfx.api.FxToolkit;
import org.testfx.framework.junit.ApplicationTest;

import java.util.concurrent.TimeoutException;

public class UpdateWithSynsetRemovalTest extends ApplicationTest {
    @Before
    public void setUpClass() throws Exception {
        ApplicationTest.launch(UpdateWithSynsetRemovalTestApplication.class);
    }

    @Override
    public void start(Stage stage) throws Exception {
        stage.show();
    }

    @After
    public void afterEachTest() throws TimeoutException {
        FxToolkit.hideStage();
        release(new KeyCode[]{});
        release(new MouseButton[]{});
    }

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