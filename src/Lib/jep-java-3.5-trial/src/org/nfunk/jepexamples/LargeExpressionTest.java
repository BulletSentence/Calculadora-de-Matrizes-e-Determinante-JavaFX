/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package org.nfunk.jepexamples;

import java.util.*;

import org.nfunk.jep.JEP;

import com.singularsys.jep.JepException;

/**
 * This example tests how the evaluation time is influenced by the size of the
 * expression and symbol table.
 */
public class LargeExpressionTest {
    public static void main(String args[]) {
        int nEvals = 500;
        int nVars = 1000;
        Date start, finish;
        String str = "";

        JEP myParser = new JEP();

        // Test small symbol table
        try {
            for (int i=0; i<10; i++) {
                myParser.addVariable("v"+i, 0);
                str += "+" + "v" + i;
            }
        } catch (JepException e) {
            // ignore since this shouldn't happen
        }
        myParser.parseExpression(str);
        System.out.print("Evaluating with small symbol table... ");
        start = new Date();
        for (int i=0; i<nEvals; i++) {
            myParser.getValue();
        }
        finish = new Date();
        System.out.println("done.");
        System.out.println("Time: " +
                (finish.getTime() - start.getTime()));

        // Test large symbol table
        str = "";
        try {
            for (int i=0; i<nVars; i++) {
                myParser.addVariable("v" + i, 0);
                str += "+" + "v" + i;
            }
        } catch (JepException e) {
            // ignore since this shouldn't happen
        }

        myParser.parseExpression(str);
        System.out.print("Evaluating with large symbol table... ");
        start = new Date();
        for (int i=0; i<nEvals; i++) {
            myParser.getValue();
        }
        finish = new Date();
        System.out.println("done.");
        System.out.println("Time: " +
                (finish.getTime() - start.getTime()));
    }	
}
