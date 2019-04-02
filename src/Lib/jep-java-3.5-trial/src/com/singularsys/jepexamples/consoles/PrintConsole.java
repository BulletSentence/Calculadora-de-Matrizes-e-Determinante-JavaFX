/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.consoles;

import com.singularsys.jep.JepException;
import com.singularsys.jep.parser.Node;

public class PrintConsole extends Console {
    private static final long serialVersionUID = 1L;
    /**
     * 
     */
    public PrintConsole() {
        super();
    }

    @Override
    public Object processEquation(Node node) throws JepException {
        jep.println(node);
        return super.processEquation(node);
    }

    /** Creates a new Console object and calls run() */
    public static void main(String args[]) {
        PrintConsole c = new PrintConsole();
        c.run(args);
    }


}
