/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.diagnostics;

/**
 * Some common utility methods
 * @author Richard Morris
 * @since Jep 3.5
 */
public class Utils {

	/**
	 *  Generate a string giving an approximation to log using Horner expression of the form "x*(1/1-x*(1/2-x*(1/3-x*(1/4 ...))))" 
	 *
	 * @param varName
	 * @param nTerms
	 * @return a string containing the expression
	 */
	public static String hornerExpression(String varName,int nTerms) {
	    StringBuffer sb2 = new StringBuffer();
	    for(int i=1;i<nTerms ;++i)
	    {
	        sb2.append(varName);
	        sb2.append("*(");
	        sb2.append("1/"+i);
	        sb2.append("-");
	    }
	    sb2.append(varName);
	    sb2.append("/");
	    sb2.append(nTerms);
	    for(int i=1;i<nTerms ;++i)
	        sb2.append(")");
	
	    String expression2 = sb2.toString();
	    return expression2;
	}

	public static String recursiveHornerExpression(String varName,String lastVar,int nTerms) {
	    StringBuffer sb2 = new StringBuffer();
	    for(int i=1;i<nTerms ;++i)
	    {
	        sb2.append(varName);
	        sb2.append("*(");
	        sb2.append("1/"+i);
	        sb2.append("-");
	    }
	    sb2.append(lastVar);
	    sb2.append("/");
	    sb2.append(nTerms);
	    for(int i=1;i<nTerms ;++i)
	        sb2.append(")");
	
	    String expression2 = sb2.toString();
	    return expression2;
	}

	/**
	 * Generate string giving approximation to log 
	 */
	public static String lnExpression(String varName,int nDeriv) {
	    StringBuffer sb = new StringBuffer();
	    for(int i=1;i<=nDeriv ;++i)
	    {
	        if(i%2==0)
	            sb.append("-");
	        else if(i>1)
	            sb.append("+");
	        sb.append(varName+"^"+i+"/"+i);
	    }
	    String expression = sb.toString();
	    return expression;
	}

}
