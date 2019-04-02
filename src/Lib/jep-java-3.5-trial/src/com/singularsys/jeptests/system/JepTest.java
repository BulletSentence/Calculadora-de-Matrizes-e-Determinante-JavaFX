/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jeptests.system;

import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.lang.reflect.Method;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Stack;
import java.util.Vector;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Evaluator;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.functions.Average;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.LazyLogical;
import com.singularsys.jep.functions.MinMax;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.functions.NaryFunction;
import com.singularsys.jep.functions.NullaryFunction;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.functions.StrictNaturalLogarithm;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.functions.VSum;
import com.singularsys.jep.misc.MacroFunction;
import com.singularsys.jep.misc.StringFunctionSet;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.IsNull;
import com.singularsys.jep.misc.functions.Switch;
import com.singularsys.jep.misc.functions.SwitchDefault;
import com.singularsys.jep.misc.javaops.PostDec;
import com.singularsys.jep.parser.ASTConstant;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.ASTOpNode;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealBinaryFunction;
import com.singularsys.jep.reals.RealNaryFunction;
import com.singularsys.jep.reals.RealNullaryFunction;
import com.singularsys.jep.reals.RealUnaryFunction;
import com.singularsys.jep.standard.Complex;

/**
 * Tests various functions on the Jep class.
 * 
 * The current implementation tests using the default setup with the JavaCC
 * parser.
 */
public class JepTest {
    public static final boolean PRINT_RESULTS = true;
    /** The parser */
    protected Jep jep;

    /**
     * Sets up the parser.
     */
    @Before
    public void setUp() {
	System.out.println("setUp");
	// Set up the parser
	jep = new Jep();
	jep.setImplicitMul(true);
	// jep.addStandardFunctions();
	jep.addStandardConstants();
	// jep.addComplex();
	// jep.setTraverse(false);
    }

    /*------------------- utility functions -------------------------------*/

    /**
     * Prints a header with the name of the test as specified in str.
     */
    protected void printTestHeader(String str) {
	System.out.println("\n\n------------------------------------------------------------------------");
	System.out.println("Running \"" + str + "\"\n");
    }

    /**
     * Test result j.evaluate(j.parse(expr))
     * 
     * @param expr
     *            the expression to parse and evaluate
     * @param expected
     *            result expected
     * @throws Exception
     */
    protected void valueTest(String expr, Object expected) throws Exception {
	Node node = jep.parse(expr);
	Object res = calcValue(node);

	myAssertEquals(expr, expected, res);
    }

    /**
     * Calculate the value of an expression.
     * 
     * @param node
     * @throws Exception
     */
    protected Object calcValue(Node node) throws Exception {
	Object res = jep.evaluate(node);
	return res;
    }

    /**
     * Calculate the value of an expression.
     * 
     * @param expr
     * @throws Exception
     */
    protected Object calcValue(String expr) throws Exception {
	Node node = jep.parse(expr);
	Object res = calcValue(node);
	return res;
    }

    /**
     * @throws EvaluationException
     */
    protected void myAssertEquals(String msg, Object expected, Object actual) throws EvaluationException {
	if (PRINT_RESULTS && !expected.equals(actual))
	    System.out.println("Error: '" + msg + "' is '" + actual + "' should be '" + expected + "'");
	assertEquals("<" + msg + ">", expected, actual);
	if (PRINT_RESULTS)
	    System.out.println("Success: value of \"" + msg + "\" is " + actual + "");
    }

    protected void myAssertNaN(String msg, Object actual) {
	if (actual instanceof Double) {
	    if (Double.isNaN(((Double) actual).doubleValue())) {
		System.out.println("Success: Value of \"" + msg + "\" is " + actual + "");
	    } else {
		System.out.println("Error: \"" + msg + "\" is '" + actual + "' should be NaN");
		fail("<" + msg + "> is " + actual + " should be NaN");
	    }
	} else {
	    System.out.println("Error: '" + msg + "' is '" + actual + "' should be 'NaN'");
	    fail("Error: '" + msg + "' is '" + actual + "' should be 'NaN'");
	}
    }

    protected void myAssertNull(String msg, Object actual) {
	if (actual == null) {
	    System.out.println("Success: Value of \"" + msg + "\" is " + actual + "");
	} else {
	    System.out.println("Error: '" + msg + "' is '" + actual + "' should be 'Null'");
	    fail("<" + msg + "> is " + actual + " should be null");
	}
    }

    protected void valueTestNull(String expr) throws Exception {
	Object res = calcValue(expr);
	myAssertNull(expr, res);
    }

    /**
     * Test whether an expression evaluates to NaN
     * 
     * @param expr
     * @throws Exception
     */
    protected void valueTestNaN(String expr) throws Exception {
	Object res = calcValue(expr);
	myAssertNaN(expr, res);
    }

    /**
     * Test whether evaluating an expression results in an exception
     * 
     * @param expr
     * @throws Exception
     */
    protected void valueTestFail(String expr) throws Exception {
	Node node = jep.parse(expr);
	try {
	    calcValue(node);
	    fail(expr + ": EvaluationException should have been thrown");
	} catch (EvaluationException e) {
	    System.out.println(expr + ": expected exception caught [" + e.getMessage() + "]");
	}
    }

    protected void valueTestString(String expr, String expected) throws Exception {
	Object res = calcValue(expr);

	if (res == null)
	    myAssertEquals(expr, expected, null);
	else
	    myAssertEquals(expr, expected, res.toString());
    }

    /**
     * Test parse-evaluate with complex number and given tolerance.
     * 
     * @param expr
     * @param expected
     * @param tol
     * @throws Exception
     */
    protected void complexValueTest(String expr, Complex expected, double tol) throws Exception {
	Object res = calcValue(expr);
	if (expected.equals((Complex) res, tol))
	    System.out.println("Success value of \"" + expr + "\" is " + res);
	else {
	    System.out.println("Error value of \"" + expr + "\" is " + res + " should be " + expected);
	    fail("<" + expr + "> expected: <" + expected + "> but was <" + res + ">");
	}
    }

    /**
     * Test values to within a given precision
     * 
     * @param expr
     *            expression
     * @param a
     *            the expected value
     * @param tol
     *            tolerance
     * @throws Exception
     */
    protected void valueTest(String expr, double a, double tol) throws Exception {
	Object res = calcValue(expr);
	if (res instanceof Double) {
	    double val = ((Double) res).doubleValue();
	    if (Math.abs(val - a) < tol) {
		System.out.println("Success value of \"" + expr + "\" is " + res);
	    } else {
		System.out.println("Error value of \"" + expr + "\" is " + res + " should be " + a);
		assertEquals(expr, a, val, tol);
	    }
	} else {
	    System.out.println("Error value of \"" + expr + "\" is " + res + " should be " + a);
	    fail("<" + expr + "> expected: <" + a + "> but was <" + res + ">");
	}
    }

    static public void nodeTest(Node n, Operator op) {
	assertTrue("Node " + n.toString() + "should have been an ASTOpNode", n instanceof ASTOpNode);
	assertEquals(op, ((ASTOpNode) n).getOperator());
    }

    static public void nodeTest(Node n, String name) {
	assertTrue("Node " + n.toString() + "should have been an ASTFunNode", n instanceof ASTFunNode);
	assertEquals(name, ((ASTFunNode) n).getName());
    }

    static public void nodeTest(Node n, Variable v) {
	assertTrue("Node " + n.toString() + "should have been an ASTVarNode", n instanceof ASTVarNode);
	assertEquals(v, ((ASTVarNode) n).getVar());
    }

    static public void nodeTest(Node n, Object v) {
	assertTrue("Node " + n.toString() + "should have been an ASTConstant node", n instanceof ASTConstant);
	assertEquals(v, ((ASTConstant) n).getValue());
    }

    protected boolean compareRecursive(Node node1, Node node2) {
	if (node1.jjtGetNumChildren() != node2.jjtGetNumChildren())
	    return false;
	if (node1 instanceof ASTConstant) {
	    if (node2 instanceof ASTConstant) {
		return ((ASTConstant) node1).getValue().equals(((ASTConstant) node2).getValue());
	    }
	    return false;
	}
	if (node1 instanceof ASTVarNode) {
	    if (node2 instanceof ASTVarNode) {
		return ((ASTVarNode) node1).getName().equals(((ASTVarNode) node2).getName());
	    }
	    return false;
	}
	if (node1 instanceof ASTOpNode) {
	    if (node2 instanceof ASTOpNode) {
		if (!((ASTOpNode) node1).getOperator().equals(((ASTOpNode) node2).getOperator()))
		    return false;
		for (int i = 0; i < node1.jjtGetNumChildren(); ++i) {
		    if (!compareRecursive(node1.jjtGetChild(i), node2.jjtGetChild(i)))
			return false;
		}
		return true;
	    }
	}
	if (node1 instanceof ASTFunNode) {
	    if (node2 instanceof ASTFunNode) {
		if (!((ASTFunNode) node1).getName().equals(((ASTFunNode) node2).getName()))
		    return false;
		if (((ASTFunNode) node1).getPFMC() != (((ASTFunNode) node2).getPFMC()))
		    return false;
		for (int i = 0; i < node1.jjtGetNumChildren(); ++i) {
		    if (!compareRecursive(node1.jjtGetChild(i), node2.jjtGetChild(i)))
			return false;
		}
		return true;
	    }
	}
	return false;
    }

