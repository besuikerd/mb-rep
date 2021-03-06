package org.spoofax.interpreter.library.index.primitives;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.library.index.IIndex;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoTerm;

public class LANG_index_stop_collection extends IndexPrimitive {
    private static String NAME = "LANG_index_stop_collection";

    public LANG_index_stop_collection() {
        super(NAME, 0, 1);
    }

    @Override public boolean call(IIndex index, IContext env, Strategy[] svars, IStrategoTerm[] tvars) {
        final IStrategoTerm source = tvars[0];
        env.setCurrent(index.stopCollection(source));
        return true;
    }
}
