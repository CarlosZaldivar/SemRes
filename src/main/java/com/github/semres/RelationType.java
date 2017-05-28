package com.github.semres;

public class RelationType {
    private final String type;
    private final String source;

    public RelationType(String type, String source) {
        this.type = type;
        this.source = source;
    }

    public String getType() {
        return type;
    }

    public String getSource() {
        return source;
    }
}
