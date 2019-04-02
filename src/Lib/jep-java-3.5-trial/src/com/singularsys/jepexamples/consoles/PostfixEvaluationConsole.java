/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 2 Dec 2006 - Richard Morris
 */
package com.singularsys.jepexamples.consoles;

import com.singularsys.jep.JepException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.PostfixEvaluator;

public class PostfixEvaluationConsole extends Console {
    private static final long serialVersionUID = 1L;
    PostfixEvaluator pfe; 
    /**
     * @param args
     */
    public static void main(String[] args) {
        Console c = new PostfixEvaluationConsole();
        c.run(args);
    }

    @Override
    public void initialise() {
        super.initialise();
        pfe = new PostfixEvaluator();
    }

    @Override
    public Object processEquation(Node node) throws JepException {
        Object result=null;
        try {
            //Node processed = ((XJep) j).preprocess(node);
            result = pfe.evaluate(node);
            System.out.println(result);
        } catch (Exception e) {
            System.out.println(e.getMessage());
        }
        return result;
    }

}
