package com.github.semres;

import org.apache.log4j.BasicConfigurator;
import org.apache.log4j.Logger;
import org.apache.log4j.spi.LoggerFactory;

import java.io.IOException;

public class Main {

    final static Logger logger = Logger.getLogger(Main.class);

    public static void main(String[] args) {
        try {
            Settings.load();
        } catch (IOException | NullPointerException e) {
            logger.error("Could not load settings from conf.yaml", e);
            return;
        }
    }
}
