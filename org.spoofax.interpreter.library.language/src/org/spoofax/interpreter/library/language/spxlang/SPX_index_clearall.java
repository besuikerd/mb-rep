package org.spoofax.interpreter.library.language.spxlang;

import org.spoofax.interpreter.core.IContext;
import org.spoofax.interpreter.core.InterpreterException;
import org.spoofax.interpreter.core.Tools;
import org.spoofax.interpreter.library.AbstractPrimitive;
import org.spoofax.interpreter.library.ssl.SSLLibrary;
import org.spoofax.interpreter.stratego.Strategy;
import org.spoofax.interpreter.terms.IStrategoString;
import org.spoofax.interpreter.terms.IStrategoTerm;


//TODO : Generalize state management primitive. Abstract them to one primitive only.  

/**
 * @author Md. Adil Akhter
 * Created On : Aug 25, 2011
 */
public class SPX_index_clearall extends AbstractPrimitive {

	private static String NAME = "SPX_index_clearall";
	private static int PROJECT_NAME_INDEX = 0;
	private final static int NO_ARGS = 1;
	
	private final SpxSemanticIndex index;

	public SPX_index_clearall(SpxSemanticIndex index) {
		super(NAME, 0, NO_ARGS);
		this.index = index;
	}
	
	
	
	/* (non-Javadoc)
	 * @see org.spoofax.interpreter.library.AbstractPrimitive#call(org.spoofax.interpreter.core.IContext, org.spoofax.interpreter.stratego.Strategy[], org.spoofax.interpreter.terms.IStrategoTerm[])
	 */
	@Override
	public boolean call(IContext env, Strategy[] svars, IStrategoTerm[] tvars){
	
		boolean retValue = false;	
		if ( (tvars.length == NO_ARGS) && Tools.isTermString(tvars[PROJECT_NAME_INDEX]))
		{	
			try 
			{
				retValue  = index.clearall((IStrategoString)tvars[PROJECT_NAME_INDEX]);
			}
			catch(Exception ex)
			{
				SSLLibrary.instance(env).getIOAgent().printError("["+NAME+"] Invokation failed. Error : "+ ex.getMessage());
			}	
				
		}
		else
			SSLLibrary.instance(env).getIOAgent().printError("["+NAME+"] Invokation failed . Error :  Mismatch in provided arguments. Variables provided : "+ tvars);
		
		return retValue;
	}

}
