/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.bigdecimal.functions;

import java.math.BigDecimal;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.bigdecimal.functions.BigDecRelational;

public class BigDecRelationalTest {
    BigDecRelational eq, ge, gt, le, lt, ne;

    @Before
    public void setUp() throws Exception {
        eq = new BigDecRelational(BigDecRelational.EQ);
        eq.setAllowStrings(true);
        ge = new BigDecRelational(BigDecRelational.GE);
        ge.setAllowStrings(true);
        gt = new BigDecRelational(BigDecRelational.GT);
        gt.setAllowStrings(true);
        le = new BigDecRelational(BigDecRelational.LE);
        le.setAllowStrings(true);
        lt = new BigDecRelational(BigDecRelational.LT);
        lt.setAllowStrings(true);
        ne = new BigDecRelational(BigDecRelational.NE);
        ne.setAllowStrings(true);
    }

    @Test
    public void testDefaults() {
       eq = new BigDecRelational(BigDecRelational.EQ);
       Assert.assertFalse(eq.getAllowStrings());
    }
    
    @Test
    public void testNumbers() {
        genericTest(BigDecimal.ZERO, BigDecimal.ONE);
    }
    
    @Test
    public void testBools() {
        genericTest(Boolean.FALSE, Boolean.TRUE);
    }
    
    @Test
    public void testStrings() {
        genericTest("a", "b");
    }
    
    /**
     * Generic test method for comparing two different values with all operators
     * @param lowVal The low value
     * @param highVal The high value
     */
    private void genericTest(Object lowVal, Object highVal) {

        test(lowVal, eq, lowVal, true);
        test(lowVal, ne, lowVal, false);
        test(lowVal, lt, lowVal, false);
        test(lowVal, gt, lowVal, false);
        test(lowVal, le, lowVal, true);
        test(lowVal, ge, lowVal, true);

        test(lowVal, eq, highVal, false);
        test(lowVal, ne, highVal, true);
        test(lowVal, lt, highVal, true);
        test(lowVal, gt, highVal, false);
        test(lowVal, le, highVal, true);
        test(lowVal, ge, highVal, false);

        test(highVal, eq, highVal, true);
        test(highVal, ne, highVal, false);
        test(highVal, lt, highVal, false);
        test(highVal, gt, highVal, false);
        test(highVal, le, highVal, true);		
        test(highVal, ge, highVal, true);

        test(highVal, eq, lowVal, false);
        test(highVal, ne, lowVal, true);
        test(highVal, lt, lowVal, false);
        test(highVal, gt, lowVal, true);
        test(highVal, le, lowVal, false);		
        test(highVal, ge, lowVal, true);
    }
    
    @Test
    public void testStringNumber()
    {
        // expect exceptions for all operators acting on a string compared
        // to a number
        testExceptionCases("a", BigDecimal.ONE);
    }

    @Test
    public void testBoolNumber()
    {
        // expect exceptions for all operators acting on a string compared
        // to a number
        testExceptionCases(Boolean.FALSE, BigDecimal.ONE);
    }
    
    @Test
    public void testBoolString()
    {
        // expect exceptions for all operators acting on a string compared
        // to a number
        testExceptionCases(Boolean.FALSE, "a");
    }
    
    /**
     * Generic method for comparing two value where all operators are expected
     * to throw an exception.
     * @param v1 Value 1
     * @param v2 Value 2
     */
    private void testExceptionCases(Object v1, Object v2) {
        testException(v1, eq, v2);
        testException(v1, ne, v2);
        testException(v1, gt, v2);
        testException(v1, ge, v2);
        testException(v1, lt, v2);
        testException(v1, le, v2);
    }
    
    /**
     * Performs test on the class with a set operator type. Checks whether
     * application of the operator to <code>v1</code> and <code>v2</code>
     * results in <code>result</code>.
     */
    private void test(Object v1, BigDecRelational pfmc, Object v2, boolean result) {
        Stack<Object> st = new Stack<>();
        // print current params
        System.out.println(v1+", "+v2);
        
        // add the values
        st.add(v1);
        st.add(v2);

        try {
            // run the operator
            pfmc.run(st);
            // if result is more than one element, fail
            if (st.size() != 1) Assert.fail("More than one return value on stack");
            // get return value
            Object value = st.pop();
            // get 
            if (!(value instanceof Boolean)) Assert.fail("Return value is not Boolean");
            // check if value is correct
            if (((Boolean)value).booleanValue() != result) Assert.fail("Evaluated as "+value+" but should be " + result);
        } catch (EvaluationException e) {
            Assert.fail("Unexpected exception occurred: " + e.getMessage());
        }

    }
    
    /**
     * Performs test on the class with a set operator type. Expects an 
     * exception to occur during evaluation.
     */
    private void testException(Object v1, BigDecRelational pfmc, Object v2) {
        Stack<Object> st = new Stack<>();
        // print current params
        System.out.println(v1+", "+v2);
        
        // add the values
        st.add(v1);
        st.add(v2);

        try {
            // run the operator
            pfmc.run(st);
            Assert.fail("Expected exception was not thrown");
        } catch (EvaluationException e) {
        }
    }
}
