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
            id: 'remove',
            title: 'Remove',
            selector: 'node, edge',
            onClickFunction: removeElement
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
            selector: 'node[expanded="true"]',
            onClickFunction: startEdgeAddition
        },
        {
            id: 'expand',
            title: 'Expand',
            selector: 'node[expanded="false"]',
            onClickFunction: expand
        },
        {
            id: 'collapse',
            title: 'Collapse',
            selector: 'node[expanded="true"]',
            onClickFunction: function (event) { collapse(event.cyTarget, []); }
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
    cy.edgehandles('start', event.cyTarget.id());
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
    }
}

function addSynset(synset) {
    if (cy.elements('#' + synset.id).length === 1) {
        return;
    }
    synset.expanded = "false";
    cy.add({
        data: synset,
        style: [{
            selector: 'node'
        }]
    });
    cy.layout({name: 'grid'});
}

function addEdge(edge) {
    if (cy.elements('#' + edge.id).length === 1) {
        return;
    }

    var target = cy.getElementById(edge.targetSynset.id);
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

function search() {
    var searchPhrase = $("#searchField").val();
    if (searchPhrase) {
        javaApp.search(searchPhrase);
    }
}

function expand(event) {
    var synset = event.cyTarget.data();
    if (synset.expanded === "false") {
        javaApp.loadEdges(synset.id);
        synset.expanded = "true";
    }
}

function collapse(cyTarget, synsetsToCollapse) {
    var synset = cyTarget.data();
    if (synset.expanded === "true") {
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
                    if (target.data().expanded === "true") {
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
        synset.expanded = "false";
    }
}

function removeElement(event) {
    javaApp.removeElement(event.cyTarget.id());
    cy.remove(event.cyTarget);
}