    protected void assertEqNodes(String s, Node expected, Node actual) {
	boolean flag = compareRecursive(expected, actual);
	boolean mode = jep.getPrintVisitor().getMode(PrintVisitor.FULL_BRACKET);
	jep.getPrintVisitor().setMode(PrintVisitor.FULL_BRACKET, true);
	if (!flag) {
	    System.out.print("Parse: ");
	    System.out.println(s.replaceAll(">\\s+", ">").replaceAll("\\s+<", "<"));
	    System.out.println("actual:\t\t" + jep.toString(actual));
	    if (!flag)
		System.out.println("expected:\t" + jep.toString(expected));
	}
	jep.getPrintVisitor().setMode(PrintVisitor.FULL_BRACKET, mode);
	assertTrue("expected: " + jep.toString(expected) + " actual: " + jep.toString(actual), flag);
	// System.out.println("\nSuccess");
	// System.out.println(s.replaceAll(">\\s+",">").replaceAll("\\s+<","<"));
	// jep.println(actual);
    }

    /*------------------------- tests ---------------------------------------*/

    @Test
    public void testSimpleSum() throws Exception {
	printTestHeader("Testing very simple sums...");

	valueTest("1+2", 3.0);
	valueTest("2*6+3", 15.0);
	valueTest("2*(6+3)", 18.0);
    }

    /**
     * Tests the evaluate() method.
     */
    @Test
    public void testEvaluate() throws Exception {
	printTestHeader("Testing evaluation results...");
	// test a very basic expression
	Object result = jep.evaluate(jep.parse("2.1345"));
	assertTrue(result instanceof Double);
	assertTrue(result.equals(2.1345));
    }

    @Test
    public void testEvaluateComplex() throws Exception {
	printTestHeader("Testing complex evaluation results...");

	// Test Complex numbers
	Object result = jep.evaluate(jep.parse("i"));
	assertTrue(result instanceof Complex);
	assertTrue(result.equals(new Complex(0, 1)));
    }

    @Test
    public void testEvaluateString() throws Exception {
	printTestHeader("Testing string evaluation results...");

	// Test whether a String is passed through
	Object result = jep.evaluate(jep.parse("\"asdf\""));
	assertTrue(result instanceof String);
	assertEquals("asdf", result);
    }

    /**
     * Test changing variables value after parsing
     */
    @Test
    public void testChangeVariable() throws Exception {
	double delta = 1E-10;
	printTestHeader("Testing changing variables...");
	Object result;
	Double d;
	// add the complex variable x = 0,0
	jep.addVariable("x", Double.valueOf(0.0));
	// parse a simple expression with the variable x

	try {
	    jep.parse("x");
	} catch (ParseException e) {
	    fail("Exception occured" + e);
	}
	// jep.getVarValue("x");
	try {
	    result = jep.evaluate();
	} catch (EvaluationException e) {
	    fail("Exception occured" + e.getMessage());
	    return;
	}
	assertTrue(result instanceof Double);
	d = (Double) result;
	assertEquals(d.doubleValue(), 0.0, delta);
	// change the value of x
	jep.addVariable("x", Double.valueOf(1.0));
	// jep.getVarValue("x");
	try {
	    result = jep.evaluate();
	} catch (EvaluationException e) {
	    fail("Exception occured");
	    return;
	}
	assertTrue(result instanceof Double);
	d = (Double) result;
	assertEquals(d.doubleValue(), 1.0, delta);
    }

    /**
     * Test changing variables value after parsing
     */
    @Test
    public void testChangeVariableComplex() throws Exception {
	Object result;
	Complex c;
	// add the complex variable x = 0,0
	jep.addVariable("x", new Complex(0, 0));
	// parse a simple expression with the variable x

	try {
	    jep.parse("x");
	} catch (ParseException e) {
	    fail("Exception occured");
	}
	// jep.getVarValue("x");
	try {
	    result = jep.evaluate();
	} catch (EvaluationException e) {
	    fail("Exception occured" + e.getMessage());
	    return;
	}
	assertTrue(result instanceof Complex);
	c = (Complex) result;
	assertTrue(c.re() == 0);
	assertTrue(c.im() == 0);
	// change the value of x
	jep.addVariable("x", new Complex(1, 1));
	// jep.getVarValue("x");
	try {
	    result = jep.evaluate();
	} catch (EvaluationException e) {
	    fail("Exception occured");
	    return;
	}
	assertTrue(result instanceof Complex);
	c = (Complex) result;
	assertTrue(c.re() == 1);
	assertTrue(c.im() == 1);
    }

    /**
     * Tests whether allowUndeclared is working properly.
     *
     */
    @Test
    public void testSetAllowUndeclared() throws Exception {
	printTestHeader("Testing AllowedUndeclared options...");

	// test whether setAllowUndeclared(true) works
	jep.getVariableTable().clear(); // clear the Variable Table
	jep.setAllowUndeclared(true);
	try {
	    jep.parse("x");
	} catch (ParseException e) {
	    fail("Exception occurred " + e.getMessage());
	}
	VariableTable st = jep.getVariableTable();

	// should only contain a single variable x
	assertTrue(st.size() == 1);
	assertTrue(st.getVariable("x") != null);

	try {
	    jep.evaluate();
	    fail("Exception should have been thrown when trying to evaluate a declared but undefined variable");
	} catch (EvaluationException e) {
	    // exception was thrown, so all is well
	}

	jep.setDefaultValue(Double.valueOf(0.0));
	jep.parse("y");
	Object val = jep.evaluate();
	assertEquals("Value of y using default value", 0.0, val);

	// test whether setAllowUndeclared(false) works
	jep.getVariableTable().clear();
	jep.addVariable("x", new Double(1));
	jep.setAllowUndeclared(false);
	try {
	    jep.parse("p");
	    // since p is not declared, an error should occur
	    fail("A ParseException should have been thrown creating variable 'p'.");
	} catch (ParseException e) {
	    // exception was thrown, so all is well
	}
    }

    protected Object negativeZero() {
	return -0.0;
    }

    @Test
    public void testNumbers() throws Exception {
	printTestHeader("Testing Numbers...");

	valueTest("0", 0.0);
	valueTest("0.", 0.0);
	valueTest(".0", 0.0);
	valueTest("0.0", 0.0);
	valueTest("-0", negativeZero());
	valueTest("-0.0", negativeZero());
	valueTest("1", 1.0);
	valueTest("1.", 1.0);
	valueTest("1.0", 1.0);
	valueTest("-1", -1.0);
	valueTest("-1.", -1.0);
	valueTest("-1.0", -1.0);
	valueTest("1.5", 1.5);
	valueTest(".5", 0.5);
	valueTest("0.5", 0.5);
	valueTest("-1.5", -1.5);
	valueTest("-.5", -0.5);
	valueTest("-0.5", -0.5);

	valueTest("5e2", 500.0);
	valueTest("5E2", 500.0);
	valueTest("5e-2", 0.05);
	valueTest("5e-2", 0.05);
	valueTest("-5e2", -500.0);
	valueTest("-5E2", -500.0);
	valueTest("-5e-2", -0.05);
	valueTest("-5e-2", -0.05);

	valueTest(".5e3", 500.0);
	valueTest(".5E3", 500.0);
	valueTest(".5e-1", 0.05);
	valueTest(".5e-1", 0.05);
	valueTest("-.5e3", -500.0);
	valueTest("-.5E3", -500.0);
	valueTest("-.5e-1", -0.05);
	valueTest("-.5e-1", -0.05);

	valueTest("0.5e3", 500.0);
	valueTest("0.5E3", 500.0);
	valueTest("0.5e-1", 0.05);
	valueTest(".5e-1", 0.05);
	valueTest("-.5e3", -500.0);
	valueTest("-.5E3", -500.0);
	valueTest("-.5e-1", -0.05);
	valueTest("-.5e-1", -0.05);

	valueTest("1.5e3", 1500.0);
	valueTest("1.5E3", 1500.0);
	valueTest("1.5e-1", 0.15);
	valueTest("1.5e-1", 0.15);
	valueTest("-1.5e3", -1500.0);
	valueTest("-1.5E3", -1500.0);
	valueTest("-1.5e-1", -0.15);
	valueTest("-1.5e-1", -0.15);

    }

    @Test
    public void testStrings() throws Exception {
	printTestHeader("Testing strings...");

	valueTest("\"\"", "");
	valueTest("\"a\"", "a");
	valueTest("\"abcdefghijklmnopqrstuvwxyz\"", "abcdefghijklmnopqrstuvwxyz");
	valueTest("sum(\"a\",\"b\")", "ab");
	valueTest("\"a\\\"b\"", "a\"b");
	valueTest("\"A\\bB\\fC\\nD\\rE\\tF\"", "A\bB\fC\nD\rE\tF");
	valueTest("A=\"1\"", "1");
	valueTest("B=\"2\"", "2");
	valueTest("(A==\"1\")&&(B==\"2\")", myTrue);
    }

