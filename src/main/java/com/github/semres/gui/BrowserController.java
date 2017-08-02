package com.github.semres.gui;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.SerializationFeature;
import com.github.semres.Edge;
import com.github.semres.Synset;
import com.github.semres.babelnet.BabelNetManager;
import com.github.semres.user.UserEdge;
import com.github.semres.user.UserSynset;
import com.teamdev.jxbrowser.chromium.*;
import com.teamdev.jxbrowser.chromium.events.FinishLoadingEvent;
import com.teamdev.jxbrowser.chromium.events.LoadAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextAdapter;
import com.teamdev.jxbrowser.chromium.events.ScriptContextEvent;
import com.teamdev.jxbrowser.chromium.javafx.BrowserView;
import javafx.application.Platform;
import javafx.stage.Stage;
import org.apache.commons.beanutils.PropertyUtils;
import org.apache.commons.lang.StringUtils;
import org.apache.log4j.Logger;

import java.io.IOException;
import java.lang.reflect.InvocationTargetException;
import java.util.*;
import java.util.stream.Collectors;

class BrowserController extends Controller {
    static Logger log = Logger.getRootLogger();

    private MainController mainController;
    private BrowserView boardView;
    private Browser browser;

    BrowserController(MainController mainController) {
        this.mainController = mainController;
        browser = new Browser();
        boardView = new BrowserView(browser);

        // Add javaApp object to javascript.
        browser.addScriptContextListener(new ScriptContextAdapter() {
            @Override
            public void onScriptContextCreated(ScriptContextEvent event) {
                Browser browser = event.getBrowser();
                JSValue window = browser.executeJavaScriptAndReturnValue("window");
                window.asObject().setProperty("javaApp", new JavaApp());
            }
        });

        // Enable loading resources from inside jar file.
        BrowserContext browserContext = browser.getContext();
        ProtocolService protocolService = browserContext.getProtocolService();
        protocolService.setProtocolHandler("jar", new JarProtocolHandler());
    }

    BrowserView getBoardView() {
        return boardView;
    }

    void loadPage() {
        browser.loadURL(getClass().getResource("/html/board.html").toExternalForm());

        // Disable options concerning BabelNet if there's no api key.
        if (StringUtils.isEmpty(BabelNetManager.getApiKey())) {
            browser.addLoadListener(new LoadAdapter() {
                @Override
                public void onFinishLoadingFrame(FinishLoadingEvent event) {
                    if (event.isMainFrame()) {
                        browser.executeJavaScript("disableBabelNetOptions()");
                    }
                }
            });
        }

        String remoteDebuggingURL = browser.getRemoteDebuggingURL();
        log.info("Remote debugging URL: " + remoteDebuggingURL);
    }

    void updateStartTime() {
        browser.executeJavaScriptAndReturnValue("updateStartTime()");
    }

    // Block input on BrowserView and return to default handlers when window is closed.
    void blockBrowserView(Stage stage) {
        boardView.setMouseEventsHandler((e) -> true);
        boardView.setScrollEventsHandler((e) -> true);
        boardView.setGestureEventsHandler((e) -> true);
        boardView.setKeyEventsHandler((e) -> true);
        stage.setOnHidden((e) -> {
                    boardView.setMouseEventsHandler(null);
                    boardView.setScrollEventsHandler(null);
                    boardView.setGestureEventsHandler(null);
                    boardView.setKeyEventsHandler(null);
                }
        );
    }

    void addSynsetToView(Synset synset) {
        Collection<Edge> edges = synset.getOutgoingEdges().values();
        List<Synset> pointedSynsets = new ArrayList<>();
        for (Edge edge : edges) {
            pointedSynsets.add(mainController.getBoard().getSynset(edge.getPointedSynsetId()));
        }
        browser.executeJavaScript(String.format("addSynset(%s, %s, %s)", synsetToJson(synset), synsetsToJson(pointedSynsets), edgesToJson(edges)));
    }

    void addEdgeToView(Edge edge) { browser.executeJavaScript("addEdge(" + edgeToJson(edge) + ");"); }

    void updateSynset(UserSynset editedSynset) {
        browser.executeJavaScript("updateSynset(" + synsetToJson(editedSynset) + ");");
    }

    void updateEdge(UserEdge editedEdge) {
        browser.executeJavaScript("updateEdge(" + edgeToJson(editedEdge) + ");");
    }

    void redrawNodes() {
        JSArray synsetIds =  browser.executeJavaScriptAndReturnValue("clear()").asArray();
        for (int i = 0; i < synsetIds.length(); ++i) {
            String id = synsetIds.get(i).getStringValue();
            Synset synset = mainController.getBoard().getSynset(id);
            if (synset != null) {
                addSynsetToView(synset);
            }
        }
    }

    void dispose() {
        browser.dispose();
    }

