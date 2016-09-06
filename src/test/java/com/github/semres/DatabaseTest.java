package com.github.semres;

import com.github.semres.user.UserSynsetSerializer;
import org.eclipse.rdf4j.repository.Repository;
import org.eclipse.rdf4j.repository.sail.SailRepository;
import org.eclipse.rdf4j.sail.memory.MemoryStore;
import org.junit.Test;

import java.util.ArrayList;
import java.util.List;

public class DatabaseTest {

    @Test
    public void constructor() throws Exception {
        Repository repo = new SailRepository(new MemoryStore());
        List<Class> serializerClasses = new ArrayList<>();
        serializerClasses.add(UserSynsetSerializer.class);

        Database database = new Database(serializerClasses, repo);
    }
}