    @Test
    public void testStringsFun() throws Exception {
	jep.setComponent(new StringFunctionSet());
	jep.setAllowUndeclared(false); // makes debugging easier

	valueTest("left(\"abcdefg\",2)", "ab");
	valueTest("left(\"a\",2)", "a");
	valueTest("left(\"a\",0)", "");
	valueTest("left(\"\",0)", "");
	valueTest("left(\"\",1)", "");
	valueTestFail("left(\"a\",-1)");

	valueTest("right(\"abcdefg\",2)", "fg");
	valueTest("right(\"g\",2)", "g");
	valueTest("right(\"g\",0)", "");
	valueTest("right(\"\",0)", "");
	valueTest("right(\"\",1)", "");
	valueTestFail("right(\"a\",-1)");

	valueTest("substr(\"abcdefg\",2)", "cdefg");
	valueTestFail("substr(\"abcdefg\",10)");
	valueTestFail("substr(\"abcdefg\",-1)");

	valueTest("substr(\"abcdefg\",2,4)", "cd");
	valueTest("substr(\"abcdefg\",2,2)", "");
	valueTestFail("substr(\"abcdefg\",4,2)");
	valueTestFail("substr(\"abcdefg\",4,10)");
	valueTestFail("substr(\"abcdefg\",4,-1)");
	valueTestFail("substr(\"abcdefg\",10,12)");
	valueTestFail("substr(\"abcdefg\",10,8)");
	valueTestFail("substr(\"abcdefg\",-1,0)");
	valueTestFail("substr(\"abcdefg\",-1,-2)");

	valueTest("substr(\"\",0,0)", "");
	valueTestFail("substr(\"\",0,1)");
	valueTestFail("substr(\"\",-1,0)");
	valueTestFail("substr(\"\",-1,0)");

	valueTest("mid(\"abcdefg\",2,2)", "cd");
	valueTest("mid(\"abcdefg\",2,10)", "cdefg");
	valueTestFail("mid(\"abcdefg\",10,2)");
	valueTestFail("mid(\"abcdefg\",-1,2)");
	valueTestFail("mid(\"abcdefg\",2,-1)");

	valueTest("trim(\"  cd  \")", "cd");
	valueTest("trim(\"\ta\")", "a"); // test mixing in tab characters
	valueTest("trim(\"a\t\")", "a");
	valueTest("trim(\"\t a\")", "a");
	valueTest("trim(\"a \t\")", "a");
	valueTest("trim(\"\t a \")", "a");
	valueTest("trim(\"\ta \t\")", "a");
	valueTest("trim(\"a\")", "a"); // nothing to trim
	valueTest("trim(\"\")", ""); // try trimming empty string

	valueTest("upper(\"abcdefg\")", "ABCDEFG");
	valueTest("upper(\"ABCDEFG\")", "ABCDEFG");
	valueTest("upper(\"ABCdEFg\")", "ABCDEFG");
	valueTest("upper(\"ABC DEFg\")", "ABC DEFG");
	valueTest("upper(\"\")", "");

	valueTest("lower(\"ABCDEFG\")", "abcdefg");
	valueTest("lower(\"abcdefg\")", "abcdefg");
	valueTest("lower(\"ABcDEFg\")", "abcdefg");
	valueTest("lower(\"ABc DEFg\")", "abc defg");
	valueTest("lower(\"\")", "");

	valueTest("len(\"abcdefg\")", 7);
	valueTest("len(\"\")", 0);
    }

    @Test
    public void testNumberAsBooleanLogical() throws Exception {
	printTestHeader("Testing logical operations");

	valueTest("T=1", 1.0);
	valueTest("F=0", 0.0);
	valueTest("!T", myFalse);
	valueTest("!F", myTrue);
	valueTest("!5", myFalse);
	valueTest("-0==0", myTrue);
	valueTest("!-5", myFalse);
	// valueTest("-!5==0",myTrue);
	// valueTest("-!0",-1.0);
	valueTest("T&&T", myTrue);
	valueTest("T&&F", myFalse);
	valueTest("F&&T", myFalse);
	valueTest("F&&F", myFalse);
	valueTest("T||T", myTrue);
	valueTest("T||F", myTrue);
	valueTest("F||T", myTrue);
	valueTest("F||F", myFalse);
	calcValue("a=F");
	calcValue("b=F");
	calcValue("c=F");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=F");
	calcValue("b=F");
	calcValue("c=T");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=F");
	calcValue("b=T");
	calcValue("c=F");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=F");
	calcValue("b=T");
	calcValue("c=T");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);

