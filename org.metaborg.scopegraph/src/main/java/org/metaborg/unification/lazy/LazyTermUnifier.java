package org.metaborg.unification.lazy;

import java.util.Set;

import javax.annotation.Nullable;

import org.metaborg.fastutil.persistent.Object2ObjectOpenHashPMap;
import org.metaborg.fastutil.persistent.Object2ObjectPMap;
import org.metaborg.unification.ITerm;
import org.metaborg.unification.ITermUnifier;
import org.metaborg.unification.terms.ITermOp;
import org.metaborg.unification.terms.ITermVar;

public final class LazyTermUnifier implements ITermUnifier {

    final Object2ObjectPMap<ITermVar,ITerm> varReps;
    final Object2ObjectPMap<ITermOp,ITerm> opReps;

    public LazyTermUnifier() {
        this.varReps = new Object2ObjectOpenHashPMap<>();
        this.opReps = new Object2ObjectOpenHashPMap<>();
    }

    LazyTermUnifier(Object2ObjectPMap<ITermVar,ITerm> varReps, Object2ObjectPMap<ITermOp,ITerm> opReps) {
        this.varReps = varReps;
        this.opReps = opReps;
    }

    @Override public @Nullable LazyUnifyResult unify(ITerm term1, ITerm term2) {
        final LazyFindResult result1 = find(term1);
        final LazyFindResult result2 = result1.unifier().find(term2);
        return result1.rep().apply(new LazyUnifyFunction(result2.unifier(), result2.rep()));
    }

    @Override public LazyFindResult find(ITerm term) {
        return term.apply(new LazyFindFunction(this));
    }

    @Override public ITermUnifier findAll() {
        ITermUnifier localUnifier = this;
        for (ITermVar var : varReps.keySet()) {
            localUnifier = localUnifier.find(var).unifier();
        }
        return localUnifier;
    }

    @Override public Set<ITermVar> variables() {
        return varReps.keySet();
    }

}