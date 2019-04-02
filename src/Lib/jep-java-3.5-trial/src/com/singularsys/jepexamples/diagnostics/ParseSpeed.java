/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 15 Jan 2007 - Richard Morris
 */
package com.singularsys.jepexamples.diagnostics;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * Tests the speed of parsing.
 * Uses a Horner expression  <code>x*(1/1-x*(1/2-x*(1/3-x*(1/4 ...))))</code>
 * The number of equations and number of terms can be set using
 * <code>java SpeedTest nEqn nTerms</code>
 * @author Richard Morris
 */
public class ParseSpeed {
    String[] equations;
    /**
     * Builds a set of equations
     * @param neqn number of equations
     * @param nvar number of variables
     */
    void buildEquations(int nEqns,int nVars) {
        equations = new String[nEqns];
        for(int i=0;i<nEqns;++i)
//            equations[i] = buildEquation(i,nVars);
//          equations[i] = SpeedTest.lnExpression(varName(i,0), nVars);
        equations[i] = Utils.hornerExpression(varName(i,0), nVars);
    }

    String buildEquation(int eqNo,int nVars) {
        StringBuffer sb = new StringBuffer();
        sb.append(varName(eqNo,0));
        for(int i=1;i<nVars;++i) {
            sb.append('+');
            sb.append(varName(eqNo,i));
        }
        return sb.toString();
    }

    String varName(int eqNo,int varNo) {
        return "x" + eqNo + "_" + varNo;

    }
    public void go(int nEqns,int nVars) {
        System.out.println("Parsing "+nEqns+" equations with "+nVars+" terms");
        long t1 = System.currentTimeMillis(); 
        buildEquations(nEqns,nVars);
        Node[] nodes = new Node[equations.length];
        long t2 = System.currentTimeMillis();
        System.out.println("Build equations\t"+(t2-t1));

        Jep jep = new Jep();
        Jep jep2 = new Jep(new StandardConfigurableParser());
        long t3 = System.currentTimeMillis(); 
        System.out.println("Initialise Jep\t"+(t3-t2));

        try {
            for(int i=0;i<nEqns;++i)
                nodes[i] = jep.parse(equations[i]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long t4 = System.currentTimeMillis(); 
        System.out.println("Parse, standard parser\t"+(t4-t3));
        
        try {
            for(int i=0;i<nEqns;++i)
                nodes[i] = jep2.parse(equations[i]);
        } catch (ParseException e) {
            e.printStackTrace();
        }
        long t5 = System.currentTimeMillis(); 
        System.out.println("Parse, configurable parser\t"+(t5-t4));
        
        try {
            int numnodes=1;
                TreeAnalyzer ta = new TreeAnalyzer(nodes[0]);
                numnodes = ta.getNumNodes();


                System.out.printf("Avg per node Standard parser %.1f Config parser %.1f%n",
                    ((double)t4-t3)/numnodes,((double)t5-t4)/numnodes);

                System.out.println("\nSample expression");
                jep.getPrintVisitor().setMaxLen(80);
                jep.println(nodes[0]);
                System.out.println(ta.summary());

        } catch (JepException e1) {
                e1.printStackTrace();
        }
    }
    /**
     * @param args
     */
    public static void main(String[] args) {
        ParseSpeed ps = new ParseSpeed();
        int nEqn = 1000;
        int nVar = 50;
        if(args.length>=1)
            nEqn = Integer.valueOf(args[0]); 
        if(args.length>=2)
            nVar = Integer.valueOf(args[1]); 
        ps.go(nEqn,nVar);
    }

}
