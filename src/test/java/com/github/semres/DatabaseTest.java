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

public class DatabaseTest {

    @Test
    public void constructor() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);
    }

    @Test
    public void getSynsets() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, new ArrayList<>(), repo);

        UserSynset synset1 = new UserSynset("Foo1");
        synset1.setId("123");
        synset1.setDescription("Bar");
        Synset synset2 = new UserSynset("Foo2");
        synset2.setId("124");

        database.addSynset(synset1);
        database.addSynset(synset2);

        List<Synset> synsets = database.getSynsets();

        assertTrue(synsets.size() == 2);

        for (Synset synset : synsets) {
            if (synset.getId().equals("123")) {
                assertTrue(synset.getRepresentation().equals("Foo1"));
                assertTrue(synset.getDescription().equals("Bar"));
            } else if (synset.getId().equals("124")) {
                assertTrue(synset.getRepresentation().equals("Foo2"));
                assertTrue(synset.getDescription() == null);
            } else {
                throw new Exception();
            }
        }
    }
}