package org.spoofax.interpreter.library.language.spxlang;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoAppl;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;
import org.spoofax.interpreter.terms.IStrategoTuple;

public class SPX_index_resolve_symbol extends AbstractPrimitive {
	private static String NAME = "SPX_index_resolve_symbol";
	private static int PROJECT_NAME_INDEX = 0;
	private static int SEARCH_CRITERIA_INDEX = 1;

	private final static int NO_ARGS = 2;

	private final SpxSemanticIndex index;

	public SPX_index_resolve_symbol(SpxSemanticIndex index) {
		super(NAME, 0, NO_ARGS);
		this.index = index;
	}

	/* Retrieve Spoofaxlang ModuleDefinition with Module ID 
	 * specified in {@code tvars}.    
	 * 
	 * {@code tvars} contains name of the project and typed qualified ModuleID  
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext, org.spoofax.interpreter.stratego.Strategy[], org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars){
		boolean successStatement = false;
	
		if ( (tvars.length == NO_ARGS)  && Tools.isTermString(tvars[PROJECT_NAME_INDEX]) && Tools.isTermAppl(tvars[SEARCH_CRITERIA_INDEX])) 
		{
			IStrategoString projectName    = (IStrategoString)tvars[PROJECT_NAME_INDEX];
			IStrategoTuple searchCriteria = (IStrategoTuple)tvars[SEARCH_CRITERIA_INDEX];
		
			try {
				IStrategoTerm t = index.resolveSymbol(projectName, searchCriteria);
				env.setCurrent(t);
				successStatement = true;
			} 
			catch(Exception ex)
			{
				SSLLibrary.instance(env).getIOAgent().printError("["+NAME+"]  Invocation failed . "+ ex.getClass().getSimpleName() +" | error message: " + ex.getMessage());
			}
		}
		else
			SSLLibrary.instance(env).getIOAgent().printError("["+NAME+"]  Invocation failed . Error :  Mismatch in provided arguments. Variables provided : "+ tvars);

		return successStatement;
	}
}