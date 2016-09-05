package com.github.semres;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
    DatabaseTest.class,
    UserSynsetSerializerTest.class,
    SemResTest.class
})

public class TestSuite {
}
