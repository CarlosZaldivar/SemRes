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

    @Override
    public String toString() {
        return getType();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) {
            return false;
        }
        if (!RelationType.class.isAssignableFrom(obj.getClass())) {
            return false;
        }
        final RelationType other = (RelationType) obj;
        return this.type.equals(other.type) && this.source.equals(other.source);
    }
}
