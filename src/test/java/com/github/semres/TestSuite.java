package com.github.semres;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        DatabaseTest.class,
        SemResTest.class,
        com.github.semres.user.TestSuite.class
})

public class TestSuite {
}
