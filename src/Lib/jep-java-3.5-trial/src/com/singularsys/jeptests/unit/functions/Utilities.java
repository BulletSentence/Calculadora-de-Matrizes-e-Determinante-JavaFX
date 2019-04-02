/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import java.math.BigDecimal;
import java.math.MathContext;

import org.junit.Assert;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.functions.PostfixMathCommand;

public class Utilities {
    /**
     * Utility method for testing unary methods
     * @param pfmc
     * @param arg
     * @param expected
     */
    public static void testUnary(PostfixMathCommand pfmc, double arg, double expected)
    {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(new Double(arg));
        pfmc.setCurNumberOfParameters(1);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        if (returnValue instanceof Double) {
            Assert.assertEquals(expected, ((Double)returnValue).doubleValue(),0.0);
        } else {
            Assert.fail();
        }
    }

    public static void testBinary(PostfixMathCommand pfmc, double l, double r, double expected) {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(new Double(l));
        stack.push(new Double(r));
        pfmc.setCurNumberOfParameters(2);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        if (returnValue instanceof Double) {
            Assert.assertEquals(expected, ((Double)returnValue).doubleValue(),0.0);
        } else {
            Assert.fail();
        }
	}


	public static void testBDBinary(PostfixMathCommand pfmc, double l, double r, String string) {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(new BigDecimal(l,MathContext.DECIMAL64));
        stack.push(new BigDecimal(r,MathContext.DECIMAL64));
        pfmc.setCurNumberOfParameters(2);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

            Assert.assertEquals(new BigDecimal(string,MathContext.DECIMAL64), returnValue);
	}

	public static void testBinary(PostfixMathCommand pfmc, Object l, Object r, Object expected) {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(r);
        stack.push(l);
        pfmc.setCurNumberOfParameters(2);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        Assert.assertEquals(expected, returnValue);
	}

	public static void testUnary(PostfixMathCommand pfmc, Object l, Object expected) {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(l);
        pfmc.setCurNumberOfParameters(1);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        Assert.assertEquals(expected, returnValue);
	}

	public static void testBDUnary(PostfixMathCommand pfmc, double l, String string) {
        java.util.Stack<Object> stack = new java.util.Stack<>();
        stack.push(new BigDecimal(l));
        pfmc.setCurNumberOfParameters(1);
        try {
            pfmc.run(stack);
        } catch (EvaluationException e) {
            Assert.fail();
        }
        Object returnValue = stack.pop();

        Assert.assertEquals(new BigDecimal(string), returnValue);
	}

}
