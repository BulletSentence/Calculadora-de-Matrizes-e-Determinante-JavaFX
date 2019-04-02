/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.consoles;

import com.singularsys.jep.Jep;
import com.singularsys.jep.configurableparser.ConfigurableParser;

/**
 * A console which illustrates a different syntax for vectors using mathematical (1,2,3) notation.
the tokenizer

 */
public class VectorConsole extends Console {
    private static final long serialVersionUID = 300L;

    public VectorConsole() { /* do nothing */ }


    static class VectorParser extends ConfigurableParser {
        private static final long serialVersionUID = 320L;

        VectorParser() {
            this.addHashComments();
            this.addSlashComments();
            this.addDoubleQuoteStrings();
            this.addWhiteSpace();
            this.addExponentNumbers();
            this.addIdentifiers();
            this.addOperatorTokenMatcher();
            this.addSymbols(new String[]{"(",")","[","]",","}); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
            this.setImplicitMultiplicationSymbols(new String[]{"(","["}); //$NON-NLS-1$ //$NON-NLS-2$
            this.addSemiColonTerminator();
            this.addWhiteSpaceCommentFilter();
            // The following matches vector with (a,b,c) or brackets (a+b) 
            this.addListOrBracketMatcher("(",")",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.addFunctionMatcher("(",")",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.addListMatcher("[","]",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
            this.addArrayAccessMatcher("[","]"); //$NON-NLS-1$ //$NON-NLS-2$

        }
    }
    @Override
    public void initialise() {
        jep = new Jep();
        jep.setComponent(new VectorParser());
    }

    @Override
    public void printIntroText() {
	println("Jep with round brackets for vector (1,2,3)+(4,5,6)");
        printStdHelp();
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        VectorConsole vc = new VectorConsole();
        vc.run(args);

    }

}
