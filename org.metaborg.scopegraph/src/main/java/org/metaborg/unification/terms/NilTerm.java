package org.metaborg.unification.terms;

public final class NilTerm implements IListTerm {

    private static final long serialVersionUID = 1000527612219597398L;

    public NilTerm() {
    }

    @Override public <T> T accept(ITermVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public int hashCode() {
        return 1;
    }

    @Override public boolean equals(Object obj) {
        if (this == obj)
            return true;
        if (getClass() != obj.getClass())
            return false;
        return true;
    }

    @Override public String toString() {
        return "[]";
    }

}