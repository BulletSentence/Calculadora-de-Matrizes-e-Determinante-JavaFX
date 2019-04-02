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
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.Collection;
import java.util.Iterator;
import java.util.List;
import java.util.Map.Entry;
import java.util.Set;
import java.util.Stack;
import java.util.Vector;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.EmptyOperatorTable.OperatorKey;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Evaluator;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableFactory;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.bigdecimal.BigDecComponents;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.TernaryOperator;
import com.singularsys.jep.configurableparser.matchers.IdentifierTokenMatcher;
import com.singularsys.jep.configurableparser.matchers.NumberTokenMatcher;
import com.singularsys.jep.configurableparser.matchers.RegExpTokenMatcher;
import com.singularsys.jep.configurableparser.tokens.NumberToken;
import com.singularsys.jep.configurableparser.tokens.Token;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.IllegalParameterException;
import com.singularsys.jep.functions.LazyLogical;
import com.singularsys.jep.functions.Logical;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.functions.Round;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.functions.VSum;
import com.singularsys.jep.misc.CaseInsensitiveFunctionTable;
import com.singularsys.jep.misc.bitwise.BitwiseOperatorTable;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.IsNull;
import com.singularsys.jep.misc.functions.Switch;
import com.singularsys.jep.misc.functions.SwitchDefault;
import com.singularsys.jep.misc.functions.ToBase;
import com.singularsys.jep.misc.javaops.JavaOperatorTable;
import com.singularsys.jep.misc.javaops.TernaryConditional;
import com.singularsys.jep.misc.nullwrapper.NullWrappedFunctionTable;
import com.singularsys.jep.misc.nullwrapper.NullWrappedOperatorTable;
import com.singularsys.jep.parser.ASTConstant;
import com.singularsys.jep.parser.ASTOpNode;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.Node.HookKey;
import com.singularsys.jep.parser.SimpleNode;
import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.standard.StandardFunctionTable;
import com.singularsys.jep.walkers.DoNothingVisitor;
import com.singularsys.jep.walkers.PrefixTreeDumper;
import com.singularsys.jep.walkers.SubstitutionVisitor;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * This class is intended to contain all tests related to reported bugs.
 * 
 * @author Nathan Funk
 */
@SuppressWarnings("nls")
public class BugsTest {
	private Jep jep;


	@Before
	public void setUp() {
		// Set up the parser
		jep = new Jep();
		jep.setImplicitMul(true);
	}

	/**
	 * Tests a bug that lead the FractalCanvas example to fail.
	 * (09/04/2007)
	 */
	@Test
	public void testFractalBug()  throws Exception {
		System.out.println("Testing FractalCanvas bug...");

		Complex c;

		//Init Parser
		jep = new Jep();

		//Add and initialize x to (0,0)
		jep.addVariable("x", new Complex(0, 0));

		//Parse the new expression
		try {
			jep.parse("x");
		} catch (ParseException e) {
			fail("Error while parsing: "+e.getMessage());
		}
		//Reset the values
		jep.addVariable("x", new Complex(1, 1));
		//z.set(0,0);
		//System.out.println("x= " + jep.getVarValue("x"));

		Object value;

		try {
			value = jep.evaluate();
		} catch (EvaluationException e) {
			fail("Error during evaluation: "+e.getMessage());
			return;
		}

		System.out.println("result = " + value);
		assertTrue(value instanceof Complex);
		c = (Complex)value;
		assertTrue(c.re() == 1);
		assertTrue(c.im() == 1);
	}


	/**
	 * Tests bug [ 1585128 ] setAllowUndeclared does not work!!!
	 * setAllowedUndeclared should add variables to the symbol table.
	 * 
	 * This test parses the expression "x" and checks whether only the
	 * variable x is in the symbol table (no more no less)
	 */
	@Test
	public void testSetAllowUndeclared() {
		System.out.println("Testing setAllowUndeclared...");
		jep.getVariableTable().clear();				// clear the variable table
		jep.setAllowUndeclared(true);
		try {
			jep.parse("x");
		} catch (ParseException e) {
			fail();
		}
		VariableTable vt = jep.getVariableTable();

		// should only contain a single variable x
		assertTrue(vt.size()==1);
		assertTrue(vt.getVariable("x") != null);
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
		assertTrue(result.equals(new Complex(-2, 0)));
	}*/

	/**
	 * Tests [ 1563324 ] getValueAsObject always return null after an error
	 * 
	 * Jep 2.4.0 checks the <code>errorList</code> variable before evaluating 
	 * an expression if there is an error in the list, null is returned. This
	 * behaviour is bad because errors are added to the list by
	 * getValueAsObject. If the first evaluation fails (after a successful parse)
	 * then an error is added to the list. Subsequent calls to getValueAsObject
	 * fail because there is an error in the list.
	 */
	//	@Test
	//	public void testBug1563324() {
	//		jep.initSymTab();
	//		jep.setAllowUndeclared(true);
	//		// parse a valid expression
	//		jep.parseExpression("abs(x)");
	//		// add a variable with a value that causes evaluation to fail
	//		// (the Random type is not supported by the abs function)
	//		jep.addVariable("x", new java.util.Random()); 
	//		Object result = jep.getValueAsObject();
	//		// evaluation should have failed
	//		assertTrue(jep.hasError());
	//		
	//		// change the variable value to a value that should be evaluated
	//		jep.addVariable("x", -1);
	//		// ensure that it is evaluated correctly
	//		result = jep.getValueAsObject();
	//		assertTrue((result instanceof Double) && ((Double)result).doubleValue() == 1.0);
	//	}

	/**
	 * Tests bug 49. Adding an operator such as "AND" does not work. Instead
	 * of being interpreted as and operator it is parsed as a variable.
	 */
	@Test
	public void testBug49() {
		System.out.println("Testing bug 49...");
		//set configurable parser
		ConfigurableParser cp = new StandardConfigurableParser();
		Jep j = new Jep(cp);

		// alter operator table
		OperatorTable2 ot = (OperatorTable2) j.getOperatorTable();
		Operator andOp = new Operator("AND", new Logical(0), Operator.BINARY+Operator.LEFT+Operator.ASSOCIATIVE);
		ot.replaceOperator(ot.getAnd(),andOp);
		j.reinitializeComponents();

		try {
			// parse a simple expression
			j.parse("1 AND 1");
			Node n = j.getLastRootNode();
			System.out.println(n.getClass().toString());

			// should be a single operator node with two children
			JepTest.nodeTest(n, andOp);
			assertEquals(2, n.jjtGetNumChildren());
			// children should be constants with no children
			JepTest.nodeTest(n.jjtGetChild(0), new Double(1));
			JepTest.nodeTest(n.jjtGetChild(1), new Double(1));

			// try evaluating the expression
			Object result = j.evaluate();
			assertTrue(result instanceof Boolean);
		} catch (Exception e) {
			// some other exception was thrown
			System.out.println(e.getMessage());
			//e.printStackTrace();
			fail();
		}
	}

	/**
	 * Test bug #72 Handling of Constants. Sets a constant and then tries to
	 * set the value again. The bug was that addVariable failed silently, and
	 * the fix was to add exception throwing to addVariable.
	 */
	@Test
	public void testBug72() {
		System.out.println("Testing bug 72 Handling of Constants...");
		Jep j = new Jep();

		// try adding a constant
		try {
			j.addConstant("x", 1.0);
		} catch (JepException e) {
			fail("addConstant failed though it shouldn't.");
		}

		try {
			j.addVariable("x", 1.0); //should fail
			fail("addVariable call didn't throw an exception as expected.");
		} catch (JepException e) {
			// this is ok, since we want an exception to be thrown in this case
		}
	}


	/*  This test was based on a user forum question. The expression supplied is
	 *  invalid due to 0,5 not having a "." instead of a "," and some if statements
	 *  only having two arguments. This test can likely be removed.
	@Test
	public void test2008_08_14() throws Exception {
	    jep = new Jep(new StandardConfigurableParser());
	String eqn = "A_N_E =\n"+
	"	if (CT == 1,\n"+
	"		(L_c / L_u) -1,\n"+
	"	if (CT == 2,\n"+
	"		if (TP == 1,\n"+
	"			0.46,\n"+
	"		if (TP == 2,\n"+
	"			(84/(L * PE(81/(L + 0.5)))) -1,\n"+
	"		if (TP == 3,\n"+
	"			(81/(L * PE(78/(L + 0.5)))) -1,\n"+
	"	if (CT == 3,\n"+
	"		if (TP == 1 || 2,\n"+
	"			0.46,\n"+
	"		if (TP == 3,\n"+
	"			1- L_c/ (PE(L_B/(L + 0,5)) * L ),\n"+
	"	0))))))))";

	jep.parse(eqn);
	}
	 */

