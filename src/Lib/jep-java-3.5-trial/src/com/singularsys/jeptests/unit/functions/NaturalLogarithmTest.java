/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import org.junit.Assert;
import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.functions.NaturalLogarithm;

public class NaturalLogarithmTest {

    /**
     * Test method for 'org.nfunk.jep.function.Logarithm.run(Stack)'
     * Tests the return value of log(NaN). This is a test for bug #1177557
     */
    @Test
    public void testNaturalLogarithm() {
        NaturalLogarithm logFunction = new NaturalLogarithm();
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(new Double(Double.NaN));
        try {
            logFunction.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        if (returnValue instanceof Double) {
            Assert.assertTrue(Double.isNaN(((Double)returnValue).doubleValue()));
        } else {
            Assert.fail();
        }
    }

}
