"use strict";

// Connect java object
alert("__CONNECT__BACKEND__javaApp");

var cy = cytoscape({
    container: document.getElementById('cy'),
    style: [
        {
            selector: 'node',
            style: {
                'label': 'data(representation)'
            }
        },
        {
            selector: 'edge',
            style: {
                'label': 'data(relationType)',
                'curve-style': 'bezier',
                'target-arrow-shape': 'triangle',
                'font-size': '10'
            }
        },
        {
            selector: 'node[class="com.github.semres.babelnet.BabelNetSynset"]',
            style: {
                'background-color': 'blue'
            }
        },
        {
            selector: 'node[class="com.github.semres.user.UserSynset"]',
            style: {
                'background-color': 'green'
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

cy.contextMenus({
    menuItems: [
        {
            id: 'removeNode',
            title: 'Remove',
            selector: 'node',
            onClickFunction: removeNode
        },
        {
            id: 'removeEdge',
            title: 'Remove',
            selector: 'edge',
            onClickFunction: removeEdge
        },
        {
            id: 'add-node',
            title: 'Add node',
            coreAsWell: true,
            onClickFunction: startSynsetAddition
        },
        {
            id: 'add-edge',
            title: 'Add edge',
            selector: 'node[?expanded]',
            onClickFunction: startEdgeAddition
        },
        {
            id: 'expand',
            title: 'Expand',
            selector: 'node[!expanded]',
            onClickFunction: expand
        },
        {
            id: 'collapse',
            title: 'Collapse',
            selector: 'node[?expanded]',
            onClickFunction: function (event) { collapse(event.cyTarget, []); }
        },
        {
            id: 'loadEdgesFromBabelNet',
            title: 'Load edges from BabelNet',
            selector: 'node[!downloadedWithEdges][class="com.github.semres.babelnet.BabelNetSynset"]',
            onClickFunction: downloadEdgesFromBabelNet
        },
        {
            id: 'select-all-nodes',
            title: 'Select all nodes',
            coreAsWell: true,
            onClickFunction: function() {
                cy.elements().unselect();
                cy.nodes().select();
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
    cy.edgehandles('start', escapeColon(event.cyTarget.id()));
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

function addSynset(synset) {
    if (cy.elements('#' + escapeColon(synset.id)).length === 1) {
        return;
    }

    cy.add({
        data: synset,
        style: [{
            selector: 'node'
        }]
    });
    cy.layout({name: 'grid'});
}

function addEdge(edge) {
    if (cy.elements('#' + escapeColon(edge.id)).length === 1) {
        return;
    }

    var origin = cy.getElementById(escapeColon(edge.sourceSynset.id));
    if (!origin.data()) {
        addSynset(edge.sourceSynset);
    }

    var target = cy.getElementById(escapeColon(edge.targetSynset.id));
    if (!target.data()) {
        addSynset(edge.targetSynset);
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

function expand(event) {
    var synset = event.cyTarget.data();
    if (synset.expanded === false) {
        javaApp.loadEdges(synset.id);
        synset.expanded = true;
    }
}

function collapse(cyTarget, synsetsToCollapse) {
    var synset = cyTarget.data();
    if (synset.expanded === true) {
        cyTarget.connectedEdges().forEach(function (edge) {
            if (edge.source().data().id === cyTarget.data().id) {
                var target = edge.target();

                if (synsetsToCollapse.indexOf(target.data().id) > -1) {
                    return;
                }

                var targetConnectedEdges = target.connectedEdges();
                var shouldBeRemoved = true;

                for (var i = 0; i < targetConnectedEdges.length; ++i) {
                    var edgeSource = targetConnectedEdges[i].source();
                    if (edgeSource.data().id !== target.data().id && edgeSource.data().id !== synset.id) {
                        shouldBeRemoved = false;
                        break;
                    }
                }

                if (shouldBeRemoved) {
                    if (target.data().expanded === true) {
                        var newSynsetsToCollapse = synsetsToCollapse.slice();
                        newSynsetsToCollapse.push(synset.id);
                        collapse(target, newSynsetsToCollapse);
                    }
                    cy.remove(target);
                } else {
                    cy.remove(edge);
                }
            }
        });
        synset.expanded = false;
    }
}

function downloadEdgesFromBabelNet(event) {
    javaApp.downloadEdgesFromBabelNet(event.cyTarget.id());
}

function removeNode(event) {
    javaApp.removeNode(event.cyTarget.id());
    cy.remove(event.cyTarget);
}

function removeEdge(event) {
    javaApp.removeEdge(event.cyTarget.id());
    cy.remove(event.cyTarget);
}

function escapeColon(string) {
    return string.replaceAll(':', '\\:');
}