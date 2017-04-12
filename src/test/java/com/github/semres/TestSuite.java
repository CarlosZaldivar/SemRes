package com.github.semres;

import org.junit.runner.RunWith;
import org.junit.runners.Suite;

@RunWith(Suite.class)
@Suite.SuiteClasses({
        BoardTest.class,
        DatabaseTest.class,
        SemResTest.class,
        com.github.semres.user.TestSuite.class,
        com.github.semres.babelnet.TestSuite.class
})

public class TestSuite {
}
