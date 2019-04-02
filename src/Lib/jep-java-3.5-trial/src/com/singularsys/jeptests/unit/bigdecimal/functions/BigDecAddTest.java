/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.bigdecimal.functions;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Stack;

import org.junit.Before;
import org.junit.Test;
import org.junit.Assert;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.bigdecimal.functions.BigDecAdd;

public class BigDecAddTest {
    BigDecAdd pfmc;

    @Before
    public void setUp() throws Exception {
        pfmc = new BigDecAdd(MathContext.DECIMAL32);
        pfmc.setAllowStrings(true);
    }
    
    @Test
    public void testMathContext() {
        pfmc.setMathContext(null);
        Assert.assertTrue(pfmc.getMathContext() == null);
        pfmc.setMathContext(MathContext.DECIMAL32);
        Assert.assertTrue(pfmc.getMathContext() == MathContext.DECIMAL32);
    }

    @Test
    public void testDefaults() {
       BigDecAdd add = new BigDecAdd(MathContext.DECIMAL32);
       Assert.assertFalse(add.getAllowStrings());
    }
    
    @Test
    public void testNullMathContext() {
        //store original mc
        MathContext mc = pfmc.getMathContext();
        pfmc.setMathContext(null);
        test(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        test(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE);
        test(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
        // set mc back again
        pfmc.setMathContext(mc);
    }
    
    @Test
    public void testNumbers() {
        test(BigDecimal.ZERO, BigDecimal.ZERO, BigDecimal.ZERO);
        test(BigDecimal.ZERO, BigDecimal.ONE, BigDecimal.ONE);
        test(BigDecimal.ONE, BigDecimal.ZERO, BigDecimal.ONE);
    }


    @Test
    public void testStrings()
    {
        test("a", "b", "ab");
        test("", "a", "a");
        test("a", "", "a");
    }
    
    @Test
    public void testExceptionCases()
    {
        testException("a", BigDecimal.ONE);
        testException(BigDecimal.ONE, "a");
        testException(Boolean.TRUE, BigDecimal.ONE);
    }
    
    /**
     * Performs test on the class with a set operator type. Checks whether
     * application of the operator to <code>v1</code> and <code>v2</code>
     * results in <code>result</code>.
     */
    private void test(Object v1, Object v2, Object expected) {
        Stack<Object> st = new Stack<>();
        // print current params
        System.out.println(v1+", "+v2);
        
        // add the values
        st.add(v1);
        st.add(v2);

        try {
            pfmc.setCurNumberOfParameters(2);
            // run the operator
            pfmc.run(st);
            // if result is more than one element, fail
            if (st.size() != 1) Assert.fail("Expected 1 value on stack but found "+st.size());
            // get return value
            Object actual = st.pop();
            // ensure it is of the expected type
            Assert.assertEquals(expected.getClass(), actual.getClass());
            // ensure it is the expected value
            Assert.assertEquals(expected, actual);
        } catch (EvaluationException e) {
            Assert.fail("Unexpected exception occurred: " + e.getMessage());
        }

    }
    
    /**
     * Performs test on the class with a set operator type. Expects an 
     * exception to occur during evaluation.
     */
    private void testException(Object v1, Object v2) {
        Stack<Object> st = new Stack<>();
        // print current params
        System.out.println(v1+", "+v2);
        
        // add the values
        st.add(v1);
        st.add(v2);

        try {
            pfmc.setCurNumberOfParameters(2);
            // run the operator
            pfmc.run(st);
            Assert.fail("Expected exception was not thrown");
        } catch (EvaluationException e) {
        }
    }
}