	calcValue("a=T");
	calcValue("b=F");
	calcValue("c=F");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=T");
	calcValue("b=F");
	calcValue("c=T");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=T");
	calcValue("b=T");
	calcValue("c=F");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
	calcValue("a=T");
	calcValue("b=T");
	calcValue("c=T");
	valueTest("(a&&(b||c)) == ((a&&b)||(a&&c))", myTrue);
	valueTest("(a||(b&&c)) == ((a||b)&&(a||c))", myTrue);
    }

    @Test
    public void testLogical() throws Exception {
	printTestHeader("Testing logical operations");


	jep.getVariableTable().remove("true");
	jep.getVariableTable().remove("false");
	jep.addConstant("true", myTrue);
	jep.addConstant("false", myFalse);
	valueTest("true", myTrue);
	valueTest("false", myFalse);
	valueTest("!true", myFalse);
	valueTest("!false", myTrue);
	valueTest("true==true", myTrue);
	valueTest("false==false", myTrue);
	valueTest("true==false", myFalse);
	valueTest("true==true&&false==false", myTrue);
	valueTest("if(true==true&&false==false,6,7)", 6.0);
	valueTest("if(false&&true,6,7)", 7.0);
	valueTest("if(true&&false==false,6,7)", 6.0);
	valueTest("if((true&&true)==true,6,7)", 6.0);
	valueTest("if((!false)==true,6,7)", 6.0);
    }

    protected Object myTrue = Boolean.TRUE;
    protected Object myFalse = Boolean.FALSE;

    @Test
    public void testNull() throws Exception {
	// check if null trapping is on by default
	printTestHeader("Testing for null values");
	jep.addFunction("isNull", new IsNull());
	jep.addConstant("mynull", null);
	try {
	    valueTest("isNull(mynull)", myTrue);
	    fail("Null value should have been trapped");
	} catch (EvaluationException e) {
	    System.out.println("Null value sucessfully trapped");
	}
	// check if isNull(5) returns false as expected
	valueTest("isNull(5)", myFalse);
	// try calling setTrapNullValues(true) with reflection
	try {
	    Evaluator ev = jep.getEvaluator();
	    Method meth;
	    meth = ev.getClass().getMethod("setTrapNullValues", Boolean.TYPE);
	    meth.invoke(ev, false);
	    try {
		valueTest("isNull(mynull)", myTrue);
		valueTestNull("nnn=mynull");
		valueTest("isNull(nnn)", myTrue);
		// valueTest("isNull(null)",myTrue);
	    } catch (Exception e) {
		fail("With TrapNullValues=false " + e.getMessage());
		e.printStackTrace();
	    }
	} catch (NoSuchMethodException e1) {
	    System.out.println("No setTrapNullValues method, skipping tests.");
	}
    }

    @Test
    public void testNaN() throws Exception {
	printTestHeader("Testing for NaN");
	double x = 1.0, y = 0.0;
	double z = x / y;
	if (z < 2.0)
	    System.out.println("1/0<2 is true");
	else
	    System.out.println("1/0<2 is false");

	jep.addVariable("x", new Double(Double.NaN));
	System.out.println("Set x to NaN");
	valueTestNaN("ln(x)");
	valueTestNaN("log(x)");
	valueTestNaN("sin(x)");
	valueTestNaN("x+x");
	valueTest("x!=x", myTrue);
	valueTest("x==x", myFalse);
	valueTest("x<x", myFalse);
	valueTest("x>x", myFalse);
	valueTest("x<=x", myFalse);
	valueTest("x>=x", myFalse);
	valueTest("x<5", myFalse);
	valueTest("x>5", myFalse);
	valueTest("x<=5", myFalse);
	valueTest("x>=5", myFalse);
	valueTest("5<x", myFalse);
	valueTest("5>x", myFalse);
	valueTest("5<=x", myFalse);
	valueTest("5>=x", myFalse);
	valueTestNaN("if(x,3,4)");

	jep.addVariable("y", new Double(Double.NaN));
	System.out.println("Set y to Double(NaN)");
	valueTestNaN("x+5");
	valueTestNaN("y");
	valueTest("x == x+5", myFalse);
	valueTest("x == 0/0", myFalse);
	valueTest("x == x", myFalse);
	valueTest("x == 0 * x", myFalse);
	valueTest("x == 5", myFalse);
	valueTest("x == y", myFalse);
	valueTest("y == y", myFalse);

	System.out.println("Set x to Double(5)");
	jep.addVariable("x", new Double(5));
	valueTest("x == x+5", myFalse);
	valueTest("x == x", myTrue);

    }

    @Test
    public void testComplex() throws Exception {
	printTestHeader("Testing complex values");
	double tol = 0.00000001;

	complexValueTest("z=complex(3,2)", new Complex(3, 2), tol);
	complexValueTest("z*z-z", new Complex(2, 10), tol);
	complexValueTest("z^3", new Complex(-9, 46), tol);
	complexValueTest("(z*z-z)/z", new Complex(2, 2), tol);
	complexValueTest("w=polar(2,pi/2)", new Complex(0, 2), tol);

	complexValueTest("ln(-1)", new Complex(0, Math.PI), tol);
	complexValueTest("log(-100)", new Complex(2.00, 1.3643763538), tol);
	complexValueTest("log(-100)", new Complex(2.00, 1.3643763538), tol);

	complexValueTest("log(10+10 i)", new Complex(1.1505149978319905, 0.3410940884604603), tol);

	complexValueTest("sqrt(-1)", new Complex(0, 1), tol);
	complexValueTest("pow(-1,0.5)", new Complex(0, 1), tol);
	valueTest("arg(w)", Math.PI / 2);
	valueTest("cmod(w)", 2.0);
	valueTest("re(z)", 3.0);
	valueTest("im(z)", 2.0);
	complexValueTest("conj(z)", new Complex(3, -2), tol);
	complexValueTest("exp(pi*i/2)", new Complex(0, 1), tol);
	// complexValueTest("cos(z)",new Complex(3,-2),tol);
    }

    @Test
    public void testFunction() throws Exception {
	printTestHeader("Testing real functions");
	valueTest("abs(2.5)", 2.5);
	valueTest("abs(-2.5)", 2.5);
	valueTest("acos(1/sqrt(2))", Math.PI / 4, 0.00000001);
	valueTest("cos(pi/3)", 0.5, 0.00000001);
	valueTest("atan2(3,4)", Math.atan2(3.0, 4.0), 0.00000001);
	valueTest("2^4", 16.0);
	valueTest("2^10", 1024.0);
	valueTest("2^0.5", Math.sqrt(2), 0.00000001);
	valueTest("2^(-0.5)", 1.0 / Math.sqrt(2), 0.00000001);
	valueTest("2^(-4)", 1.0 / 16, 0.00000001);
	valueTest("2^(-10)", 1.0 / 1024, 0.00000001);
	calcValue("rand()");
	valueTest("4 atan2(1,1)", Math.PI, 0.00000001);
	Node n = jep.getNodeFactory().buildOperatorNode(
		jep.getOperatorTable().getPower(),
		jep.getNodeFactory().buildConstantNode(2.0),
		jep.getNodeFactory().buildConstantNode(-2.0));
	Object res = calcValue(n);
	assertEquals(1.0 / 4.0, res);
	
    }

    @Test
    public void testIf() throws Exception {
	printTestHeader("Testing if statement");
	valueTest("if(1,2,3)", 2.0);
	valueTest("if(-1,2,3)", 3.0);
	valueTest("if(0,2,3)", 3.0);
	valueTest("if(1,2,3,4)", 2.0);
	valueTest("if(-1,2,3,4)", 3.0);
	valueTest("if(0,2,3,4)", 4.0);
	valueTest("if(0>=0,2,3,4)", 2.0);
	valueTest("x=3", 3.0);
	valueTest("if(x==3,1,-1)", 1.0);
	valueTest("if(x!=3,1,-1)", -1.0);
	valueTest("if(x>=3,1,-1)", 1.0);
	valueTest("if(x>3,1,-1)", -1.0);
	valueTest("if(x<=3,1,-1)", 1.0);
	valueTest("if(x<3,1,-1)", -1.0);
    }

    @Test
    public void testPlusPlus() throws Exception {
	printTestHeader("++ notation");
	valueTest("3++2", 5.0);
	valueTest("3+-2", 1.0);
	valueTest("3-+2", 1.0);
	valueTest("3--2", 5.0);
	valueTest("3+++2", 5.0);
	valueTest("3++-2", 1.0);
	valueTest("3+-+2", 1.0);
	valueTest("3+--2", 5.0);
	valueTest("3-++2", 1.0);
	valueTest("3-+-2", 5.0);
	valueTest("3--+2", 5.0);
	valueTest("3---2", 1.0);

    }

    @Test
    public void testImplicitMul() throws Exception {
	printTestHeader("Implicit Multiplication");
	valueTest("x=5", 5.0);
	valueTest("y=7", 7.0);
	valueTest("x2=37", 37.0);
	valueTest("2 x", 10.0);
	valueTest("2x", 10.0);
	valueTest("x 2", 10.0);
	valueTest("x2", 37.0);
	valueTest("-2 x", -10.0);
	valueTest("x y", 35.0);
	valueTest("-x y", -35.0);
	valueTest("x x x x x x x x", Math.pow(5, 8));
	valueTest("3 (2+4)", 18.0);
	valueTest("3(2+4)", 18.0);
	valueTest("2(x-1)", 8.0);
	valueTest("(x+1)(x-1)", 24.0);
	valueTest("x(x-1)", 20.0);
	valueTest("(x+1)x", 30.0);
	valueTest("1+2 3", 7.0);
	valueTest("2 3+4", 10.0);
	valueTest("1-2 3", -5.0);
	valueTest("2 3 4", 24.0);
	valueTest("-(4-2)", -2.0);
    }

    @Test
    public void testUminusPower() throws Exception {
	printTestHeader("Testing precedence of unitary minus and power...");
	valueTest("-1^2", -1.0);
	valueTest("2^-3", 0.125);
    }

    @Test
    public void testNumParam() throws Exception {
	printTestHeader("Number of parameters");
	jep.parse("if(3,1,2)");
	jep.parse("if(4,1,2,3)");
	try {
	    jep.parse("if(5,1,2,3,4)");
	    fail("Did not trap illegal number of arguments");
	} catch (ParseException e) {
	    /* ignore */ }
	jep.parse("a1=1234");
	jep.parse("a2=5678");
	jep.parse("ApportionmentAmt=4321");
	jep.parse("a4 = 2000 + (3000 /2000) + (3.45787 * 33544 - (212.223 /2000)) + + 1200");
	jep.parse(
		"a3 = if(a1 > 0 && ApportionmentAmt < 1000, if(a2 < 2000, if(a2 < 1000, 200, 0), if(a1 > 1000, if((2000 + (3000 /2000) + (3.45787 * 33544 - (212.223 /2000)) + 1200 + ApportionmentAmt / 2000 + ApportionmentAmt * ApportionmentAmt + 2000) > 0, 100, 200),200)), if(a1/a2 < 1000, a1/a2, 1, a1 * a2 + a1))");
	try {
	    jep.parse(
		    "a3 = if(a1 > 0 && ApportionmentAmt < 1000, if(a2 < 2000, if(a2 < 1000, 200, 0), if(a1 > 1000, if((2000 + (3000 /2000) + (3.45787 * 33544 - (212.223 /2000)) + 1200 + ApportionmentAmt / 2000 + ApportionmentAmt * ApportionmentAmt + 2000) > 0, 100, 200)),200), if(a1/a2 < 1000, a1/a2, 1, a1 * a2 + a1))");
	    fail("Did not trap illegal number of arguments");
	} catch (ParseException e) {
	    /* ignore */}
    }

    @Test
    public void testBinom() throws ParseException, Exception {
	printTestHeader("Testing binomial coeffs");
	valueTest("binom(0,0)", 1);
	valueTest("binom(1,0)", 1);
	valueTest("binom(1,1)", 1);
	valueTest("binom(2,0)", 1);
	valueTest("binom(2,1)", 2);
	valueTest("binom(2,2)", 1);
	valueTest("binom(3,0)", 1);
	valueTest("binom(3,1)", 3);
	valueTest("binom(3,2)", 3);
	valueTest("binom(3,3)", 1);
	valueTest("binom(4,0)", 1);
	valueTest("binom(4,1)", 4);
	valueTest("binom(4,2)", 6);
	valueTest("binom(4,3)", 4);
	valueTest("binom(4,4)", 1);
	valueTest("binom(5,0)", 1);
	valueTest("binom(5,1)", 5);
	valueTest("binom(5,2)", 10);
	valueTest("binom(5,3)", 10);
	valueTest("binom(5,4)", 5);
	valueTest("binom(5,5)", 1);

	valueTest("binom(6,0)", 1);
	valueTest("binom(6,1)", 6);
	valueTest("binom(6,2)", 15);
	valueTest("binom(6,3)", 20);
	valueTest("binom(6,4)", 15);
	valueTest("binom(6,5)", 6);
	valueTest("binom(6,6)", 1);

	valueTest("binom(10,1)", 10);
	valueTest("binom(10,5)", 252);
    }

    @Test
    public void testFormat() throws Exception {
	printTestHeader("Format");
	NumberFormat format = NumberFormat.getInstance();
	jep.getPrintVisitor().setNumberFormat(format);
	format.setMaximumFractionDigits(3);
	format.setMinimumFractionDigits(0);

	String s1 = "[10,0,0.1,0.11,0.111,0.1111]";
	Node node = jep.parse(s1);
	String r1 = jep.toString(node);
	this.myAssertEquals(s1, "[10,0,0.1,0.11,0.111,0.111]", r1);
	String s2 = "[0.9,0.99,0.999,0.9999]";
	String r2 = jep.toString(jep.parse(s2));
	this.myAssertEquals(s2, "[0.9,0.99,0.999,1]", r2);

	String s3 = "8 - 7.9";
	jep.parse(s3);
	double res = jep.evaluateD();
	String s = String.format("%.3f", res);
	this.myAssertEquals(s3, "0.100", s);

	String s4 = "round(8-7.9,3)";
	jep.parse(s4);
	res = jep.evaluateD();
	this.myAssertEquals(s4, "0.1", String.valueOf(res));
    }

    @Test
    public void testAssign() throws Exception {
	printTestHeader("Assignment of variables");
	valueTest("x=3", 3.0);
	valueTest("y=3+4", 7.0);
	valueTest("z=x+y", 10.0);
	valueTest("a=b=c=z", 10.0);
	valueTest("b", 10.0);
	valueTest("d=f=a-b", 0.0);
    }

    @Test
    public void testMultiplyBug() throws Exception {
	valueTest("x=2", 2.0);
	valueTest("(x*x)*x*(x*x)", 32.0); // Works fine with Multiply
	// new org.lsmp.djep.vectorJep.VectorJep();
	valueTest("(x*x)*x*(x*x)", 32.0);
	// this created an error in 2.3.0b
	// as creating a VectorJep changed the operator set
	// and hence the broken MMultiply was used.
    }

    @Test
    public void testNoAssign() throws Exception {
	printTestHeader("Assignment of variables");
	jep.setAllowAssignment(false);
	try {
	    jep.parse("x=3");
	    fail("Assignment should not have been possible");
	} catch (ParseException e) {
	    System.out.println("Attempt at assignment successfully caught");
	}
	jep.setAllowAssignment(true);
	jep.parse("x=3");
    }

    @Test
    public void testLazyLogical() throws Exception {
	printTestHeader("Lazy Logical");
	jep.getOperatorTable().getAnd().setPFMC(new LazyLogical(LazyLogical.AND));
	jep.getOperatorTable().getOr().setPFMC(new LazyLogical(LazyLogical.OR));
	class SideEffect extends PostfixMathCommand {
	    private static final long serialVersionUID = 1L;

	    public SideEffect() {
		this.numberOfParameters = 1;
	    }

	    public boolean called = false;

	    @Override
	    public void run(Stack<Object> aStack) throws EvaluationException {
		called = true;
	    }
	}
	SideEffect se = new SideEffect();
	jep.addFunction("sideEffect", se);
	Node n1 = jep.parse("0 || sideEffect(1==2)");
	Object v1 = jep.evaluate(n1);
	assertTrue(se.called);
	assertEquals(myFalse, v1);
	se.called = false;

	Node n2 = jep.parse("1 || sideEffect(1==2)");
	Object v2 = jep.evaluate(n2);
	assertFalse(se.called);
	assertEquals(myTrue, v2);
	se.called = false;

	Node n3 = jep.parse("0 && sideEffect(1==2)");
	Object v3 = jep.evaluate(n3);
	assertFalse(se.called);
	assertEquals(myFalse, v3);
	se.called = false;

	Node n4 = jep.parse("1 && sideEffect(1==2)");
	Object v4 = jep.evaluate(n4);
	assertTrue(se.called);
	assertEquals(myFalse, v4);
	se.called = false;

	Node n5 = jep.parse("0 || sideEffect(2==2)");
	Object v5 = jep.evaluate(n5);
	assertTrue(se.called);
	assertEquals(myTrue, v5);
	se.called = false;

	Node n6 = jep.parse("1 || sideEffect(2==2)");
	Object v6 = jep.evaluate(n6);
	assertFalse(se.called);
	assertEquals(myTrue, v6);
	se.called = false;

	Node n7 = jep.parse("0 && sideEffect(2==2)");
	Object v7 = jep.evaluate(n7);
	assertFalse(se.called);
	assertEquals(myFalse, v7);
	se.called = false;

	Node n8 = jep.parse("1 && sideEffect(2==2)");
	Object v8 = jep.evaluate(n8);
	assertTrue(se.called);
	assertEquals(myTrue, v8);
	se.called = false;

	Node n9 = jep.parse("if(1,sideEffect(2),3)");
	Object v9 = jep.evaluate(n9);
	assertTrue(se.called);
	assertEquals(new Double(2), v9);
	se.called = false;

	Node n10 = jep.parse("if(1,2,sideEffect(3))");
	Object v10 = jep.evaluate(n10);
	assertFalse(se.called);
	assertEquals(new Double(2), v10);
	se.called = false;

	Node n11 = jep.parse("if(0,sideEffect(2),3)");
	Object v11 = jep.evaluate(n11);
	assertFalse(se.called);
	assertEquals(new Double(3), v11);
	se.called = false;

	Node n12 = jep.parse("if(0,2,sideEffect(3))");
	Object v12 = jep.evaluate(n12);
	assertTrue(se.called);
	assertEquals(new Double(3), v12);
	se.called = false;
    }

    @Test
    public void testLazyLogical2() throws Exception {
	printTestHeader("Lazy Logical2");
	class SideEffect extends PostfixMathCommand {
	    private static final long serialVersionUID = 1L;

	    public SideEffect() {
		this.numberOfParameters = 1;
	    }

	    public boolean called = false;

	    @Override
	    public void run(Stack<Object> aStack) throws EvaluationException {
		called = true;
	    }
	}
	SideEffect se = new SideEffect();
	jep.addFunction("sideEffect", se);
	Node n1 = jep.parse("false || sideEffect(1==2)");
	Object v1 = jep.evaluate(n1);
	assertTrue(se.called);
	assertEquals(myFalse, v1);
	se.called = false;

	Node n2 = jep.parse("true || sideEffect(1==2)");
	Object v2 = jep.evaluate(n2);
	assertFalse(se.called);
	assertEquals(myTrue, v2);
	se.called = false;

	Node n3 = jep.parse("false && sideEffect(1==2)");
	Object v3 = jep.evaluate(n3);
	assertFalse(se.called);
	assertEquals(myFalse, v3);
	se.called = false;

	Node n4 = jep.parse("true && sideEffect(1==2)");
	Object v4 = jep.evaluate(n4);
	assertTrue(se.called);
	assertEquals(myFalse, v4);
	se.called = false;

	Node n5 = jep.parse("false || sideEffect(2==2)");
	Object v5 = jep.evaluate(n5);
	assertTrue(se.called);
	assertEquals(myTrue, v5);
	se.called = false;

	Node n6 = jep.parse("true || sideEffect(2==2)");
	Object v6 = jep.evaluate(n6);
	assertFalse(se.called);
	assertEquals(myTrue, v6);
	se.called = false;

	Node n7 = jep.parse("false && sideEffect(2==2)");
	Object v7 = jep.evaluate(n7);
	assertFalse(se.called);
	assertEquals(myFalse, v7);
	se.called = false;

	Node n8 = jep.parse("true && sideEffect(2==2)");
	Object v8 = jep.evaluate(n8);
	assertTrue(se.called);
	assertEquals(myTrue, v8);
	se.called = false;

	valueTest("if(1,sideEffect(2),3)",calcValue("2"));
	assertTrue(se.called);
	se.called = false;

	valueTest("if(1,2,sideEffect(3))",calcValue("2"));
	assertFalse(se.called);
	se.called = false;

	valueTest("if(0,sideEffect(2),3)",calcValue("3"));
	assertFalse(se.called);
	se.called = false;

	valueTest("if(0,2,sideEffect(3))",calcValue("3"));
	assertTrue(se.called);
	se.called = false;
    }

    /**
     * Test for bug with 1&amp;&amp;1&amp;&amp;1
     * 
     */

    @Test
    public void testLazyLogicalBug() throws Exception {
	valueTest("true&&1", myTrue);
    }

    @Test
    public void testLogarithm() throws Exception {
	printTestHeader("Logarithm");
	FunctionTable ft = jep.getFunctionTable();
	ft.addFunction("slog", new StrictNaturalLogarithm());
	ft.addFunction("slog10", new StrictNaturalLogarithm(10));
	ft.addFunction("slog2", new StrictNaturalLogarithm(2));
	jep.addVariable("NaN", Double.NaN);
	valueTest("ln(1)", 0.0, 1E-7);
	valueTest("slog(1)", 0.0, 1E-7);
	valueTest("slog10(1)", 0.0, 1E-7);
	valueTest("ln(e)", 1.0, 1E-7);
	valueTest("slog(e)", 1.0, 1E-7);
	valueTest("slog10(10)", 1.0, 1E-7);
	valueTestNaN("slog(-1)");
	valueTestNaN("slog10(-1)");
	valueTestNaN("slog2(-1)");
    }

    /**
     * Test creating a Jep instance with no functions and no variables.
     * 
     * @throws Exception
     */
    @Test
    public void testBlankParser() throws Exception {
	printTestHeader("Testing blank parser");
	Jep j = new Jep();
	// override standard variable table with blank one
	j.setComponent(new VariableTable());
	// override standard function table with blank one
	j.setComponent(new FunctionTable());

	// ensure function and variable tables are blank
	assertTrue(j.getFunctionTable().size() == 0);
	assertTrue(j.getVariableTable().size() == 0);
	// ensure operators still work
	j.parse("1+1");
	Object res = j.evaluate();
	assertTrue(res instanceof Double);
	assertTrue(((Double) res).doubleValue() == 2.0);
    }

    @Test
    public void testMacroFunction() throws Exception {
	printTestHeader("Testing MacroFunctions...");
	MacroFunction fact = new MacroFunction(jep, "mySec", "x", "1/cos(x)");
	// MacroFunction fact = new MacroFunction(jep, "mySec", new String[] {
	// "x" }, "1/cos(x)");
	jep.addFunction("mySec", fact);
	// fact.init(jep);
	valueTest("mySec(5)", 1.0 / Math.cos(5.0), 0.000001);
	jep.addVariable("x", 4);
	valueTest("mySec(5)", 1.0 / Math.cos(5.0), 0.000001);
	assertEquals(4.0, jep.getVariableValue("x"));
	valueTest("x+mySec(x+1)", 4.0 + 1.0 / Math.cos(5.0), 0.000001);
    }

    @Test
    public void testRecursiveMacroFunction() throws Exception {
	printTestHeader("Testing MacroFunctions...");
	MacroFunction fact = new MacroFunction("fact", new String[] { "x" }, "if(x>1,x*fact(x-1),1)");
	jep.addFunction("fact", fact);
	fact.init(jep);
	valueTest("fact(5)", 120.0);
	jep.addVariable("x", 4);
	valueTest("fact(5)", 120.0);
	assertEquals(4.0, jep.getVariableValue("x"));
	valueTest("x+fact(x+1)", 124.0);
    }

    /**
     * Tests whether a parse exceptions are thrown when they should be. See
     * ticket #50 Tests on both the standard parser and the configurable parser,
     * is carried out by CPTest which extends this.
     */
    @Test
    public void testParseException() {
	printTestHeader("Testing ParseException...");

	// all these expressions should cause ParseException
	String expr[] = { "1+", "(", ")", "sin(", "(1+)", "*1", "1*", "-", "avg()", "min()", "max()" };

	// for each expression
	for (int i = 0; i < expr.length; i++) {
	    try {
		System.out.println("Attempting parse of \"" + expr[i] + "\"");
		jep.parse(expr[i]);
		// expression should have failed to parse
		fail("Attempting parse of \"" + expr[i] + "\"");
	    } catch (ParseException e) {
		// expected a parse exception has been thrown
		System.out.println("Caught expected exception \"" + e.getMessage() + "\"");
	    } catch (Exception e) {
		// some other exception was thrown
		fail("Attempting parse of \"" + expr[i] + "\"" + e.getMessage());
	    }
	}
    }

    /**
     * Test whether evaluation exceptions are thrown when they should be.
     */
    @Test
    public void testEvalExceptions() {
	printTestHeader("Ensuring evaluation exception are thrown when they should be...");
	ArrayList<String> exprs = new ArrayList<>();

	// random expressions that will fail during evaluation
	// exprs.add("avg()");
	// exprs.add("min()");
	// exprs.add("max()");
	exprs.add("avg([])");
	exprs.add("min([])");
	exprs.add("max([])");

	// add expressions like sin([1, 1]) that can not handle arrays
	String noArrayFuncs[] = { // list of functions that can not take arrays
				  // as arguments
		"sin", "cos", "tan", "asin", "acos", "atan", "sinh", "cosh", "tanh", "asinh", "acosh", "atanh", "log",
		"ln", "exp" };
	for (String funcname : noArrayFuncs) {
	    exprs.add(funcname + "([])");
	    exprs.add(funcname + "([1,1])");
	}

	// loop through each exception
	for (String str : exprs) {
	    // parsing should work fine
	    try {
		jep.parse(str);
	    } catch (ParseException e) {
		fail("Could not parse expression: \"" + str + "\", " + e.getMessage());
	    }
	    // but evaluation should fail
	    try {
		jep.evaluate();
		// we shouldn't get here because these expressions should
		// end up throwing eval expections
		fail("No evaluation exception was thrown as expected.");
	    } catch (EvaluationException e) {
		// we should end up here
	    }
	}
    }

    @Test
    public void testSemiColon() throws Exception {
	String s = "x=5; y=6; x+y";
	printTestHeader("Testing reading multiple equations: " + s);
	jep.initMultiParse(s);
	Node n1 = jep.continueParsing();
	Node n2 = jep.continueParsing();
	Node n3 = jep.continueParsing();
	Object o1 = jep.evaluate(n1);
	this.myAssertEquals("x=5", 5.0, o1);
	Object o2 = jep.evaluate(n2);
	this.myAssertEquals("y=6", 6.0, o2);
	Object o3 = jep.evaluate(n3);
	this.myAssertEquals("x+y", 11.0, o3);
	Node n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);
    }

    @Test
    public void testMultiLine() throws Exception {
	String s = "\nx=\n5;y=6;\nx+y";
	printTestHeader("Testing reading multiple equations: " + s);
	jep.initMultiParse(s);

	Node n1 = jep.continueParsing();
	Object o1 = jep.evaluate(n1);
	this.myAssertEquals("x=5", 5.0, o1);

	Node n2 = jep.continueParsing();
	Object o2 = jep.evaluate(n2);
	this.myAssertEquals("y=6", 6.0, o2);

	Node n3 = jep.continueParsing();
	Object o3 = jep.evaluate(n3);
	this.myAssertEquals("x+y", 11.0, o3);

	Node n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

	/******/

	s = "x*y";
	jep.initMultiParse(s);

	n1 = jep.continueParsing();
	o1 = jep.evaluate(n1);
	myAssertEquals("x*y", 30.0, o1);

	n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

	/******/

	s = "\n";
	jep.initMultiParse(s);
	n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

	/******/

	s = "";
	jep.initMultiParse(s);
	n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

	/******/

	s = "\n\n\n";
	jep.initMultiParse(s);
	n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

	/******/

	s = "\n\n\n1\n\n\n";
	jep.initMultiParse(s);
	n1 = jep.continueParsing();
	o1 = jep.evaluate(n1);
	myAssertEquals("1", 1.0, o1);
	n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);

    }

    @Test
    public void testEmptyEqn() throws Exception {
	String s = "x=5;;y=6;x+y";
	printTestHeader("Testing reading multiple equations: " + s);
	jep.initMultiParse(s);

	Node n1 = jep.continueParsing();
	Object o1 = jep.evaluate(n1);
	this.myAssertEquals("x=5", 5.0, o1);

	Node n2 = jep.continueParsing();
	Object o2 = jep.evaluate(n2);
	this.myAssertEquals("y=6", 6.0, o2);

	Node n3 = jep.continueParsing();
	Object o3 = jep.evaluate(n3);
	this.myAssertEquals("x+y", 11.0, o3);

	Node n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);
    }

    @Test
    public void testComments() throws Exception {
	printTestHeader("Testing comments: ");
	NodeFactory nf = jep.getNodeFactory();
	OperatorTableI ot = jep.getOperatorTable();

	Node expected = nf.buildOperatorNode(ot.getDivide(),
		nf.buildOperatorNode(ot.getMultiply(), nf.buildVariableNode("x"), nf.buildVariableNode("y")),
		nf.buildVariableNode("z"));

	String s = "x*y/z //foo\n";
	Node node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "//foo\nx*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x//foo\n*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* foo */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* foo */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/ /* foo */z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/* foo *//z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*/* foo */y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x/* foo */*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "/* foo */x*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* foo/bar */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z/* foo*bar */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z/* foo\n */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/ /* foo\n */z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/* foo\n *//z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*/* foo\n */y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x/* foo\n */*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "/* foo\n */x*y/z";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* foo/bar\n */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* foo*bar\n */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* \nfoo/bar */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* \nfoo*bar */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* \nfoo/bar\n */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

	s = "x*y/z /* \nfoo*bar\n */";
	node = jep.parse(s);
	this.assertEqNodes(s, expected, node);

    }

    @Test
    public void testEmptyEqn2() throws Exception {
	String s = "x=5;/*foo*/;y=6;x+y";
	printTestHeader("Testing reading multiple equations: " + s);
	jep.initMultiParse(s);

	Node n1 = jep.continueParsing();
	Object o1 = jep.evaluate(n1);
	this.myAssertEquals("x=5", 5.0, o1);

	Node n2 = jep.continueParsing();
	Object o2 = jep.evaluate(n2);
	this.myAssertEquals("y=6", 6.0, o2);

	Node n3 = jep.continueParsing();
	Object o3 = jep.evaluate(n3);
	this.myAssertEquals("x+y", 11.0, o3);

	Node n4 = jep.continueParsing();
	assertNull("Next parse should give a null result", n4);
    }

    @Test
    public void testX2Y() throws Exception {
	String s = "x^2y";
	Node node = jep.parse(s);
	NodeFactory nf = jep.getNodeFactory();
	OperatorTableI ot = jep.getOperatorTable();
	Node expected = nf.buildOperatorNode(ot.getMultiply(),
		nf.buildOperatorNode(ot.getPower(), nf.buildVariableNode("x"), nf.buildConstantNode(2.0)),
		nf.buildVariableNode("y"));
	this.assertEqNodes(s, expected, node);

	s = "!x y";
	node = jep.parse(s);
	expected = nf.buildOperatorNode(ot.getMultiply(), nf.buildOperatorNode(ot.getNot(), nf.buildVariableNode("x")),
		nf.buildVariableNode("y"));
	this.assertEqNodes(s, expected, node);

	s = "x*2y";
	node = jep.parse(s);
	expected = nf.buildOperatorNode(ot.getMultiply(),
		nf.buildOperatorNode(ot.getMultiply(), nf.buildVariableNode("x"), nf.buildConstantNode(2.0)),
		nf.buildVariableNode("y"));
	this.assertEqNodes(s, expected, node);

	s = "x/2y";
	node = jep.parse(s);
	expected = nf.buildOperatorNode(ot.getMultiply(),
		nf.buildOperatorNode(ot.getDivide(), nf.buildVariableNode("x"), nf.buildConstantNode(2.0)),
		nf.buildVariableNode("y"));
	this.assertEqNodes(s, expected, node);

	s = "x+2y";
	node = jep.parse(s);
	expected = nf.buildOperatorNode(ot.getAdd(), nf.buildVariableNode("x"),
		nf.buildOperatorNode(ot.getMultiply(), nf.buildConstantNode(2.0), nf.buildVariableNode("y")));
	this.assertEqNodes(s, expected, node);

    }

    /**
     * Tests for bug #52
     */
    @Test
    public void testExpectedTokenSequence() {
	printTestHeader("Checking whether expected token sequence is set");
	Jep j = new Jep();
	try {
	    j.parse("1+");
	} catch (com.singularsys.jep.ParseException e) {
	    Throwable t = e.getCause();
	    // ensure the cause is set
	    assertNotNull(t);
	    if (t instanceof com.singularsys.jep.parser.ParseException) {
		com.singularsys.jep.parser.ParseException specialE = (com.singularsys.jep.parser.ParseException) t;
		System.out.println("Contains parser.ParseException :)");
		int expectedTokens[][] = specialE.expectedTokenSequences;
		assertTrue(expectedTokens.length > 0);
	    } else {
		fail();
	    }
	}
    }

    @Test
    public void testSwitch() throws Exception {
	jep.addFunction("switch", new Switch());
	jep.addFunction("case", new Case());
	jep.addFunction("switchd", new SwitchDefault());

	valueTest("switch(1,5,6,7,8)", 5.0);
	valueTest("switch(2,5,6,7,8)", 6.0);
	valueTest("switch(3,5,6,7,8)", 7.0);
	valueTest("switch(4,5,6,7,8)", 8.0);
	try {
	    valueTest("switch(5,5,6,7,8)", 8.0);
	    fail("Exception should hav been thrown");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}
	try {
	    valueTest("switch(0,5,6,7,8)", 0.0);
	    fail("Exception should hav been thrown " + "switch(0,5,6,7,8)");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}

	valueTest("case(1.0,1.0,5,2.0,6,3.0,7,8)", 5.0);
	valueTest("case(2.0,1.0,5,2.0,6,3.0,7,8)", 6.0);
	valueTest("case(3.0,1.0,5,2.0,6,3.0,7,8)", 7.0);
	valueTest("case(4.0,1.0,5,2.0,6,3.0,7,8)", 8.0);
	try {
	    valueTest("case(4.0,1.0,5,2.0,6,3.0,7)", 8.0);
	    fail("Exception should have been thrown");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}

	try {
	    valueTest("case(0.0,1.0,5,2.0,6,3.0,7)", 8.0);
	    fail("Exception should have been thrown " + "case(0.0,1.0,5,2.0,6,3.0,7)");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}

	valueTest("switchd(1,5,6,7,8)", 5.0);
	valueTest("switchd(2,5,6,7,8)", 6.0);
	valueTest("switchd(3,5,6,7,8)", 7.0);
	valueTest("switchd(4,5,6,7,8)", 8.0);
	valueTest("switchd(5,5,6,7,8)", 8.0);
	try {
	    valueTest("switchd(0,5,6,7,8)", 8.0);
	    fail("Exception should hav been thrown " + "switchd(0,5,6,7,8)");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}

    }

    @Test
    public void testCaseString() throws Exception {
	jep.addFunction("case", new Case());
	jep.addFunction("cased", new Case(-1.0, Case.NullBehaviour.TEST_ARG));
	jep.addConstant("null", null);

	valueTest("case(\"a\",\"a\",5,\"b\",6,\"c\",7,8)", 5.0);
	valueTest("case(\"b\",\"a\",5,\"b\",6,\"c\",7,8)", 6.0);
	valueTest("case(\"c\",\"a\",5,\"b\",6,\"c\",7,8)", 7.0);
	valueTest("case(\"d\",\"a\",5,\"b\",6,\"c\",7,8)", 8.0);
	valueTest("cased(\"d\",\"a\",5,\"b\",6,\"c\",7)", -1.0);
	try {
	    valueTest("case(\"d\",\"a\",5,\"b\",6,\"c\",7)", 8.0);
	    fail("Exception should hav been thrown");
	} catch (JepException e) {
	    System.out.println("Expected exception thrown: " + e.toString());
	}
    }

    @Test
    public void testCaseNull() throws Exception {
	jep.addFunction("cased", new Case(-1.0, Case.NullBehaviour.TEST_ARG));
	jep.addConstant("null", null);
	Method m = jep.getEvaluator().getClass().getMethod("setTrapNullValues", Boolean.TYPE);
	if (m != null)
	    m.invoke(jep.getEvaluator(), false);

	valueTest("cased(null,\"a\",5,null,6,\"c\",7)", 6.0);
	valueTest("cased(null,\"a\",5,\"b\",6,\"c\",7)", -1.0);
    }

    protected Object buildVector(Object... eles) throws Exception {
	Vector<Object> vec = new Vector<>();
	for (Object o : eles)
	    vec.add(o);
	return vec;
    }

    protected Object buildMatrix(Object[]... eles) throws Exception {
	Vector<Object> mat = new Vector<>();
	for (Object[] r : eles) {
	    Vector<Object> row = new Vector<>();
	    for (Object e : r)
		row.add(e);
	    mat.add(row);
	}
	return mat;
    }

    @Test
    public void testListAccess() throws Exception {
	printTestHeader("List operations");
	valueTestString("x=[4,3,2,1]", buildVector(4.0, 3.0, 2.0, 1.0).toString());
	valueTest("x[2]", 3.0);
	valueTest("x[4]=5", 5.0);
	valueTestString("x", buildVector(4.0, 3.0, 2.0, 5.0).toString());
	valueTestString("x+x", buildVector(8.0, 6.0, 4.0, 10.0).toString());
	valueTestString("x-x", buildVector(0.0, 0.0, 0.0, 0.0).toString());
	valueTestString("x*2", buildVector(8.0, 6.0, 4.0, 10.0).toString());
	valueTestString("2*x", buildVector(8.0, 6.0, 4.0, 10.0).toString());
	valueTestString("x/2", buildVector(2.0, 1.5, 1.0, 2.5).toString());
	valueTestString("w=[1+i,1-i]", buildVector(new Complex(1.0, 1.0), new Complex(1.0, -1.0)).toString());
	valueTestString("2*w", buildVector(new Complex(2.0, 2.0), new Complex(2.0, -2.0)).toString());

	jep.addVariable("y", buildVector(1.2, 3.4));
	Object res = jep.evaluate(jep.parse("y[2]"));
	assertEquals(3.4, res);
	Object res2 = jep.evaluate(jep.parse("y*2"));
	checkVector(new Object[] { 2.4, 6.8 }, res2);

	{
	    String s = "x[5]";
	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

	{
	    String s = "x[0]";
	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}
	{
	    String s = "x[5]=7";
	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

	{
	    String s = "x[0]=7";
	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

    }

    /**
     * @throws EvaluationException
     */
    public void checkVector(Object[] expected, Object actual) throws EvaluationException {
	assertTrue(actual instanceof Vector<?>);
	@SuppressWarnings("unchecked")
	Vector<Object> vec = (Vector<Object>) actual;
	Object[] array = vec.toArray();
	assertArrayEquals(expected, array);
    }

    @Test
    public void testMultiDimArray() throws Exception {
	printTestHeader("List operations");
	valueTestString("x=[[1,2],[3,4]]", buildMatrix(new Object[][] { { 1.0, 2.0 }, { 3.0, 4.0 } }).toString());
	valueTestString("x+x", buildMatrix(new Object[][] { { 2.0, 4.0 }, { 6.0, 8.0 } }).toString());
	valueTestString("x-x", buildMatrix(new Object[][] { { 0.0, 0.0 }, { 0.0, 0.0 } }).toString());
	valueTestString("x*2", buildMatrix(new Object[][] { { 2.0, 4.0 }, { 6.0, 8.0 } }).toString());
	valueTestString("2*x", buildMatrix(new Object[][] { { 2.0, 4.0 }, { 6.0, 8.0 } }).toString());
	valueTestString("x/2", buildMatrix(new Object[][] { { 0.5, 1.0 }, { 1.5, 2.0 } }).toString());

	{
	    String s = "x[3]";

	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

	{
	    String s = "x[0]";

	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}
	{
	    String s = "x[3]=7";

	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

	{
	    String s = "x[0]=7";

	    try {
		jep.parse(s);
		jep.evaluate();
		fail(s + " should throw an EvaluationException");
	    } catch (EvaluationException e) {
		System.out.println(s + " expected exception caught " + e.toString());
	    }
	}

    }

    @Test
    public void testListFunctions() throws Exception {
	printTestHeader("List functions");
	valueTestString("x=[[1,2],[3,4]]", buildMatrix(new Object[][] { { 1.0, 2.0 }, { 3.0, 4.0 } }).toString());
	valueTestString("min(x)", "1.0");
	valueTestString("max(x)", "4.0");
	valueTestString("avg(x)", "2.5");
	valueTestString("vsum(x)", "10.0");
	valueTestString("y=[]", "[]");

	this.valueTestFail("avg(y)");
	this.valueTestFail("min(y)");
	this.valueTestFail("max(y)");
	valueTestString("vsum(y)", "0.0");
	((Average) jep.getFunctionTable().getFunction("avg"))
		.setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
	((MinMax) jep.getFunctionTable().getFunction("min"))
		.setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
	((MinMax) jep.getFunctionTable().getFunction("max"))
		.setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
	((VSum) jep.getFunctionTable().getFunction("vsum"))
		.setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
	this.valueTestNaN("avg(y)");
	this.valueTestNaN("min(y)");
	this.valueTestNaN("max(y)");
	valueTestString("vsum(y)", "0.0");
    }

    @Test
    public void testListExtra() throws Exception {
	printTestHeader("List extra operations");
	// valueTestString("[]","[]");
	valueTestString("x=[[1,2],[3,4],[5,6]]",
		buildMatrix(new Object[][] { { 1.0, 2.0 }, { 3.0, 4.0 }, { 5.0, 6.0 } }).toString());
	valueTestString("x[2]", buildVector(3.0, 4.0).toString());
    }

    static class PiFun extends PostfixMathCommand implements RealNullaryFunction {
	private static final long serialVersionUID = 1L;

	public PiFun() {
	    super(0);
	}

	@Override
	public double evaluate() {
	    return Math.PI;
	}

	@Override
	public void run(Stack<Object> s) throws EvaluationException {
	    s.push(Math.E);
	}
    }

    static class Square extends PostfixMathCommand implements RealUnaryFunction {
	private static final long serialVersionUID = 1L;

	public Square() {
	    super(1);
	}

	@Override
	public double evaluate(double l) {
	    return l * l;
	}

	@Override
	public void run(Stack<Object> s) throws EvaluationException {
	    double res = (double) s.pop();
	    s.push(-res);
	}

    }

    static class Diff extends PostfixMathCommand implements RealBinaryFunction {
	private static final long serialVersionUID = 1L;

	public Diff() {
	    super(2);
	}

	@Override
	public double evaluate(double l, double r) {
	    return l - r;
	}

	@Override
	public void run(Stack<Object> s) throws EvaluationException {
	    double res = (double) s.pop();
	    res += (double) s.pop();
	    s.push(res);
	}
    }

    static class Product extends PostfixMathCommand implements RealNaryFunction {
	private static final long serialVersionUID = 1L;

	public Product() {
	    super(-1);
	}

	@Override
	public double evaluate(double[] parameters) {
	    double res = parameters[0];
	    for (int i = 1; i < parameters.length; ++i)
		res *= parameters[i];
	    return res;
	}

	@Override
	public void run(Stack<Object> s) throws EvaluationException {
	    int n = this.curNumberOfParameters;
	    double res = (double) s.pop();
	    for (int i = 1; i < n; ++i)
		res += (double) s.pop();
	    s.push(res);
	}

    }

    class MyNullary extends NullaryFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public Object eval() throws EvaluationException {
	    return Math.PI;
	}
    }

    class MyUnary extends UnaryFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public Object eval(Object l) throws EvaluationException {
	    double val = this.asDouble(0, l);
	    return Double.valueOf(val * val * val);
	}
    }

    class MyBinary extends BinaryFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public Object eval(Object l, Object r) throws EvaluationException {
	    double val = this.asDouble(0, l);
	    double val2 = this.asDouble(1, r);
	    return Double.valueOf(val + val2);
	}
    }

    class MyNaryBinary extends NaryBinaryFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean checkNumberOfParameters(int n) {
	    return n > 0;
	}

	@Override
	public Object eval(Object l, Object r) throws EvaluationException {
	    double val = this.asDouble(0, l);
	    double val2 = this.asDouble(1, r);
	    return Double.valueOf(val + val2);
	}
    }

    class MyNary extends NaryFunction {
	private static final long serialVersionUID = 1L;

	@Override
	public boolean checkNumberOfParameters(int n) {
	    return n > 0;
	}

	@Override
	public Object eval(Object[] args) throws EvaluationException {
	    double res = this.asDouble(0, args[0]);
	    for(int i=1;i<args.length;++i)
		res += this.asDouble(i, args[i]);
	    return res;
	}
    }

    @Test
    public void testSpecialFunctions() throws Exception {
	testSpecialFunctions(false, true);
    }

    public void testSpecialFunctions(boolean hasRealEval, boolean hasCallback) throws Exception {

	jep.addFunction("Pi", new PiFun());
	jep.addFunction("Square", new Square());
	jep.addFunction("Diff", new Diff());
	jep.addFunction("Product", new Product());
	jep.addFunction("postDec", new PostDec());
	jep.addFunction("mypi", new MyNullary());
	jep.addFunction("cube", new MyUnary());
	jep.addFunction("myadd", new MyBinary());
	jep.addFunction("mysum", new MyNaryBinary());
	jep.addFunction("mysum2", new MyNary());

	if (hasRealEval) {
	    valueTest("Pi()", Math.PI); // RealNullary
	    valueTest("Square(2)", 4.0); // RealUnary
	    valueTest("Diff(5,2)", 3.0); // RealBinary
	    valueTest("Product(1)", 1.0); // RealNary
	    valueTest("Product(1,2)", 2.0); // RealNary
	    valueTest("Product(1,2,3)", 6.0);
	    valueTest("Product(1,2,3,4)", 24.0);
	    valueTest("Product(1,2,3,4,5)", 120.0);
	} else {
	    valueTest("Pi()", Math.E); // RealNullary
	    valueTest("Square(2)", -2.0); // RealUnary
	    valueTest("Diff(5,2)", 7.0); // RealBinary
	    valueTest("Product(1)", 1.0); // RealNary
	    valueTest("Product(1,2)", 3.0); // RealNary
	    valueTest("Product(1,2,3)", 6.0);
	    valueTest("Product(1,2,3,4)", 10.0);
	    valueTest("Product(1,2,3,4,5)", 15.0);
	}

	valueTest("max(1)", 1.0); // Just a PFMC
	valueTest("max(1,2)", 2.0);
	valueTest("max(1,2,3)", 3.0);
	valueTest("max(1,2,3,4)", 4.0);
	valueTest("max(1,2,3,4,5)", 5.0);

	valueTest("mypi()", Math.PI); // NullaryFunction
	valueTest("cube(3)", 27.0); // UnaryFunction
	valueTest("myadd(4,2)", 6.0); // BinaryFunction

	valueTest("mysum(1)", 1.0); // NaryBinaryFunction
	valueTest("mysum(1,2)", 3.0); // NaryBinaryFunction
	valueTest("mysum(1,2,3)", 6.0);
	valueTest("mysum(1,2,3,4)", 10.0);
	valueTest("mysum(1,2,3,4,5)", 15.0);

	valueTest("mysum2(1)", 1.0); // NaryFunction
	valueTest("mysum2(1,2)", 3.0); // NaryFunction
	valueTest("mysum2(1,2,3)", 6.0);
	valueTest("mysum2(1,2,3,4)", 10.0);
	valueTest("mysum2(1,2,3,4,5)", 15.0);

	try {
	    valueTest("x=6.0", 6.0);
	    valueTest("postDec(x)", 6.0); // CallbackEvaluationI
	    valueTest("x", 5.0);
	    assertTrue(hasCallback);
	} catch (JepException e) {
	    assertFalse(hasCallback);
	}
    }
}