	@Test
	public void test2008_08_14_switch() throws Exception {
		jep = new Jep(new StandardConfigurableParser());
		jep.addFunction("switchd",new SwitchDefault());
		jep.addFunction("switch",new Switch());
		String eqn = 
				"A_N_E =\n"+
						" switchd(CT,\n"+
						"  (L_c / L_u) -1,\n"+
						"  switch(TP,\n"+
						"   0.46,\n"+
						"   (84/(L * PE(81/(L + 0.5)))) -1,\n"+
						"	(81/(L * PE(78/(L + 0.5)))) -1\n"+
						"  ),\n"+
						"  switch(TP,\n"+
						"   0.46,\n"+
						"   0.46,\n"+
						"   1- L_c/ (PE(L_B/(L + 0.5)) * L )\n"+
						"  ),\n"+
						"  0)";

		jep.parse(eqn);
	}

	@Test
	public void testToBase() throws Exception {
		jep.addFunction("toBin", new ToBase(2));
		jep.addVariable("x",Integer.valueOf(-9));
		String eqn = "toBin(x)";
		String res = (String) jep.evaluate(jep.parse(eqn));
		assertEquals(eqn,"-1001",res);
	}

	@Test
	public void testBug2008_09_12() {
		jep.setAllowUndeclared(false);
		try {
			jep.parse( "v0 / 1000000000" );
			fail("Should have a parse exception with undeclared variable");
		} catch (ParseException e) {
			System.out.println("expected exception caught "+e.getMessage());
		}
	}

	@Test
	public void testBug84() throws Exception {
		String eqn = "if(stringVariable==\"someString\",1.0,0.0)";
		Node testNode = jep.parse(eqn); 
		String out = jep.toString(testNode);
		assertEquals(eqn,out);
	}

	@Test
	public void testBug28() throws Exception {
		String eqn = "x+y";
		String rep = "z^2";
		Node n1 = jep.parse(eqn);
		Node n2 = jep.parse(rep);
		SubstitutionVisitor sv = new SubstitutionVisitor(jep);
		Node n3 = sv.substitute(n1,"x",n2);
		String res = jep.toString(n3);
		assertEquals("z^2.0+y",res);
	}

	@Test
	public void test2008_12_13() throws Exception {
		HookKey key1 = new HookKey() {/* empty */ };
		HookKey key2 = new HookKey() {/* empty */ };
		ASTConstant astConstant = jep.getNodeFactory().buildConstantNode("value");
		astConstant.setHook(key1, "value1");

		//The call to setHook second time throws ArrayIndexOutOfBoundsException
		//NOTE: IBJepHooks implements HookKey
		astConstant.setHook(key2, "value2");

		assertEquals("value1",astConstant.getHook(key1));
		assertEquals("value2",astConstant.getHook(key2));
	}

	@Test
	public void test2009_02_02() throws Exception {
		jep.addFunction("contains",new BinaryFunction() {
			private static final long serialVersionUID = 1L;

			@Override
			public Object eval(Object l, Object r) throws EvaluationException {
				String ls = (String) l;
				String rs = (String) r;
				System.out.println(ls);
				System.out.println(rs);
				return ls.contains(rs);
			}} );
		jep.setComponent(new StandardConfigurableParser());
		String s = "contains (\"AB01,AB02,AB03\", \"AB02,AB03\")";
		Node n = jep.parse(s);
		Object res = jep.evaluate(n);
		System.out.println(res);
	}

	@Test
	public void test2009_02_16() {
		jep.setComponent(new StandardConfigurableParser());
		OperatorTable2 ot = ((OperatorTable2)jep.getOperatorTable());
		TernaryOperator op = new TernaryOperator("cond", "?", ":", 
				new TernaryConditional(), 
				Operator.TERNARY+Operator.NARY+Operator.LEFT);
		ot.insertOperator(new OperatorKey(){/* empty */ }, 
				op,ot.getAssign());
	}

	@Test
	public void testBug116() throws Exception {
		jep.setComponent(new FastEvaluator());
		//jep.setComponent(new StandardEvaluator());
		String expr = "[[1],[2]]";
		jep.parse(expr);
		try
		{
			jep.evaluate();
		}
		catch (Exception e)
		{
			Assert.fail("Evaluation of \""+expr+"\" failed.");
		}
	}

	static class OperatorUMinus extends PostfixMathCommand {
		private static final long serialVersionUID = 1L;
		@Override
		public void run(Stack<Object> aStack) throws EvaluationException {/* empty */  }
	}
	static class OperatorAdd extends PostfixMathCommand {
		private static final long serialVersionUID = 1L;
		@Override
		public void run(Stack<Object> aStack) throws EvaluationException { /* empty */ }
	}
	static class OperatorSubtract extends PostfixMathCommand {
		private static final long serialVersionUID = 1L;
		@Override
		public void run(Stack<Object> aStack) throws EvaluationException { /* empty */ }
	}
	static class OperatorMultiply extends PostfixMathCommand {
		private static final long serialVersionUID = 1L;
		@Override
		public void run(Stack<Object> aStack) throws EvaluationException { /* empty */ }
	}
	static class OperatorDivide extends PostfixMathCommand {
		private static final long serialVersionUID = 1L;
		@Override
		public void run(Stack<Object> aStack) throws EvaluationException { /* empty */ }
	}

	/*
	 * Code which uses the old OperatorTable which will always break. see #121 
    static class JepExpressionOperatorTable extends OperatorTable
    {
        private static final long serialVersionUID = 1L;

        JepExpressionOperatorTable()
        {
            setNumOps(5);

            addOperator(OP_NEGATE,new Operator("UMinus","-",new OperatorUMinus(),Operator.UNARY+Operator.RIGHT+Operator.PREFIX+Operator.SELF_INVERSE));
            addOperator(OP_ADD,new Operator("+",new OperatorAdd(),Operator.BINARY+Operator.LEFT+Operator.COMMUTATIVE+Operator.ASSOCIATIVE));
            addOperator(OP_SUBTRACT,new Operator("-",new OperatorSubtract(),Operator.BINARY+Operator.LEFT+Operator.COMPOSITE+Operator.USE_BINDING_FOR_PRINT));
            addOperator(OP_MULTIPLY,new Operator("*",new OperatorMultiply(),Operator.BINARY+Operator.LEFT+Operator.COMMUTATIVE+Operator.ASSOCIATIVE));
            addOperator(OP_DIVIDE,new Operator("/",new OperatorDivide(),Operator.BINARY+Operator.LEFT+Operator.COMPOSITE));

            setPrecedenceTable(new int[][] 
                                         {   
                                             {OP_POWER},
                                             {OP_NEGATE,OP_UPLUS,OP_NOT},
                                             {OP_MULTIPLY,OP_DIVIDE,OP_MOD,OP_DOT,OP_CROSS},
                                             {OP_ADD,OP_SUBTRACT},
                                             {OP_LT,OP_LE,OP_GT,OP_GE},
                                             {OP_EQ,OP_NE},
                                             {OP_AND},
                                             {OP_OR},
                                             {OP_ASSIGN},
                                         });

            this.setStandardOperatorRelations();
        }
    }

    @Test
    public void testBug121() throws Exception {
        Jep jep = new Jep();
        jep.setComponent(new JepExpressionOperatorTable());
        //jep.setComponent(new StandardEvaluator());
    }
	 */
	static class JepExpressionOperatorTable2 extends EmptyOperatorTable
	{
		private static final long serialVersionUID = 1L;

		JepExpressionOperatorTable2()
		{
			Set<Entry<OperatorKey, Operator>> es = this.entrySet();
			es.clear();

			addOperator(OperatorTable2.BasicOperators.NEG,new Operator("UMinus","-",new OperatorUMinus(),Operator.UNARY+Operator.RIGHT+Operator.PREFIX+Operator.SELF_INVERSE));
			addOperator(OperatorTable2.BasicOperators.ADD,new Operator("+",new OperatorAdd(),Operator.BINARY+Operator.LEFT+Operator.COMMUTATIVE+Operator.ASSOCIATIVE));
			addOperator(OperatorTable2.BasicOperators.SUB,new Operator("-",new OperatorSubtract(),Operator.BINARY+Operator.LEFT+Operator.COMPOSITE+Operator.USE_BINDING_FOR_PRINT));
			addOperator(OperatorTable2.BasicOperators.MUL,new Operator("*",new OperatorMultiply(),Operator.BINARY+Operator.LEFT+Operator.COMMUTATIVE+Operator.ASSOCIATIVE));
			addOperator(OperatorTable2.BasicOperators.DIV,new Operator("/",new OperatorDivide(),Operator.BINARY+Operator.LEFT+Operator.COMPOSITE));

			setPrecedenceTable(new OperatorKey[][] 
					{   
					{OperatorTable2.BasicOperators.NEG},
					{OperatorTable2.BasicOperators.MUL,OperatorTable2.BasicOperators.DIV},
					{OperatorTable2.BasicOperators.ADD,OperatorTable2.BasicOperators.SUB},
					});

			this.setStandardOperatorRelations();
		}
	}

