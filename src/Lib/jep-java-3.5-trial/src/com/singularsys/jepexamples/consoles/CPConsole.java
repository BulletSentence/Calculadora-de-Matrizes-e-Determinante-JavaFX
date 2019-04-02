/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.consoles;

import com.singularsys.jep.Jep;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;

public class CPConsole extends Console {
    private static final long serialVersionUID = 300L;

    @Override
    public void initialise() {
        jep = new Jep(new StandardConfigurableParser());
        jep.getOperatorTable().getUMinus().addAltSymbol("\u2212");
        jep.getOperatorTable().getSubtract().addAltSymbol("\u2212");
        jep.getOperatorTable().getMultiply().addAltSymbol("\u00d7");
        jep.getOperatorTable().getDivide().addAltSymbol("\u00f7");
        jep.reinitializeComponents();
    }

    /** Creates a new Console object and calls run() */
    public static void main(String args[]) {
        Console c = new CPConsole();
        c.run(args);
    }

}
