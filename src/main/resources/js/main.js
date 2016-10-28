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
        }
    ]
});

function startSynsetAddition(e) {
    try {
        javaApp.openNewSynsetWindow();
    }
    catch(err) {
        console.log(err);
    }
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