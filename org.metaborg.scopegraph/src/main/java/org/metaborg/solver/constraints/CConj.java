package org.metaborg.solver.constraints;

import com.google.common.collect.ImmutableList;

public class CConj implements IConstraint {

    private static final long serialVersionUID = -7392869760223304136L;

    public final ImmutableList<? extends IConstraint> constraints;

    public CConj(IConstraint... constraints) {
        this(ImmutableList.copyOf(constraints));
    }

    public CConj(ImmutableList<? extends IConstraint> constraints) {
        this.constraints = constraints;
    }

    @Override public <T> T accept(IConstraintVisitor<T> visitor) {
        return visitor.visit(this);
    }

    @Override public String toString() {
        final StringBuilder sb = new StringBuilder();
        sb.append("(");
        boolean tail = false;
        for (IConstraint constraint : constraints) {
            if (tail) {
                sb.append(", ");
            } else {
                tail = true;
            }
            sb.append(constraint);
        }
        sb.append(")");
        return sb.toString();
    }

}
