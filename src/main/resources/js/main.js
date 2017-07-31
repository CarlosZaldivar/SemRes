"use strict";

var startTime = moment();

var cy = cytoscape({
    container: document.getElementById('cy'),
    style: [
        {
            selector: 'node',
            style: {
                'label': 'data(representation)',
                'background-color': nodeBackgroundColor
            }
        },
        {
            selector: 'edge',
            style: {
                'label': 'data(relationType)',
                'curve-style': 'bezier',
                'target-arrow-shape': 'triangle',
                'font-size': '10',
                'line-color': edgeBackgroundColor,
                'target-arrow-color': edgeBackgroundColor
            }
        },
        {
            selector: '.edgehandles-hover',
            css: {
                'background-color': 'red'
            }
        },

        {
            selector: '.edgehandles-source',
            css: {
                'border-width': 2,
                'border-color': 'red'
            }
        },

        {
            selector: '.edgehandles-target',
            css: {
                'border-width': 2,
                'border-color': 'red'
            }
        },
        {
            selector: '.edgehandles-preview, .edgehandles-ghost-edge',
            css: {
                'line-color': 'red',
                'target-arrow-color': 'red',
                'source-arrow-color': 'red'
            }
        }
    ]
});

var menus = cy.contextMenus({
    menuItems: [
        {
            id: 'removeSynset',
            content: 'Remove',
            selector: 'node',
            onClickFunction: removeSynset
        },
        {
            id: 'removeEdge',
            content: 'Remove',
            selector: 'edge',
            onClickFunction: removeEdge
        },
        {
            id: 'add-node',
            content: 'Add node',
            coreAsWell: true,
            onClickFunction: startSynsetAddition
        },
        {
            id: 'add-edge',
            content: 'Add edge',
            selector: 'node[?expanded]',
            onClickFunction: startEdgeAddition
        },
        {
            id: 'sendExpandRequest',
            content: 'Expand',
            selector: 'node[!expanded]',
            onClickFunction: sendExpandRequest
        },
        {
            id: 'collapse',
            content: 'Collapse',
            selector: 'node[?expanded]',
            onClickFunction: function (event) { collapse(event.target, []); }
        },
        {
            id: 'loadEdgesFromBabelNet',
            content: 'Load edges from BabelNet',
            selector: 'node[!downloadedWithEdges][class="com.github.semres.babelnet.BabelNetSynset"]',
            onClickFunction: downloadEdgesFromBabelNet
        },
        {
            id: 'checkForUpdates',
            content: 'Check for updates',
            selector: 'node[class="com.github.semres.babelnet.BabelNetSynset"]',
            onClickFunction: checkForUpdates
        },
        {
            id: 'synsetDetails',
            content: 'Details',
            selector: 'node',
            onClickFunction: openSynsetDetailsWindow
        },
        {
            id: 'edgeDetails',
            content: 'Details',
            selector: 'edge',
            onClickFunction: openEdgeDetailsWindow
        },
        {
            id: 'select-all-nodes',
            content: 'Select all nodes',
            coreAsWell: true,
            onClickFunction: function() {
                cy.elements().unselect();
                cy.nodes().select();
            }
        },
        {
            id: 'fit',
            content: 'Fit',
            coreAsWell: true,
            onClickFunction: function () {
                cy.fit();
            }
        }
    ]
});

cy.edgehandles({
    complete: setEdgeDetails,
    edgeType: function(sourceNode, targetNode) {return sourceNode.edgesTo(targetNode).empty() ? 'flat' : null; }
});
cy.edgehandles('disable');

function startEdgeAddition(event) {
    cy.edgehandles('start', escapeColon(event.target.id()));
}

function startSynsetAddition() {
    try {
        javaApp.openNewSynsetWindow();
    }
    catch(err) {
        console.log(err);
    }
}

function setEdgeDetails(sourceNode, targetNode, addedEntities) {
    if (addedEntities.length === 1) {
        javaApp.openNewEdgeWindow(sourceNode.data().id, targetNode.data().id);
        cy.remove(addedEntities);
    }
}

function openSynsetDetailsWindow(event) {
    javaApp.openSynsetDetailsWindow(event.target.data().id);
}

function openEdgeDetailsWindow(event) {
    javaApp.openEdgeDetailsWindow(event.target.data().id);
}

function addSynset(synset, pointedSynsets, edges) {
    addSynsetToCytoscape(synset);
    pointedSynsets.forEach(function (synset) {
       addSynsetToCytoscape(synset);
    });
    edges.forEach(function (edge) {
        addEdgeToCytoscape(edge);
    });
    cy.layout({name: 'cola', fit: false}).run();
}

function addSynsetToCytoscape(synset) {
    if (elementExists(synset)) {
        return;
    }
    cy.add({
        data: synset,
        style: [{
            selector: 'node'
        }]
    });
}

function elementExists(element) {
    return cy.elements('#' + escapeColon(element.id)).length === 1;
}