	/**
	 * Tests a custom operator table with only 5 operators, see bug #121
	 * @throws Exception
	 */
	@Test
	public void testBug121A() throws Exception {
		jep.setComponent(new JepExpressionOperatorTable2());
		//jep.setComponent(new StandardEvaluator());
	}

	class myRound extends UnaryFunction
	{
		private static final long serialVersionUID = 1L;

		@Override
		public Object eval(Object arg) throws EvaluationException {
			double val = this.asDouble(0, arg);
			return Double.valueOf(Math.round(val));
		}

	}

	@Test
	public void testBug122() throws Exception {
		// This breaks on the parse() call
		jep = new Jep();
		FunctionTable oldFT = jep.getFunctionTable();
		jep.setComponent(new CaseInsensitiveFunctionTable());
		for(Entry<String, PostfixMathCommandI> ent:oldFT.entrySet()) {
			jep.addFunction(ent.getKey(), ent.getValue());
		}
		//jep.addFunction("if",new If());
		jep.parse("if(1>0,2,3)");
		System.out.println(jep.evaluate());
		jep.parse("If(1>0,2,3)");
		System.out.println(jep.evaluate());
	}

	@Test
	public void testBug123() throws Exception {
		jep.addFunction("round", new myRound());
		Double res = (Double) jep.evaluate(jep.parse("round(1.5)"));
		assertEquals("round(1.5) = "+res,2.0d,res.doubleValue(),0.1);

		res = (Double) jep.evaluate(jep.parse("round(2.5)"));
		assertEquals("round(2.5) = "+res,3.0d,res.doubleValue(),0.1);

		System.out.println(java.lang.Math.round(1.5));
		System.out.println(java.lang.Math.round(2.5));
		System.out.println(java.lang.Math.rint(1.5));
		System.out.println(java.lang.Math.rint(2.5));
	}

