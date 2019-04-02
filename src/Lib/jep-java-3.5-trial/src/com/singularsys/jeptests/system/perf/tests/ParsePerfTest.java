/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system.perf.tests;

import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.parser.StandardParser;
import com.singularsys.jeptests.system.perf.PerfTest;

/**
 * Parses an expression <code>nIterations</code> times.
 * @author nathan
 *
 */
public class ParsePerfTest extends PerfTest {

    Jep j;
    public boolean useConfigParser;

    public ParsePerfTest(String name, String expression, int nIterations,
            boolean useConfigParser)
    {
        super(name, expression, nIterations);
        this.useConfigParser = useConfigParser;
    }


    /**
     * Set up the parser
     */
    @Override
	public void setup() throws Exception {
        j = new Jep();
        // choose parser
        if (useConfigParser)
        {
            j.setComponent(new StandardConfigurableParser());
        }
        else
        {
            j.setComponent(new StandardParser());
        }
    }

    /**
     * Parse the expression nIterations times. This is the method that is timed.
     */
    @Override
	public void run() throws Exception {
        try {
            // try parsing the the expression the given number of iterations
            for (int i=0; i < getIterations(); i++)
            {
                j.parse(getExpression());
            }
        } catch (ParseException e) {
            // something went wrong during the parse call
            throw new Exception(e);
        }
    }


}
