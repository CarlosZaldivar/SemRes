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
                'label': 'label'
            },
            css: {
                'curve-style': 'bezier',
                'target-arrow-shape': 'triangle'
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
            onClickFunction: function (event) {
                event.cyTarget.remove();
            }
        },
        {
            id: 'hide',
            title: 'Hide',
            selector: '*',
            onClickFunction: function (event) {
                event.cyTarget.hide();
            },
            disabled: false
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
            selector: 'node',
            onClickFunction: startEdgeAddition
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
    complete: setEdgeDetails
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

function setEdgeDetails(sourceNode, targetNodes, addedEntities) {
    javaApp.openNewEdgeWindow(sourceNode.data().id, targetNodes.data().id);
}

function addSynset(synset) {
    cy.add({
        data: synset,
        style: [{
            selector: 'node',
            style: {
                shape: 'hexagon',
                // 'background-color': 'red',
                label: 'data(id)'
            }
        }]
    });
    cy.layout({name: 'grid'});
}

function search() {
    var searchPhrase = $("#searchField").val();
    if (searchPhrase) {
        javaApp.search(searchPhrase);
    }
}