function addEdge(edge) {
    addEdgeToCytoscape(edge);
}

function addEdgeToCytoscape(edge) {
    if (cy.elements('#' + escapeColon(edge.id)).length === 1) {
        return;
    }

    edge.target = edge.targetSynset.id;
    edge.source = edge.sourceSynset.id;

    delete edge.sourceSynset;
    delete edge.targetSynset;

    cy.add({
        group: "edges",
        data: edge
    });
}

function sendExpandRequest(event) {
    var synset = event.target.data();
    if (synset.expanded === false) {
        javaApp.loadEdges(synset.id);
        synset.expanded = true;
    }
}

function collapse(target, synsetsToCollapse) {
    var synset = target.data();
    if (synset.expanded === true) {
        target.connectedEdges().forEach(function (edge) {
            if (edge.source().data().id === synset.id) {
                var edgeTarget = edge.target();

                if (synsetsToCollapse.indexOf(edgeTarget.data().id) > -1) {
                    return;
                }

                var targetConnectedEdges = edgeTarget.connectedEdges();
                var shouldBeRemoved = true;

                for (var i = 0; i < targetConnectedEdges.length; ++i) {
                    var edgeSource = targetConnectedEdges[i].source();
                    if (edgeSource.data().id !== edgeTarget.data().id && edgeSource.data().id !== synset.id) {
                        shouldBeRemoved = false;
                        break;
                    }
                }

                if (shouldBeRemoved) {
                    if (edgeTarget.data().expanded === true) {
                        var newSynsetsToCollapse = synsetsToCollapse.slice();
                        newSynsetsToCollapse.push(synset.id);
                        collapse(edgeTarget, newSynsetsToCollapse);
                    }
                    cy.remove(edgeTarget);
                } else {
                    cy.remove(edge);
                }
            }
        });
        synset.expanded = false;
    }
}

function downloadEdgesFromBabelNet(event) {
    javaApp.downloadEdgesFromBabelNet(event.target.id());
}

function checkForUpdates(event) {
    javaApp.checkForUpdates(event.target.id());
}

function removeSynset(event) {
    javaApp.removeSynset(event.target.id());
    cy.remove(event.target);
}

function removeEdge(event) {
    javaApp.removeEdge(event.target.id());
    cy.remove(event.target);
}

function clear() {
    var synsetIds = cy.nodes().map(function (synset) {
        return synset.data().id;
    });
    cy.elements().remove();
    return synsetIds;
}

function expandSynset(synsetId, pointedSynsets, edges) {
    var originSynset = cy.getElementById(synsetId).data();
    originSynset.expanded = true;

    pointedSynsets.forEach(function (synset) {
        if (!elementExists(synset) && synset.expanded === true) {
            synset.expanded = false;
        }
        addSynsetToCytoscape(synset);
    });
    edges.forEach(function (edge) {
        addEdgeToCytoscape(edge);
    });
    cy.layout({name: 'cola', fit: false}).run();
}

function addBabelNetEdges(synsetId, pointedSynsets, edges) {
    var originSynset = cy.getElementById(synsetId).data();
    originSynset.expanded = true;
    originSynset.downloadedWithEdges = true;

    pointedSynsets.forEach(function (synset) {
        addSynsetToCytoscape(synset);
    });
    edges.forEach(function (edge) {
        addEdgeToCytoscape(edge);
    });
    cy.layout({name: 'cola', fit: false}).run();
}

function updateSynset(editedSynset) {
    var oldSynset = cy.getElementById(editedSynset.id);
    oldSynset.data('representation', editedSynset.representation);
    oldSynset.data('description', editedSynset.description);
}

function updateEdge(editedEdge) {
    var oldEdge = cy.getElementById(editedEdge.id);
    oldEdge.data('relationType', editedEdge.relationType);
    oldEdge.data('description', editedEdge.description);
    oldEdge.data('weight', editedEdge.weight);
}

function disableUpdates() {
    menus.disableMenuItem("checkForUpdates");
}

function updateStartTime() {
    startTime = moment();
}

function nodeBackgroundColor(node) {
    var synset = node.data();
    if (moment(synset.lastEditedTime, 'YYYY-MM-DDTHH:mm:ss.SSS').isAfter(startTime)) {
        return 'yellow';
    }
    if (synset.class === "com.github.semres.babelnet.BabelNetSynset") {
        return 'blue';
    } else {
        return 'green'
    }
}

function edgeBackgroundColor(node) {
    var edge = node.data();
    if (moment(edge.lastEditedTime, 'YYYY-MM-DDTHH:mm:ss.SSS').isAfter(startTime)) {
        return 'yellow';
    }
    if (edge.class === "com.github.semres.babelnet.BabelNetEdge") {
        return 'blue';
    } else {
        return 'green'
    }
}

function escapeColon(string) {
    return string.replaceAll(':', '\\:');
}