package com.github.semres;

import org.apache.log4j.Logger;

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
