package com.github.semres;

import com.github.semres.babelnet.BabelNetEdgeSerializer;
import com.github.semres.babelnet.BabelNetSynsetSerializer;
import com.github.semres.user.UserEdgeSerializer;
import com.github.semres.user.UserSynset;
import com.github.semres.user.UserSynsetSerializer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

import static org.junit.Assert.*;

public class BoardTest {

    @Test
    public void addSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo");
        board.addSynset(synset);
        board.save();

        // Check if id of length 26 has been generated.
        assertTrue(synset.getId().length() == 26);
        assertTrue(database.searchSynsets("Foo").size() == 1);
    }

    @Test
    public void removeSynset() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class<? extends SynsetSerializer>> synsetSerializerClasses = new ArrayList<>();
        List<Class<? extends EdgeSerializer>> edgeSerializerClasses = new ArrayList<>();
        synsetSerializerClasses.add(UserSynsetSerializer.class);
        synsetSerializerClasses.add(BabelNetSynsetSerializer.class);
        edgeSerializerClasses.add(UserEdgeSerializer.class);
        edgeSerializerClasses.add(BabelNetEdgeSerializer.class);

        Database database = new Database(synsetSerializerClasses, edgeSerializerClasses, repo);
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo");
        synset.setId("123");
        board.addSynset(synset);
        board.save();
        board.removeElement("123");
        board.save();
        assertTrue(database.searchSynsets("Foo").size() == 0);
    }


}