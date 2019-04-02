/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package org.nfunk.jeptesting;

import java.util.Stack;

import junit.framework.Assert;
import org.junit.Before;
import org.junit.Test;
import org.nfunk.jep.JEP;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.JepException;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.standard.Complex;

/**
 * This class is intended to contain all tests related to reported bugs.
 * 
 * @author Nathan Funk
 */
@SuppressWarnings("deprecation")
public class BugsTest {
    private JEP jep;


    @Before
    public void setUp() {
        // Set up the parser
        jep = new JEP();
        jep.setImplicitMul(true);
        jep.addStandardFunctions();
        jep.addStandardConstants();
        jep.addComplex();
        jep.setTraverse(false);
    }

    /**
     * Tests a bug that lead the FractalCanvas example to fail.
     * (09/04/2007)
     */
    @Test
    public void testFractalBug() {
        JEP myParser;

        //Init Parser
        myParser = new JEP();

        try {
            //Add and initialize x to (0,0)
            myParser.addVariable("x", 0, 0);

            //Parse the new expression
            myParser.parseExpression("x");
            Assert.assertTrue(!myParser.hasError());
            //Reset the values
            myParser.addVariable("x", 1, 1);
            //z.set(0,0);
            System.out.println("x= " + myParser.getVarValue("x"));

            Complex value = myParser.getComplexValue();
            Assert.assertTrue(!myParser.hasError());

            System.out.println("result = " + value);
            Assert.assertTrue(value.re() == 1);
            Assert.assertTrue(value.im() == 1);
        } catch (JepException e) {
            Assert.fail("An exception occured: " + e.getMessage());
        }
    }

    /**
     * Tests the uninitialized OperatorSet bug 1061200
     */
    @Test
    public void testOpSetBug() {
        JEP j = new JEP(false, true, true, null);
        Assert.assertNotNull(j.getOperatorSet());
    }

    /**
     * Tests [ 1562371 ] ParseException not sets jep.hasError() flag.
     * 
     * This bug turned out to actually not be a bug. The user reported that 
     * no error occured from a custom function during parsing, only after
     * evaluation. This is expected behaviour since the run() method is
     * not called during parsing - so even if there is a type compatibility
     * issue, it will not be determined while parsing.
     */
    @Test
    public void testHasError() {		
        System.out.println("---- testHasError ----");
        jep.addFunction("custFunc", new CustFunc());
        jep.parseExpression("custFunc(-1)");
        Assert.assertTrue(!jep.hasError());
        System.out.println("Function should throw exception here");
        jep.getValue();
        Assert.assertTrue(jep.hasError());		

        // additional tests
        // test too many arguments
        jep.parseExpression("custFunc(1, 1)");
        Assert.assertTrue(jep.hasError());
        jep.getValue();
        Assert.assertTrue(jep.hasError());

        // test for empty expression causing error (should have error after parsing)
        jep.parseExpression("");
        Assert.assertTrue(jep.hasError());
        jep.getValue();
        Assert.assertTrue(jep.hasError());

        // test syntax error (should have error after parsing)
        jep.parseExpression("1+");
        Assert.assertTrue(jep.hasError());
        jep.getValue();
        Assert.assertTrue(jep.hasError());

        // test type error (should have error after evaluation)
        jep.parseExpression("sin([1, 1])");
        Assert.assertTrue(!jep.hasError());
        jep.getValue();
        Assert.assertTrue(jep.hasError());
    }

    /**
     * Inner class for testing bug 1562371
     * This custom function returns the parameter if it is a regular number 
     * greater than zero. It throws an exception otherwise.
     * @author singularsys
     */
    private static class CustFunc extends PostfixMathCommand
    {    private static final long serialVersionUID = 330L;

    public CustFunc() { numberOfParameters = 1; }

    @Override
    public void run(Stack<Object> inStack) throws EvaluationException 
    {
        Object param = inStack.pop();
        if (param instanceof Number && ((Number)param).doubleValue() > 0) {
            inStack.push(param);
        } else {
            System.out.println("Throwing exception");
            throw new EvaluationException("Parameter is not a Number or not >0");
        }
        return;
    }
    }

    /**
     * Tests bug [ 1585128 ] setAllowUndeclared does not work!!!
     * setAllowedUndeclared should add variables to the symbol table.
     * 
     * This test parses the expression "x" and checks whether only the
     * variable x is in the symboltable (no more no less)
     */
    @Test
    public void testSetAllowUndeclared() {
        jep.initSymTab();				// clear the Symbol Table
        jep.setAllowUndeclared(true);
        jep.parseExpression("x");
        VariableTable st = jep.getSymbolTable();

        // should only contain a single variable x
        Assert.assertTrue(st.size()==1);
        Assert.assertTrue(st.getVariable("x") != null);
    }

    /**
     * Tests [ 1589277 ] Power function and "third root".
     * 
     * Simple test for (-8)^(1/3) == -2.
     *
	public void testComplexPower() {
		jep.initSymTab();
		jep.parseExpression("(-8)^(1/3)");
		Complex result = jep.getComplexValue();
		Assert.assertTrue(result.equals(new Complex(-2, 0)));
	}*/

    /**
     * Tests [ 1563324 ] getValueAsObject always return null after an error
     * 
     * JEP 2.4.0 checks the <code>errorList</code> variable before evaluating 
     * an expression if there is an error in the list, null is returned. This
     * behaviour is bad because errors are added to the list by
     * getValueAsObject. If the first evaluation fails (after a successful parse)
     * then an error is added to the list. Subsequent calls to getValueAsObject
     * fail because there is an error in the list.
     */
    @Test
    public void testBug1563324() {
        jep.initSymTab();
        jep.setAllowUndeclared(true);
        // parse a valid expression
        jep.parseExpression("abs(x)");
        try {
            // add a variable with a value that causes evaluation to fail
            // (the Random type is not supported by the abs function)
            jep.addVariable("x", new java.util.Random()); 
            Object result = jep.getValueAsObject();
            // evaluation should have failed
            Assert.assertTrue(jep.hasError());

            // change the variable value to a value that should be evaluated
            jep.addVariable("x", -1);
            // ensure that it is evaluated correctly
            result = jep.getValueAsObject();
            Assert.assertTrue(result instanceof Integer);
            Assert.assertEquals(1, ((Integer)result).intValue());
        } catch (JepException e) {
            Assert.fail("Failed with exception: "+e.getMessage());
        }
    }
}