	@Test
	public void testBug30_10_09() throws Exception {
		{
			jep.addFunction("isNull", new IsNull());
			((FastEvaluator) jep.getEvaluator()).setTrapNullValues(false);
			((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
			Double y = new Double(5);           

			jep.addVariable("x",null);
			jep.addVariable("y", y);
			jep.addVariable("z", 50000);

			Node node = jep.parse("isNull(x)");
			System.out.println(jep.evaluate(node));
			node = jep.parse("isNull(y)");
			System.out.println(jep.evaluate(node));
			node = jep.parse("isNull(w)");
			System.out.println(jep.evaluate(node));
		}
	}

	@Test
	public void testBug23_11_09() {

		String f1 = "min(1)";
		String f2 = "min(2)";
		String f3 = "min(1,2)";
		String f4 = "max(min(1),min(2))";
		String f5 = "min(min(1),min(2))";

		testMU(f1);
		testMU(f2);
		testMU(f3);
		testMU(f4);
		testMU(f5);
	}

	private static void testMU(String formula) {

		try {

			Jep jep = new Jep();
			jep.parse(formula);
			Double d = (Double) jep.evaluate();
			System.out.println(d);
		} catch (Exception e) {

			System.out.println(e);
		}
	}

	@Test
	public void testDottedIdentifiers() throws ParseException {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		cp.setImplicitMultiplicationSymbols("(","[");

		// Sets it up for identifiers with dots in them.
		cp.addTokenMatcher(IdentifierTokenMatcher.dottedIdentifierMatcher());

		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");

		// Construct the Jep instance and set the parser
		jep = new Jep(cp);

		// Remove the dot operator
		((OperatorTable2) jep.getOperatorTable()).removeOperator(jep.getOperatorTable().getDot());
		//notify other components of change in operator table
		jep.reinitializeComponents();

		Node n = jep.parse("a.b=c.d");
		Node n2 = n.jjtGetChild(0);
		assertEquals("a.b",n2.getName());

	}

	@Test
	public void test31Jan2010() throws JepException {
		jep.addFunction("vsum",new VSum());
		String formula = "vsum(x)"; 
		Node node = jep.parse(formula); 
		SubstitutionVisitor sv = new SubstitutionVisitor(jep); 
		Node node1 = jep.parse("x = [1, 2]"); 
		Node substitute = sv.substitute(node, node1); 
		Object result = jep.evaluate(substitute); 
		System.out.println(result); 

		jep.addVariable("x", new Vector<Object>(Arrays.asList(3.0,2.0,4.0)));
		result = jep.evaluate(node); 
		System.out.println(result); 
	}

	@Test
	public void testNamesWithSpaces() throws JepException {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		//cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		//cp.setImplicitMultiplicationSymbols("(","[");

		// Sets it up for identifiers with spaces in them.
		// Identifiers must start with a letter or underscore
		// can contain letters numbers underscore and space
		// and end with letters numbers underscore
		cp.addTokenMatcher(new IdentifierTokenMatcher("[a-zA-Z_][a-zA-Z_0-9 ]*\\w"));
		cp.addTokenMatcher(new IdentifierTokenMatcher("[a-zA-Z]")); // allow single letter names
		cp.addTokenMatcher(new IdentifierTokenMatcher("\"[^\"]+\"")); 
		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");

		// Construct the Jep instance and set the parser
		jep = new Jep(cp);
		jep.getOperatorTable().getSubtract().addAltSymbol("–");
		jep.getOperatorTable().getUMinus().addAltSymbol("–");
		jep.setImplicitMul(false);
		jep.reinitializeComponents();
		
		Node n = jep.parse("Interval * My Value");
		Node n2 = n.jjtGetChild(1);
		assertEquals("My Value",n2.getName());

		String longName = "\"Total % Busy - (Cpu_1)\"";
		n = jep.parse("Two space name * "+ longName);
		Node n1 = n.jjtGetChild(0);
		assertEquals("Two space name",n1.getName());
		n2 = n.jjtGetChild(1);
		assertEquals(longName,n2.getName());
		
		
		String s4 = "(GD SoCal BR – 0.0015) * 0.85 + (NGI SoCal BR + 0.005) * 0.15";
		Node n4 = jep.parse(s4);
		assertNotNull(jep.getVariable("GD SoCal BR"));
		assertNotNull(jep.getVariable("NGI SoCal BR"));
		(new PrefixTreeDumper()).dump(n4);
	}


	/** 
	 * Test for non latin variable names
	 * @throws ParseException
	 */
	@Test
	public void testChinese() throws ParseException {
		ConfigurableParser cp = new ConfigurableParser();

		cp.addHashComments();
		cp.addSlashComments();
		cp.addSingleQuoteStrings();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		cp.setImplicitMultiplicationSymbols("(","[");
		//cp.addIdentifiers();
		//cp.addTokenMatcher(new IdentifierTokenMatcher("\\p{javaJavaIdentifierStart}\\p{javaJavaIdentifierPart}*"));
		cp.addTokenMatcher(new IdentifierTokenMatcher("[\\p{L}_][\\p{L}\\p{N}_\\.]*"));
		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");


		jep = new Jep(cp);
		String s = "A\u901aB\u3400C\u1820D\ua007E='test'";

		Node node = jep.parse(s);
		assertEquals("A\u901aB\u3400C\u1820D\ua007E",node.jjtGetChild(0).getName());
		
		String  expression = "产品A + 1";
		node = jep.parse(expression);
	}

	@Test
	public void testVectorExpressionBug_Version_3_30() throws Exception {
		Node node = jep.parse("[min([A, 20])]");
		jep.addVariable("A", 2.0);
		Object result = jep.evaluate(node);

		assertNotNull(result);
		assertArrayEquals(new Object[]{2.0}, ((List<?>) result).toArray());
	}

	@Test
	public void testBigDecRoundMode() throws Exception {
		jep = new Jep(new BigDecComponents(MathContext.DECIMAL64));
		jep.getFunctionTable().remove("round");
		jep.addFunction("round", new Round());
		//    Jep jep = new Jep(new BigDecComponents(new MathContext(1,
		//            RoundingMode.HALF_EVEN)));
		jep.addVariable("A", new BigDecimal("57.820"));
		jep.addVariable("B", new BigDecimal("1.09208"));
		jep.addVariable("C", new BigDecimal("0.02976"));
		jep.parse("round((A+50*B+2500*C)/7.5,1)");
		Object result = jep.evaluate();
		assertEquals(24.9,result);
		System.out.println(result);
	}

	/**
	 * Tests for other symbols used.
	 * Minus sign is actually  unicode U+2013
	 * @throws Exception
	 */
	@Test
	public void testAlternateSymbols() throws Exception {
		jep.setComponent(new StandardConfigurableParser());

		jep.getOperatorTable().getSubtract().addAltSymbol("\u2013"); // en dash
		jep.getOperatorTable().getUMinus().addAltSymbol("\u2013");
		jep.getOperatorTable().getSubtract().addAltSymbol("\u2212"); // unicode minus symbol
		jep.getOperatorTable().getUMinus().addAltSymbol("\u2212");
        jep.getOperatorTable().getMultiply().addAltSymbol("\u00d7");


		jep.reinitializeComponents();
		//	   String formule = "S = if(C == 1, (1 + a_n_e) * (1 + mi) * (1 + p_f_c) � 1), if(C == 2, a_n_e + m + p_f_c, if(C == 3, p_b + mi + p_f_c, 0)))";
		//	   String substr = formule.substring(50);
		//	   System.out.println(substr);
		String formule = "S = \u2212 1";
		jep.parse(formule);
		//            jep.addVariable("S_a_e", 0);
		//          jep.addVariable("C", 1);        jep.getOperatorTable().getMultiply().addAltSymbol("\u00d7");

		//            jep.addVariable("a_n_e", 1);
		//            jep.addVariable("mi", 1);
		//            jep.addVariable("p_f_c", 1);
		Object result = jep.evaluate();
		System.out.println("S_a_e = " + result);
	}

	//23June11
	@Test
	public void testChangeOperatorSymbols() throws Exception {
		jep = new Jep(new StandardConfigurableParser()); 
		// make the jep recognise your syntax
		// Get the existing operator
		Operator eq1=jep.getOperatorTable().getEQ();
		//jep.getOperatorTable().getAssign().setSymbol(":=");
		// Create a new equality operator with the symbol =, the same function and the same set of flags
		Operator eq2 = new Operator("=",eq1.getPFMC(),eq1.getFlags());
		// add it with the same precedence level
		((OperatorTable2)jep.getOperatorTable()).addOperator(new OperatorKey(){/*empty*/},eq2,eq1);

		((OperatorTable2)jep.getOperatorTable()).removeOperator(OperatorTable2.SpecialOperators.ASSIGN);

		jep.getOperatorTable().getOr().setSymbol("OR"); 
		jep.getOperatorTable().getAnd().setSymbol("AND"); 
		jep.getOperatorTable().getOr().setPrintSymbol(" OR "); 
		jep.getOperatorTable().getAnd().setPrintSymbol(" AND "); 
		jep.addFunction("isNull",new IsNull());
		((FastEvaluator)jep.getEvaluator()).setTrapNullValues(false);
		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		jep.reinitializeComponents(); 

		jep.parse("x=3");
		jep.addVariable("x", 3.0);
		assertTrue((Boolean) jep.evaluate());
		jep.addVariable("x", 4.0);
		assertFalse((Boolean) jep.evaluate());
		jep.parse("x==3");
		jep.addVariable("x", 3.0);
		assertTrue((Boolean) jep.evaluate());
		jep.addVariable("x", 4.0);
		assertFalse((Boolean) jep.evaluate());

		String s = "((A=name)AND(isNull(B) OR(B=address)))"; 
		// still need quotes round strings though 
		String s1 = s.replaceAll("=([^\\)]*)\\)","=\"$1\")"); 
		System.out.println(s1); 
		Node node = jep.parse(s1); 
		jep.println(node); 
		// Here is what I wanted to do. 
		// I know jep will fail, it supposed to be behave that way. I don't 
		//blame it. 
		// Now I wanted to change it. How can I do it. 
		jep.addVariable("A", "name"); 
		Object result = jep.evaluate(); 

		if (result.equals(true) ) { 
			System.out.println("Campaigns are matched for the above mentioned"); 
		} else { 
			System.out.println("Campaigns does not matched for the above mentioned"); 
		} 
		assertEquals(s,Boolean.TRUE,result);
	} 

	@Test
	public void testAlternateSymbolBug() throws Exception {
		jep = new Jep(new StandardConfigurableParser()); 
		Operator eq1=jep.getOperatorTable().getEQ();
		eq1.addAltSymbol("=");
		((OperatorTable2)jep.getOperatorTable()).removeOperator(OperatorTable2.SpecialOperators.ASSIGN);
		jep.reinitializeComponents(); 

		jep.parse("x=3");
		jep.println();
		jep.addVariable("x", 3.0);
		assertTrue((Boolean) jep.evaluate());
		jep.addVariable("x", 4.0);
		assertFalse((Boolean) jep.evaluate());

		jep.parse("x==3");
		jep.println();
		jep.addVariable("x", 3.0);
		assertTrue((Boolean) jep.evaluate());
		jep.addVariable("x", 4.0);
		assertFalse((Boolean) jep.evaluate());
	}

	/**
     * See <a href="https://ar.trac.cvsdude.com/jep/ticket/167">ticked 167</a>
	 * 
	 * @throws Exception
	 */
	@Test
	public void test167() throws Exception {
		jep = new Jep(new StandardConfigurableParser()); 
		jep.getOperatorTable().getOr().setSymbol("OR"); 
		jep.getOperatorTable().getAnd().setSymbol("AND"); 
		jep.getOperatorTable().getOr().setPrintSymbol(" OR "); 
		jep.getOperatorTable().getAnd().setPrintSymbol(" AND "); 
		jep.getOperatorTable().getEQ().setSymbol("="); 
		jep.getOperatorTable().getAssign().setSymbol(":="); 
		jep.reinitializeComponents(); 
		String s = "A=X AND B=Y"; 
		Node node = jep.parse(s); 
		String r = jep.toString(node); 
		System.out.println(r);
		assertEquals(s,"A=X AND B=Y",r);

		s = "(CAR_MAKE=SAAB)AND(CAR_MAX=100000 OR CAR_MAX=200000 OR CAR_MIN=1000 OR CAR_MIN=10000)";
		node = jep.parse(s); 
		r = jep.toString(node); 
		System.out.println(r);
		assertEquals(s,"CAR_MAKE=SAAB AND (CAR_MAX=100000.0 OR CAR_MAX=200000.0 OR CAR_MIN=1000.0 OR CAR_MIN=10000.0)",r);

		//        jep.getOperatorTable().getOr().setPrintSymbol(null); 
		//        jep.getOperatorTable().getAnd().setPrintSymbol(null); 
		//        r = jep.toString(node); 
		//        System.out.println(r);
	}


	static class CarWalker extends DoNothingVisitor {
		private static final long serialVersionUID = 1L;

		@Override
		public Object visit(ASTOpNode node, Object data) throws JepException {

			if( node.getOperator().getSymbol().equals("=") &&
					( node.jjtGetChild(0) instanceof ASTVarNode ) && 
					( node.jjtGetChild(0).getName().equals("CAR_MAKE") ) &&
					( node.jjtGetChild(1) instanceof ASTConstant ) )
			{ System.out.println(node.jjtGetChild(1)); }
			return super.visit(node, data);
		}

	}

	@Test
	public void testCarWalk() throws Exception {
		jep = new Jep(new StandardConfigurableParser());
		jep.getOperatorTable().getEQ().setSymbol("=");
		jep.getOperatorTable().getAssign().setSymbol(":=");
		jep.getOperatorTable().getOr().setSymbol("OR");
		jep.getOperatorTable().getAnd().setSymbol("AND");
		jep.getOperatorTable().getOr().setPrintSymbol(" OR "); 
		jep.getOperatorTable().getAnd().setPrintSymbol(" AND "); 

		jep.reinitializeComponents();

		String s = "(((CAR_MAKE=audi)OR(CAR_MAKE=volvo)OR(CAR_MAKE=vauxhall)OR(CAR_MAKE=toyota)" +
				"OR(CAR_MAKE=seat)OR(CAR_MAKE=renault)OR(CAR_MAKE=peugeot)OR" +
				"(CAR_MAKE=merced es)OR(CAR_MAKE=mercedes-"+ 
				"benz)OR(CAR_MAKE=mazda)OR(CAR_MAKE=hyundai)OR(CAR_MAKE=honda)OR(CAR_MAKE=fo rd)" +
				"OR(CAR_MAKE=fiat)OR(CAR_MAKE=bmw)OR(CAR_MAKE=chevrolet))OR((CAR_MODEL=ri o)" +
				"OR(CAR_MODEL=VENGA)OR(CAR_MODEL=ceed))) ";
		String s1 = s.replaceAll("=([^\\)]*)\\)","=\"$1\")");
		System.out.println(s1);
		Node node = jep.parse(s1);
		jep.println(node);

		CarWalker cw = new CarWalker();
		cw.init(jep);
		cw.visit(node);
		s = "(CAR_MAKE=SAAB)AND((CAR_MAX=100000)OR(CAR_MAX=200000))OR((CAR_MIN=1000)OR(CAR_MIN=10000))";
		node = jep.parse(s);
		((SimpleNode) node).dump("");

	}

	@Test
	public void testPrefixOperators() throws Exception {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		cp.addSingleQuoteStrings();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		cp.setImplicitMultiplicationSymbols("(","[");
		cp.addTokenMatcher(new IdentifierTokenMatcher("\\&\\&|\\|\\||[a-zA-Z_]\\w*"));
		//cp.addIdentifiers();
		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");

		jep = new Jep(cp);
		OperatorTableI ot = jep.getOperatorTable();
		((OperatorTable2)ot).removeOperator(ot.getAnd());
		((OperatorTable2)ot).removeOperator(ot.getOr());
		jep.addFunction("&&",new LazyLogical(LazyLogical.AND));
		jep.addFunction("||",new LazyLogical(LazyLogical.OR));
		jep.reinitializeComponents();
		//Node n = jep.parse("|| (true , false )");
		Node n = jep.parse("|| (numOptions> 1 , numBarriers== \"1/0\" ) ");
		Node n2 = jep.parse("&& (hasTableValues, || (numOptions> 1 , numBarriers== \"1/0\" ) )");
		PrefixTreeDumper ptd = new PrefixTreeDumper();
		ptd.dump(n);
		ptd.dump(n2);
	}

	@Test
	public void testNullWrapMin() throws JepException {
		jep.setComponent(new NullWrappedFunctionTable(new StandardFunctionTable()));
		jep.setComponent(new NullWrappedOperatorTable(new JavaOperatorTable(), true));
		//    jep.setComponent(new JavaOperatorTable());
		jep.setComponent(new StandardConfigurableParser());
		jep.parse("min(4, 5)");
		System.out.println(jep.evaluateD());
		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		((FastEvaluator)jep.getEvaluator()).setTrapNullValues(false);
		jep.parse("1 == 1 ? 1 : 0");
		System.out.println(jep.evaluate());
		jep.addVariable("null", null);
		jep.addFunction("myRound",new myRound());
		jep.parse("myRound(null)");
		jep.evaluate();
		try {
			jep.parse("myRound([1,2])");
			jep.evaluate();
		} catch(IllegalParameterException e) {
			assertEquals("myRound",e.getFunctionName());
		}

	}

	@Test
	public void test1_9_11() throws ParseException, Exception {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		cp.setImplicitMultiplicationSymbols("(","[");

		// Sets it up for identifiers to be sequence  {.*} 
		cp.addTokenMatcher(new IdentifierTokenMatcher("\\{[^\\{]*\\}"));
		// Adds normal identifiers
		cp.addIdentifiers();

		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");

		// Construct the Jep instance and set the parser
		jep = new Jep(cp);

		jep.parse("{Dollar Sales:i}/{Dollar Sales:SUM}");
		jep.setVariable("{Dollar Sales:i}",100);
		jep.setVariable("{Dollar Sales:SUM}",1000);
		double res = jep.evaluateD();
		System.out.println(res);
	}

	@Test
	public void test31_8_11() throws Exception {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",",");
		cp.setImplicitMultiplicationSymbols("(");

		// Adds normal identifiers
		cp.addIdentifiers();

		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")");
		cp.addFunctionMatcher("(",")",",");
		cp.addListMatcher("[","]",",");
		cp.addArrayAccessMatcher("[","]");


		// Construct the Jep instance and set the parser
		jep = new Jep(cp);

		//TODO Still broken. How to fix parsing to allow abstract array access? 
		//jep.parse("sin(x)[1]");
		//double res = jep.evaluateD();
		//System.out.println(res);

		//jep.parse("([1,2]+[3,4])[2]");
	}

	/**
	 * Returns true if n1 depends on n2. That is if one of the variables on the 
	 * rhs of n1 is the lhs variable of n2.
	 */
	boolean dependsOn(Node n1,Node n2) throws JepException {
		OperatorTableI ot = jep.getOperatorTable();
		//boolean isEqn1 = ot.getAssign().equals(n1.getOperator());
		boolean isEqn2 = ot.getAssign().equals(n2.getOperator());
		//boolean isAssign1 = isEqn1 && (n1.jjtGetChild(0) instanceof ASTVarNode);
		boolean isAssign2 = isEqn2 && (n2.jjtGetChild(0) instanceof ASTVarNode);
		//String n1lhs = isAssign1 ? n1.jjtGetChild(0).getName() : "";
		String n2lhs = isAssign2 ? n2.jjtGetChild(0).getName() : "";

		TreeAnalyzer ta = new TreeAnalyzer(n1.jjtGetChild(1));
		List<String> n1rhs = Arrays.asList(ta.getVariableNames());
		return n1rhs.contains(n2lhs);
	}
	

	/**
	 * Tests use of TreeAnalyzer to work out the order of expressions.
	 * @throws ParseException
	 * @throws Exception
	 */
	@Test
	public void testExpressionOrdering() throws ParseException, Exception {
		jep = new Jep();
		// setup equations to be parsed
		String[] eqns = new String[]{
				"Var4 = Var1 + Var3",
				"Var5 = Var4 + 1",
				"Var3 = 3 * Var2"
		};
		// parse each expression
		Node[] nodes = new Node[eqns.length];
		for(int i=0;i<eqns.length;++i) {
			nodes[i]=jep.parse(eqns[i]);
		}

		// build an array of dependencies
		boolean[][] deps = new boolean[eqns.length][eqns.length]; 
		for(int i=0;i<eqns.length;++i) {
			for(int j=0;j<eqns.length;++j) {
				if(i==j) continue;
				if(dependsOn(nodes[i],nodes[j])) {
					System.out.printf("\"%s\" depends on \"%s\"%n",
							jep.toString(nodes[i]),jep.toString(nodes[j]));
					deps[i][j]=true;
				}
				else {
					System.out.printf("\"%s\" does not depends on \"%s\"%n",
							jep.toString(nodes[i]),jep.toString(nodes[j]));
					deps[i][j]=false;
				}
			}
		}
		System.out.println(Arrays.deepToString(deps));


		int[] order = new int[eqns.length];
		Arrays.fill(order, -1);
		boolean[] done = new boolean[eqns.length];
		Arrays.fill(done, false);

		for(int loop=0;loop<eqns.length;++loop) {

			// find an expression A for which (B dep A) is false for all other B
			// exclude those which have already been done
			for(int i=0;i<eqns.length;++i) {
				if(done[i]) continue; 

				boolean OK=true; // does anything depend on this
				for(int j=0;j<eqns.length;++j) {
					if(done[j]) continue;
					if(i==j) continue;
					// something depends on this 
					if(deps[j][i]) OK=false;
				}
				if(OK) {
					order[loop] = i;
					done[i] = true;
					break;
				}
			}
			if(order[loop]==-1) {
				System.out.println("nothing found");
			}
		}
		System.out.println(Arrays.toString(order));
		System.out.println(Arrays.toString(done));

		jep.addVariable("Var1",1.0);
		jep.addVariable("Var2",2.0);
		for(int i=eqns.length-1;i>=0;--i) {
			jep.println(nodes[order[i]]);
			jep.evaluate(nodes[order[i]]);
		}
	}

	// Operators
	private static final String SYMBOL_AND = "#AND#";
	private static final String SYMBOL_OR = "#OR#";

	private void initJep() throws JepException {
		jep = new Jep();
		ConfigurableParser cp = new ConfigurableParser();
		//cp.addHashComments();
		cp.addSlashComments();
		cp.addSingleQuoteStrings();
		cp.addDoubleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(",")","[","]",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$ //$NON-NLS-5$
		cp.setImplicitMultiplicationSymbols("(","["); //$NON-NLS-1$ //$NON-NLS-2$
		cp.addIdentifiers();
		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(",")"); //$NON-NLS-1$ //$NON-NLS-2$
		cp.addFunctionMatcher("(",")",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cp.addListMatcher("[","]",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
		cp.addArrayAccessMatcher("[","]"); //$NON-NLS-1$ //$NON-NLS-2$

		jep.setComponent(cp);
		jep.setAllowUndeclared(true);
		OperatorTableI operatorTable = jep.getOperatorTable();
		operatorTable.getOr().setSymbol(SYMBOL_OR);
		operatorTable.getAnd().setSymbol(SYMBOL_AND);
		jep.reinitializeComponents();
		jep.addVariable("T", true);
		jep.addVariable("F", false);
	}

	@Test
	public void andCustomOperatorWorks() throws Exception {
		initJep();
		String expression = "T " + SYMBOL_AND + " T";
		jep.parse(expression);
		assertTrue(expression + " should return true", (Boolean)
				jep.evaluate());

		expression = "T " + SYMBOL_AND + " F";
		jep.parse(expression);
		assertFalse(expression + " should return false", (Boolean)
				jep.evaluate());

		expression = "F " + SYMBOL_AND + " F";
		jep.parse(expression);
		assertFalse(expression + " should return false", (Boolean)
				jep.evaluate());

		expression = "F " + SYMBOL_AND + " T";
		jep.parse(expression);
		assertFalse(expression + " should return false", (Boolean)
				jep.evaluate());
	}

	@Test
	public void orCustomOperatorWorks() throws Exception {
		initJep();

		String expression = "T " + SYMBOL_OR + " T";
		jep.parse(expression);
		assertTrue(expression + " should return true", (Boolean)
				jep.evaluate());

		expression = "T " + SYMBOL_OR + " F";
		jep.parse(expression);
		assertTrue(expression + " should return true", (Boolean)
				jep.evaluate());

		expression = "F " + SYMBOL_OR + " F";
		jep.parse(expression);
		assertFalse(expression + " should return false", (Boolean)
				jep.evaluate());

		expression = "F " + SYMBOL_OR + " T";
		jep.parse(expression);
		assertTrue(expression + " should return true", (Boolean)
				jep.evaluate()); 
	}

	@Test
	public void findNewVariables() throws Exception {
		jep = new Jep();
		jep.setAllowUndeclared(true);
		// Two new variables and one existing variable
		String expression="var1 + var2 * pi"; 
		Collection<Variable> preParseVariables = 
				new ArrayList<>(jep.getVariableTable().getVariables());
		jep.parse(expression);
		Collection<Variable> postParseVariables = 
				new ArrayList<>(jep.getVariableTable().getVariables());
		postParseVariables.removeAll(preParseVariables);
		jep.addVariable("var1", 5);
		assertEquals(2,postParseVariables.size());
		for(Variable v:jep.getVariableTable().getVariables()) {
			System.out.println(""+v.getValue() +" "+ v.hasValidValue()); 
		}
	}

	/**
	 * Tests use of TreeAnalyzer to work out the order of expressions.
	 * @throws ParseException
	 * @throws Exception
	 */
	@Test
	public void testMultiLineLoop() throws ParseException, Exception {
		// setup equations to be parsed
		String eqns = 
				"x=5;"+
						"y=6;"+
						"z=x^2-y^2;"+
						"w=z/(x-y);"+
						"v=z/(x+y);";
		List<Node> nodes = new ArrayList<>();
		jep.initMultiParse(eqns);
		// parse each expression
		Node node;
		while((node = jep.continueParsing())!=null) {
			nodes.add(node);
		}
		// Now evaluate them in turn
		for(Node n:nodes) {
			Object res = jep.evaluate(n);
			System.out.print("Equation:\t");
			jep.println(n);
			System.out.println("Result:\t"+res);
		}
	}

	/**
	 * Tests use of TreeAnalyzer to work out the order of expressions.
	 * @throws ParseException
	 * @throws Exception
	 */
	@Test
	public void testExpressionOrderingMultiLine() throws ParseException, Exception {
		// setup equations to be parsed
		String eqns = 
				"Var4 = Var1 + Var3;"+
						"Var5 = Var4 + 1;"+
						"Var3 = 3 * Var2;";
		List<Node> nodes = new ArrayList<>();
		jep.initMultiParse(eqns);
		// parse each expression
		Node node;
		while((node = jep.continueParsing())!=null) {
			nodes.add(node);
		}

		for(Node n:nodes) {
			jep.println(n);
		}

		int length = nodes.size();

		// build an array of dependancies
		boolean[][] deps = new boolean[length][length]; 
		for(int i=0;i<length;++i) {
			for(int j=0;j<length;++j) {
				if(i==j) continue;
				if(dependsOn(nodes.get(i),nodes.get(j))) {
					System.out.printf("\"%s\" depends on \"%s\"%n",
							jep.toString(nodes.get(i)),jep.toString(nodes.get(j)));
					deps[i][j]=true;
				}
				else {
					System.out.printf("\"%s\" does not depends on \"%s\"%n",
							jep.toString(nodes.get(i)),jep.toString(nodes.get(j)));
					deps[i][j]=false;
				}
			}
		}
		System.out.println(Arrays.deepToString(deps));


		int[] order = new int[length];
		Arrays.fill(order, -1);
		boolean[] done = new boolean[length];
		Arrays.fill(done, false);

		for(int loop=0;loop<length;++loop) {

			// find an expression A for which (B dep A) is false for all other B
			// exclude those which have already been done
			for(int i=0;i<length;++i) {
				if(done[i]) continue; 

				boolean OK=true; // does anything depend on this
				for(int j=0;j<length;++j) {
					if(done[j]) continue;
					if(i==j) continue;
					// something depends on this 
					if(deps[j][i]) OK=false;
				}
				if(OK) {
					order[loop] = i;
					done[i] = true;
					break;
				}
			}
			if(order[loop]==-1) {
				System.out.println("nothing found");
			}
		}
		System.out.println(Arrays.toString(order));
		System.out.println(Arrays.toString(done));

		jep.addVariable("Var1",1.0);
		jep.addVariable("Var2",2.0);
		for(int i=length-1;i>=0;--i) {
			jep.println(nodes.get(order[i]));
			jep.evaluate(nodes.get(order[i]));
		}
	}


	class PathEvaluator extends FastEvaluator {
		private static final long serialVersionUID = 1L;

		@Override
		protected Object nodeAccept(Node node) throws EvaluationException {
			System.out.println("Visit "+node.toString());
			return super.nodeAccept(node);
		}
	}

	class PathEvaluator2 extends FastEvaluator {
		private static final long serialVersionUID = 1L;
		HookKey key = new Node.HookKey(){ /*empty */ };
		int pos=0;
		@Override
		protected Object nodeAccept(Node node) throws EvaluationException {
			System.out.println("Visit "+node.toString());
			node.setHook(key, new Integer(++pos));
			return super.nodeAccept(node);
		}
	}

	@Test
	public void testEvaluationPath() throws JepException {
		String s = "if(Z==1," +
				"if(Y==2,if(X==3,\"A\",\"B\"),if(W==4,\"C\",\"D\"))," +
				"if(V==5,if(U==6,\"E\",\"F\"),if(T==7,\"G\",\"H\")))";
		// Create the evaluator
		Evaluator ev = new PathEvaluator();
		// Set up jep using this evaluator
		jep = new Jep(ev);
		jep.addVariable("Z", 1);
		jep.addVariable("Y", 2);
		jep.addVariable("X", 3);
		jep.addVariable("W", 1);
		jep.addVariable("U", 1);
		jep.addVariable("V", 1);
		jep.addVariable("T", 1);
		// Parse and evaluate the equation
		Node n = jep.parse(s);
		Object res = jep.evaluate(n);
		System.out.println("Result: "+res);
	}

	/**
	 * Pattern used to match numbers with units.
	 * The pattern needs some work as it not smart enough to get all numbers/units so .1m/s
	 */
	static Pattern numberWithUnitPattern = Pattern.compile("(\\d+.\\d*)([a-zA-Z]+)"); 

	/**
	 * Class to hold a number with a unit.
	 */
	static class NumberWithUnit {
		double num;
		String unit;
		public NumberWithUnit(String s) throws ParseException {
			Matcher m = numberWithUnitPattern.matcher(s);
			if(!m.matches()) throw new ParseException("Number didn't match \""+s+"\"");
			num = Double.parseDouble(m.group(1));
			unit = m.group(2);
		}

		public NumberWithUnit(double n,String u) {
			num=n;
			unit=u;
		}

		@Override
		public boolean equals(Object obj) {
			if(!(obj instanceof NumberWithUnit)) return false;
			NumberWithUnit that=(NumberWithUnit) obj;
			boolean b1 = this.num == that.num;
			boolean b2 = this.unit.equals(that.unit);
			return b1 && b2;
		}

		@Override
		public String toString() {
			return "" + num + unit;
		}

		public double getNum() {
			return num;
		}

		public String getUnit() {
			return unit;
		}

		@Override
		public int hashCode() {
			return super.hashCode();
		}
		
	}
	
	/**
	 * TokenMatcher used to match the a number with a unit.
	 */
	static class NumberWithUnitTokenMatcher extends RegExpTokenMatcher {
		private static final long serialVersionUID = 1L;

		public NumberWithUnitTokenMatcher() {
			super(numberWithUnitPattern);
		}

		@Override
		public void init(Jep j) {
		}

		@Override
		public Token buildToken(String s) {
			NumberToken t;
			try {
				t = new NumberToken(s,new NumberWithUnit(s));
				return t;

			} catch (ParseException e) {
				System.out.println(e);
				return null;
			}
		}}
	
    /**
     * <a href="https://groups.google.com/forum/?hl=en_US&fromgroups=#!topic/jep-users/isBXCrRBg3c">forum</a>
     * @throws JepException
     */
	@Test
	public void testNumbersAsStrings() throws JepException {
		NumberWithUnit num1 = new NumberWithUnit("1.5m");
		NumberWithUnit num2 = new NumberWithUnit(1.5,"m");
		assertEquals(num2,num1);
		
		ConfigurableParser cp = new ConfigurableParser();
        cp.addHashComments();
        cp.addSlashComments();
        cp.addSingleQuoteStrings();
        cp.addDoubleQuoteStrings();
        cp.addWhiteSpace();
        cp.addTokenMatcher(new NumberWithUnitTokenMatcher()); 
        cp.addExponentNumbers();
        cp.addOperatorTokenMatcher();
        cp.addSymbols("(",")","[","]",",");
        cp.setImplicitMultiplicationSymbols("(","[");
        cp.addIdentifiers();
        cp.addSemiColonTerminator();
        cp.addWhiteSpaceCommentFilter();
        cp.addBracketMatcher("(",")");
        cp.addFunctionMatcher("(",")",",");
        cp.addListMatcher("[","]",",");
        cp.addArrayAccessMatcher("[","]");
		
		
		Jep j = new Jep(cp);

		String s0="1.5m";
		Node n0 = j.parse(s0);
		assertEquals(num2,n0.getValue());
		
		String s="x + 1.5m + y";
		Node n = j.parse(s);
		Node term1 = n.jjtGetChild(0).jjtGetChild(0);
		Node term2 = n.jjtGetChild(0).jjtGetChild(1);
		Node term3 = n.jjtGetChild(1);
		assertEquals("x",term1.getName());
		assertEquals("y",term3.getName());
		assertEquals(num2,term2.getValue());
		
		
	}
	
	static class RegExpFun extends BinaryFunction {
		private static final long serialVersionUID = 1L;

		@Override
		public Object eval(Object l, Object r) throws EvaluationException {
			Pattern pat = Pattern.compile((String) r);
			Matcher m = pat.matcher((CharSequence) l);
			return m.matches();
		}
	}

	@Test
	public void testRegExpOp() throws JepException {
		
		// create a version of jep using the StandardConfigurableParser which allows adding operators
		jep = new Jep(new StandardConfigurableParser());
		OperatorTable2 ot = (OperatorTable2) jep.getOperatorTable();
		
		// Get the current equals op
		Operator eq = ot.getEQ();
		
		// Create a new operator, with symbol =~ using RegExpFun for computation, 
		// and the same set of flags (associativity etc as equals)
		Operator reg = new Operator("=~",new RegExpFun(),eq.getFlags());
						
		ot.addOperator(new OperatorKey(){}, reg, eq); // Adds operator with same precedence as equals
		jep.reinitializeComponents(); // Let jep know the operators have changed
		
		String s1 = "var =~ \"fo+\""; // does foo match an f followed by a number of o's
		jep.parse(s1);
		
		jep.addVariable("var", "foo");
		Object res1 = jep.evaluate();
		Assert.assertTrue((Boolean) res1);

		jep.addVariable("var", "bar");
		Object res2 = jep.evaluate();
		Assert.assertFalse((Boolean) res2);

	}
	
	@Test
	public void testGetNonConstantVaraibles() throws JepException {
		jep = new Jep();
		VariableTable vtsrc = jep.getVariableTable();
		VariableTable vt = new VariableTable();
		vt.setVariableFactory(new VariableFactory());
		vt.copyVariablesFrom(vtsrc);  //throws null pointer exception
		
		jep.addVariable("x", 5.0);
		jep.addVariable("y", 5.0);
		jep.addVariable("z", 5.0);
		
		Collection<Variable> vars = vtsrc.getVariables();
		System.out.println("before");
		System.out.println(vars);
		Iterator<Variable> itt = vars.iterator();
		while(itt.hasNext()) {
			Variable v=itt.next();
			if(v.isConstant()) {
				itt.remove();
			}			
		}
		System.out.println("after");
		System.out.println(vars);
		assertEquals(3,vars.size());
		
		System.out.println(vtsrc.toString());
		
	}
	
	static class CommaNumberTokenMatcher extends NumberTokenMatcher {
		private static final long serialVersionUID = 1L;

		public CommaNumberTokenMatcher() {
			super("(\\d+\\,?\\d*)|(\\,\\d+)");
		}

		@Override
		public Token buildToken(String s) {
			String s2=s.replace(',','.');
			return super.buildToken(s2);
		}
	}

	@Test
	public void testCommaNumber() throws JepException {
		ConfigurableParser cp = new ConfigurableParser();
        cp.addHashComments();
        cp.addSlashComments();
        cp.addSingleQuoteStrings();
        cp.addDoubleQuoteStrings();
        cp.addWhiteSpace();
        cp.addTokenMatcher(new CommaNumberTokenMatcher()); 
        cp.addExponentNumbers();
        cp.addOperatorTokenMatcher();
        cp.addSymbols("(",")","[","]",";");
        cp.setImplicitMultiplicationSymbols("(","[");
        cp.addIdentifiers();
        cp.addSemiColonTerminator();
        cp.addWhiteSpaceCommentFilter();
        cp.addBracketMatcher("(",")");
        cp.addFunctionMatcher("(",")",";");
        cp.addListMatcher("[","]",";");
        cp.addArrayAccessMatcher("[","]");
		
		
		jep = new Jep(cp);
		jep.parse("24,9");
		Object result = jep.evaluate();
		assertEquals(24.9,result);

		jep.parse("if(2,1 > 3,4 ; 5,6 ; 7,8)");
		result = jep.evaluate();
		assertEquals(7.8,result);
		
		jep.parse("[2,1;3,4]");
		result = jep.evaluate();
		assertArrayEquals(new Object[]{2.1,3.4}, ((List<?>) result).toArray());

		
	}
	
	
	@Test
	public void test18April2013() throws JepException {
	jep = new Jep(new StandardConfigurableParser());
    BitwiseOperatorTable bitwiseOperatorTable = new BitwiseOperatorTable("**", "^");
    jep.setComponent(bitwiseOperatorTable);
    //jep.reinitializeComponents();
    String formula = "a|b ";//== b";
    jep.addVariable("a", 0x1);
    jep.addVariable("b", 0x1);
    jep.parse(formula);
    System.out.println(jep.evaluate());
	}
	
	static class JexParser extends Jep {
		private static final long serialVersionUID = 1L;

		JexParser() {
	        super(new StandardConfigurableParser());
	        OperatorTable2 ot = (OperatorTable2) this.getOperatorTable();

	        Operator jepEQ = ot.getEQ();
	        jepEQ.setSymbol("=");
	        jepEQ.setPrintSymbol("=");
	        
//	        Operator ourEQ = new Operator("=", jepEQ.getPFMC(), jepEQ.getFlags(), jepEQ.getPrecedence());
//	        ot.addOperator(ot.getKey(jepEQ), ourEQ);

	        Operator jepOR = ot.getOr();
	        Operator ourOR = new Operator("|", jepOR.getPFMC(), jepOR.getFlags(), jepOR.getPrecedence());
	        ot.addOperator(ot.getKey(jepOR), ourOR);
	 
	        // replace default AND op (&&) with our AND op (&)
	        Operator jepAND = ot.getAnd();
	        Operator ourAND = new Operator("&", jepAND.getPFMC(), jepAND.getFlags(), jepAND.getPrecedence());
	        ot.addOperator(ot.getKey(jepAND), ourAND);

	        ot.removeOperator(ot.getAssign());
	        System.out.println(ot);
	 
	        this.reinitializeComponents(); // Let jep know the operators have changed
	    }
	}
	
	@Test
	public void testJexParser() throws JepException {
		JexParser jex = new JexParser();
        OperatorTable2 ot = (OperatorTable2) jex.getOperatorTable();

        Node n = jex.parse("a = b");
        assertEquals(n.getOperator(),ot.getEQ());

		Node n2 = jex.parse("a | b");
        assertEquals(n2.getOperator(),ot.getOr());

		Node n3 = jex.parse("a & b");
        assertEquals(n3.getOperator(),ot.getAnd());

	}
	
	@Test
	public void testNullWrapBigDec() throws JepException {
        jep = new Jep(new BigDecComponents());
		jep.setComponent(new StandardConfigurableParser());
		jep.setComponent(new NullWrappedOperatorTable(
            (OperatorTable2) jep.getOperatorTable(),true));
		jep.setComponent(new StandardConfigurableParser());
		((FastEvaluator) jep.getEvaluator()).setTrapNullValues(false);
		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		jep.setComponent(new NullWrappedFunctionTable(jep.getFunctionTable()));
		jep.addFunction("case",new Case());

        jep.parse("3+4");
		System.out.println(jep.evaluate());
        jep.parse("3+null");
		System.out.println(jep.evaluate());
		jep.parse("min(3,4)");
		System.out.println(jep.evaluate());
		jep.parse("min(3,4,null)");
		System.out.println(jep.evaluate());
		jep.parse("case(3,1,\"one\",2,\"two\",3,\"three\")");
		System.out.println(jep.evaluate());
		jep.parse("case(3,1,\"one\",null,\"two\",3,\"three\")");
		System.out.println(jep.evaluate());
		jep.parse("case(null,1,\"one\",null,\"two\",3,\"three\")");
		System.out.println(jep.evaluate());
	}
	
	@Test
	public void testSlope() throws JepException {
	    jep = new Jep();
        OperatorTableI ot = jep.getOperatorTable();

        Node n = jep.parse("x*-3-5");
	    Node l = n.jjtGetChild(0); // x*-3 
        Node r = n.jjtGetChild(1); // - 5
        Node ll = l.jjtGetChild(0); // x
        Node lr = l.jjtGetChild(1); // -(3)

        double slope=0.0;
        if(ot.getUMinus().equals(lr.getOperator())) {
            Node lrl = lr.jjtGetChild(0); // 3
            slope = -((Double) lrl.getValue());
        } else {
            slope = (Double) lr.getValue();
        }
        
        double intercept = 0.0;
        if( n.getOperator().equals(ot.getAdd())) {
            intercept = (Double) r.getValue();
        } else if( n.getOperator().equals(ot.getSubtract())) {
            intercept = -(Double) r.getValue();
        }
               
        assertEquals("x",ll.getName());
        assertEquals(-3.0,slope,1e-6);
        assertEquals(-5.0,intercept,1e-6); 	    
        
        Node n2 = jep.parse("if ((  ( (P5525__V_MIN >= 30.0 || P5525__V_MAX <= 50.0) && (P0003__V_MIN >= 30.0 || P0003__V_MAX <= 50.0) ) && P0001__V_MAX > 3000.0 ), RESULT = 1, RESULT = 0)");
        Node c1 = n2.jjtGetChild(0);
//        Node c2 = n2.jjtGetChild(1);
//        Node c3 = n2.jjtGetChild(2);
        Node c1ll = c1.jjtGetChild(0).jjtGetChild(0);
        Node c1lr = c1.jjtGetChild(0).jjtGetChild(1);
        Node c1r = c1.jjtGetChild(1);
        jep.println(c1ll);
        jep.println(c1lr);
        jep.println(c1r);
	}
	
	/**
	 * Workaround class for following test
	 *
	 */
	static class MyEvaluator extends FastEvaluator {
		private static final long serialVersionUID = 1L;

		@Override
		public Object visit(ASTVarNode node, Object data) throws EvaluationException {
			Variable var = node.getVar();
			if(!var.hasValidValue())
				throw new EvaluationException("Value not set");
			return super.visit(node, data);
		}
	}

	/**
	 * See http://stackoverflow.com/questions/36928755/strange-behavior-in-singularsys-jep
	 * 
	 * @throws JepException
	 */
	@Test
	public void testClearValues() throws JepException {
		jep = new Jep();
	    jep.parse("x*2");

	    try {
	    	jep.evaluate();
	    	fail("Unset variable should have been found");
	    } catch(EvaluationException e) {
	    	System.out.println(e);
	    }
	    jep.addVariable("x",1.0);
	    Object r1 = jep.evaluate();
	    assertEquals(Double.valueOf(2.0),r1);

	    // Another workaround
//	    for(Variable var:jep.getVariableTable().getVariables()) {
//	    	var.setValue(null);
//	    }
	    jep.getVariableTable().clearValues();
	    for(Variable var:jep.getVariableTable().getVariables()) {
	    	var.setValue(null);
	    }
	    try {
	       jep.evaluate();
	    	fail("Unset variable should have been found");
	    } catch (Exception ex) {
	    	System.out.println(ex);
	    }

	}
	
	/**
	 * A evaluator which checks to a result of negative zero and converts it to positive zero.
	 */
	static class PosZeroEvaluator implements Evaluator {
	    private static final long serialVersionUID = 1L;
	    Evaluator rootEval;
	    Double negativeZero = -0.0;
	    Double positiveZero = 0.0;
	    
	    public PosZeroEvaluator(Evaluator rootEval) {
		super();
		this.rootEval = rootEval;
	    }

	    @Override
	    public void init(Jep jep) {
		rootEval.init(jep);
	    }

	    @Override
	    public JepComponent getLightWeightInstance() {
		return this;
	    }

	    @Override
	    public Object evaluate(Node node) throws EvaluationException {
		Object res = rootEval.evaluate(node);
		if(res instanceof Double) {
		    @SuppressWarnings("unused")
		    double dval = (Double) res;
		    if(res.equals(negativeZero))
			return positiveZero;
		}
		return res;
	    }

	    @Override
	    public Object eval(Node node) throws EvaluationException {
		return rootEval.eval(node);
	    }
	}

	@Test
	public void PosZeroTest() throws JepException {
	    String s = "0.0 * -1.0";
	    jep.parse(s);
	    Object res = jep.evaluate();
	    System.out.println(res);
	    assertEquals("-0.0",res.toString());

	    jep.setComponent(new PosZeroEvaluator(new FastEvaluator()));
	    jep.reinitializeComponents();
	    
	    res = jep.evaluate();
	    System.out.println(res);
	    assertEquals("0.0",res.toString());
	}
	
	/**
	 * A function which can incorrectly read the stack.
	 * Last argument controls how many items to pop off the stack
	 * 
	 */
	public static class StackMessingFunc extends PostfixMathCommand {
	    private static final long serialVersionUID = 1L;

	    public StackMessingFunc() {
		super(-1);
	    }

	    @Override
	    public void run(Stack<Object> stack) throws EvaluationException {
		int n = ((Number)stack.pop()).intValue();
		int sum = n;
		for(int i=1;i<n;++i)
		    sum += ((Number)stack.pop()).intValue();
		stack.push(sum);
	    }
	    
	    
	}
	
	/** Uses a special evaluator to check for stack errors
	 * 
	 * @throws JepException
	 */
	
	@Test
	public void StackCorruptionTest() throws JepException {
	    jep = new Jep(new StackCheckingFastEvaluator());
	    
	    // First test the function when it does not corrupt the stack
	    jep.addFunction("func", new StackMessingFunc());
	    jep.parse("func(func(4,2),func(1),3)");
	    double res1 = jep.evaluateD();
	    assertEquals(10.0,res1,1e-9);  
	    
	    // Tests a case where inner function reads too few stack items 
	    // but the outer function reads one too many
	    // caught error with smarter evaluator
	    try {
		jep.parse("func(func(4,1),func(1),4)");
		@SuppressWarnings("unused")
		double res2 = jep.evaluateD();
		fail("Stak Error should have been found");        
	    } catch(JepException e) {
		System.out.println("Caught: "+e.toString());
	    }
	    
	    // Normal fastEvaluator does not detect this error
	    jep.setComponent(new FastEvaluator());
	    @SuppressWarnings("unused")
	    double res2 = jep.evaluateD();
	}
}
