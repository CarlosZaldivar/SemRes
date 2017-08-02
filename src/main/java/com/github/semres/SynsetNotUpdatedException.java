package com.github.semres;

public class SynsetNotUpdatedException extends RuntimeException {
    public SynsetNotUpdatedException() {
        super("No changes between updated and original synset detected.");
    }
}
