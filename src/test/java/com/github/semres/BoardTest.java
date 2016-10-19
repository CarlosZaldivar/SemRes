package com.github.semres;

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
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, repo);
        Board board = new Board(database);

        Synset synset = new UserSynset("Foo");
        board.addSynset(synset);

        // Check if id of length 26 has been generated.
        assertTrue(synset.getId().length() == 26);
    }
}