    private Map<String, Object> synsetToMap(Synset synset) {
        Map<String, Object> synsetMap;
        try {
            synsetMap = PropertyUtils.describe(synset);
        } catch (IllegalAccessException | InvocationTargetException | NoSuchMethodException e) {
            throw new RuntimeException(e);
        }

        synsetMap.remove("outgoingEdges");
        return synsetMap;
    }

    private String synsetToJson(Synset synset) {
        ObjectMapper mapper = getMapper();
        String jsonSynset = null;
        try {
            jsonSynset = mapper.writeValueAsString(synsetToMap(synset));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonSynset;
    }

    private String synsetsToJson(Collection<? extends Synset> synsets) {
        List<Map<String, Object>> synsetMaps = synsets.stream().map(this::synsetToMap).collect(Collectors.toList());
        ObjectMapper mapper = getMapper();
        String jsonSynsets = null;
        try {
            jsonSynsets = mapper.writeValueAsString(synsetMaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonSynsets;
    }

    private Map<String, Object> edgeToMap(Edge edge) {
        Map<String, Object> edgeMap = new HashMap<>();
        edgeMap.put("id", edge.getId());
        edgeMap.put("description", edge.getDescription());
        edgeMap.put("weight", edge.getWeight());
        edgeMap.put("relationType", edge.getRelationType().toString().toLowerCase());
        edgeMap.put("targetSynset", synsetToMap(mainController.getBoard().getSynset(edge.getPointedSynsetId())));
        edgeMap.put("sourceSynset", synsetToMap(mainController.getBoard().getSynset(edge.getOriginSynsetId())));
        edgeMap.put("lastEditedTime", edge.getLastEditedTime());
        edgeMap.put("class", edge.getClass().getCanonicalName());
        return edgeMap;
    }

    private String edgeToJson(Edge edge) {
        String jsonEdge = null;
        ObjectMapper mapper = getMapper();
        try {
            jsonEdge = mapper.writeValueAsString(edgeToMap(edge));
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdge;
    }

    private String edgesToJson(Collection<? extends Edge> edges) {
        List<Map<String, Object>> edgeMaps = edges.stream().map(this::edgeToMap).collect(Collectors.toList());
        ObjectMapper mapper = getMapper();
        String jsonEdges = null;
        try {
            jsonEdges = mapper.writeValueAsString(edgeMaps);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return jsonEdges;
    }

    private ObjectMapper getMapper() {
        ObjectMapper mapper = new ObjectMapper();

        // Configure mapper to properly parse LocalDateTime
        mapper.findAndRegisterModules();
        mapper.configure(SerializationFeature.WRITE_DATES_AS_TIMESTAMPS, false);
        return mapper;
    }

    public class JavaApp {
        public void openNewSynsetWindow() {
            Platform.runLater(() -> {
                try {
                    mainController.openNewSynsetWindow();
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openNewEdgeWindow(String originSynsetId, String destinationSynsetId) {
            Platform.runLater(() -> {
                try {
                    mainController.openNewEdgeWindow(originSynsetId, destinationSynsetId);
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openSynsetDetailsWindow(String synsetId) {
            Platform.runLater(() -> {
                try {
                    mainController.openSynsetDetailsWindow(synsetId);

                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void openEdgeDetailsWindow(String edgeId) {
            Platform.runLater(() -> {
                try {
                    mainController.openEdgeDetailsWindow(edgeId);

                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }

        public void removeSynset(String synsetId) {
            mainController.getBoard().removeSynset(synsetId);
        }

        public void removeEdge(String edgeId) {
            mainController.getBoard().removeEdge(edgeId);
        }

        public void loadEdges(String synsetId) {
            Synset synset = mainController.getBoard().getSynset(synsetId);
            Collection<Edge> edges;
            if (synset.hasDatabaseEdgesLoaded()) {
                edges = synset.getOutgoingEdges().values();
            } else {
                mainController.getBoard().loadEdges(synsetId);
                edges = synset.getOutgoingEdges().values();
            }
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : edges) {
                pointedSynsets.add(mainController.getBoard().getSynset(edge.getPointedSynsetId()));
            }

            browser.executeJavaScript(String.format("expandSynset(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }

        public void downloadEdgesFromBabelNet(String synsetId) {
            Collection<Edge> edges;
            try {
                edges = mainController.getBoard().downloadBabelNetEdges(synsetId);
            } catch (IOException e) {
                Utils.showError(e.getMessage());
                return;
            }
            List<Synset> pointedSynsets = new ArrayList<>();
            for (Edge edge : edges) {
                pointedSynsets.add(mainController.getBoard().getSynset(edge.getPointedSynsetId()));
            }

            browser.executeJavaScript(String.format("addBabelNetEdges(\"%s\", %s, %s);", synsetId, synsetsToJson(pointedSynsets), edgesToJson(edges)));
        }

        public void checkForUpdates(String synsetId) {
            Platform.runLater(() -> {
                try {
                    mainController.openUpdatesWindow(synsetId);
                } catch (IOException e) {
                    Utils.showError(e.getMessage());
                }
            });
        }
    }
}
