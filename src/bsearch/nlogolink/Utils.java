package bsearch.nlogolink;

import org.nlogo.agent.BooleanConstraint;
import org.nlogo.agent.ChooserConstraint;
import org.nlogo.agent.SliderConstraint;
import org.nlogo.api.ValueConstraint;
import org.nlogo.headless.HeadlessWorkspace;


public strictfp class Utils {
	public static final long MIN_EXACT_NETLOGO_INT = -9007199254740992L;
	public static final long MAX_EXACT_NETLOGO_INT = 9007199254740992L;
	
	private static HeadlessWorkspace emptyWorkspace; 

	public static Object evaluateNetLogoReporterInEmptyWorkspace(String reporterString ) throws NetLogoLinkException
	{
		if (emptyWorkspace == null)
		{
			emptyWorkspace = HeadlessWorkspace.newInstance();			
		}
		try {
			return emptyWorkspace.report( reporterString );
		} catch (Exception ex)
		{
			throw new NetLogoLinkException(ex.getMessage());
		}
	}		

	
	public static HeadlessWorkspace createWorkspace()
	{
		HeadlessWorkspace workspace = HeadlessWorkspace.newInstance();
		return workspace;
	}
	/** If you don't call this method, there will still be a NetLogo workspace thread running in the background, which might
	 * prevent your application from shutting down naturally when main() is exited, for instance.  If you call
	 * System.exit(), then it might not matter whether you call this method first or not.  Perhaps it's good form
	 * to always call it before your application quits. */
	public static void fullyShutDownNetLogoLink() throws InterruptedException
	{
		emptyWorkspace.dispose();
	}
	
	public static String getDefaultConstraintsText(String modelFileName) throws NetLogoLinkException
	{
		HeadlessWorkspace workspace = Utils.createWorkspace();

// TODO: THIS try/catch stuff is commented out for now, because of a Scala-based regression in NetLogo 5.x,
// which neglected to put in "throws" annotations for these various exceptions, even though they actually 
// can occur.  Once NetLogo fixes the issue on their end, we can put this error handling back in place.
		
//		try {
//		workspace.open(modelFileName);
//		} catch (IOException e) {
//			throw new NetLogoLinkException("I/O Error trying to open model file '" + modelFileName + "'\n " + e.toString(), e);
//		} catch (CompilerException e) {
//			throw new NetLogoLinkException("Model file '" + modelFileName + "' didn't compile: " + e.toString(), e);
//		} catch (LogoException e) {
//			throw new NetLogoLinkException("Unexpected error loading model: " + e.toString(), e);
//		}
		
		try {
			workspace.open(modelFileName);
		} catch (Exception e)
		{
			throw new NetLogoLinkException("A problem occurred trying to open or compile model file '" + modelFileName + "'\n " + e.toString(), e);
		}
		StringBuilder sb = new StringBuilder();
		int numVars = workspace.world().observer().getVariableCount();
	    for (int i = 0; i < numVars; i++)
	    {
	    	ValueConstraint con = workspace.world().observer().variableConstraint(i); 
	    	if ( con != null)
	    	{
	    		String name = workspace.world.observerOwnsNameAt(i);
	    		sb.append("[\"");
	    		sb.append(name.toLowerCase());
	    		sb.append("\" ");
    			StringBuilder sb2 = new StringBuilder();
	    		if (con instanceof SliderConstraint)
	    		{
	    			SliderConstraint scon = (SliderConstraint) con;
    				double min = scon.minimum();
    				double incr = scon.increment();
    				double max = scon.maximum();
    				String strIncr = org.nlogo.api.Dump.logoObject(incr, true, false);
    				
    				// if it's a non-integer slider with more than 100 factor levels, let's suggest continuous "C"
    				if (min != StrictMath.floor(min) && incr != StrictMath.floor(incr) &&
    						StrictMath.abs((max - min) / incr) > 100)
    				{
    					strIncr = "\"C\"";
    				}
    				sb2.append("["); 
    				sb2.append(org.nlogo.api.Dump.logoObject(min, true, false));
    				sb2.append(" "); 
    				sb2.append(strIncr);
    				sb2.append(" "); 
    				sb2.append(org.nlogo.api.Dump.logoObject(max, true, false));
    				sb2.append("]"); 
	    		}
	    		else if (con instanceof ChooserConstraint)
	    		{
	    			ChooserConstraint ccon = (ChooserConstraint) con;
	    			for (Object obj: ccon.acceptedValues())
	    			{
		    			sb2.append(org.nlogo.api.Dump.logoObject(obj, true, false));
	    				sb2.append(" "); 
	    			}
	    			sb2.setLength(sb2.length() - 1);
	    		}
	    		else if (con instanceof BooleanConstraint)
	    		{
	    			sb2.append(org.nlogo.api.Dump.logoObject(Boolean.TRUE, true, false));
    				sb2.append(" "); 
	    			sb2.append(org.nlogo.api.Dump.logoObject(Boolean.FALSE, true, false));
	    		}
	    		else
	    		{
	    			sb2.append(org.nlogo.api.Dump.logoObject(con.defaultValue(), true, false));
	    		}
	    		sb.append(sb2);
	    		sb.append("]\n");
	    	}
	    }
	    try {
			workspace.dispose();
		} catch (InterruptedException e) {
		}
	    return sb.toString();
	}			
}
