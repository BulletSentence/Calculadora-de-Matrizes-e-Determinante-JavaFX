/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system.perf.tests;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.reals.RealEvaluator;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jeptests.system.perf.PerfTest;

/**
 * Evaluates an expression <code>nIterations</code> times.
 * @author nathan
 */
public class EvalPerfTest extends PerfTest {

    Jep j;
    public boolean useRealEval;

    public EvalPerfTest(String name, String expression, int nIterations, boolean useRealEval)
    {
        super(name, expression, nIterations);
        this.useRealEval = useRealEval;
    }


    /**
     * Set up the parser and parse the expression
     */
    @Override
	public void setup() throws Exception {
        j = new Jep();

        if (useRealEval) {
            j.setComponent(new RealEvaluator());
        } else {
            j.setComponent(new StandardEvaluator());
        }

        try {
            // try parsing the the expression the given number of iterations
            j.parse(getExpression());
        } catch (ParseException e) {
            // something went wrong during the parse call
            throw new Exception(e);
        }
    }

    /**
     * Evaluate the expression nIterations times.
     * This is the method that is timed.
     */
    @Override
	public void run() throws Exception {
        try {
            for (int i=0; i<getIterations(); i++) {
                j.evaluate();
            }
        } catch (EvaluationException e) {
            throw new Exception(e);
        }
    }
}
