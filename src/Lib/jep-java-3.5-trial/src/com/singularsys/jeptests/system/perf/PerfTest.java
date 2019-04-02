/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system.perf;

/**
 * Base class for a single performance test.
 * @author Nathan Funk
 */
public class PerfTest {
    String name;
    String expression;
    int nIterations;
    /**
     * Sets up the name and number of iterations.
     * @param name
     * @param nIterations
     */
    public PerfTest(String name, String expression, int nIterations)
    {
        this.name = name;
        this.expression = expression;
        this.nIterations = nIterations;
    }

    /**
     * 
     */
    public void setup() throws Exception {}

    /**
     * Runs the test which is timed by the PerfRunner class
     * @throws Exception
     */
    public void run() throws Exception {}

    public String getName() { return name; }
    public String getExpression() {return expression; }
    public int getIterations() { return nIterations; }
}
