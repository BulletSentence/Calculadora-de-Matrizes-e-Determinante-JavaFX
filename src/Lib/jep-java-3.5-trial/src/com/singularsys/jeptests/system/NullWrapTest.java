/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 10 Jul 2009 - Richard Morris
 */
package com.singularsys.jeptests.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;
import static java.lang.System.out;

import java.lang.reflect.Method;
import java.util.Stack;

//import java.lang.reflect.Method;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Evaluator;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.matchers.HexNumberTokenMatcher;
import com.singularsys.jep.configurableparser.matchers.UpperCaseOperatorTokenMatcher;
import com.singularsys.jep.functions.Average;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.IllegalParameterException;
import com.singularsys.jep.functions.MinMax;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.functions.NaryFunction;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.functions.VSum;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.IsNull;
import com.singularsys.jep.misc.functions.Switch;
import com.singularsys.jep.misc.functions.SwitchDefault;
import com.singularsys.jep.misc.javaops.JavaOperatorTable;
import com.singularsys.jep.misc.nullwrapper.NullWrappedFunctionTable;
import com.singularsys.jep.misc.nullwrapper.NullWrappedOperatorTable;
import com.singularsys.jep.misc.nullwrapper.functions.NullWrappedFunctionI;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.FastEvaluator;

public class NullWrapTest extends JepTest {

	/**
	 * Sets up the parser.
	 */
	@Override
	@Before
	public void setUp() {
		System.out.println("setUp");
		// Set up the parser
		jep = new Jep();
		jep.setImplicitMul(true);
		//jep.addStandardFunctions();
		jep.addStandardConstants();
		//jep.addComplex();
		//jep.setTraverse(false);
		jep.setComponent(new NullWrappedOperatorTable((OperatorTable2) jep.getOperatorTable(),true));
		jep.setComponent(new NullWrappedFunctionTable(jep.getFunctionTable()));
		jep.setComponent(new StandardConfigurableParser());
		//        jep.getOperatorTable().getAnd().setPFMC(new NullLazyLogical(NullLazyLogical.AND));
		//        jep.getOperatorTable().getOr().setPFMC(new NullLazyLogical(NullLazyLogical.OR));
		//Evaluator eval = jep.getEvaluator();
		//Method meth = eval.getClass().getDeclaredMethod("setTrapNullValues", Boolean.TYPE);
		//meth.invoke(eval, false);
		((FastEvaluator) jep.getEvaluator()).setTrapNullValues(false);
		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		
	}

	@Override
	protected void myAssertNull(String msg,Object actual)
	{
		if(actual == null) {
			System.out.println("Success: Value of \""+msg+"\" is "+actual+"");
		}
		else {
			System.out.println("Error: '"+msg+"' is '"+actual+"' should be 'Null'");
			fail("<"+msg+"> is "+actual+" should be null");
		}
	}

	@Override
	protected void myAssertEquals(String msg, Object expected, Object actual) {
		if(PRINT_RESULTS && !expected.equals(actual))
			System.out.println("Error: '"+msg+"' is '"+actual+"' should be '"+expected+"'");
		assertEquals("<"+msg+">",expected,actual);
		if(PRINT_RESULTS)
			System.out.println("Success: value of \""+msg+"\" is "+actual+"");
	}

	@Test 
	public void testNullWrap() throws Exception
	{
		System.out.println("testNullWrap");

		Variable var = jep.addVariable("null",null);
		var.setValidValue(true);
		var.setIsConstant(true);

		String eqn = "null * 2";
		Node n = jep.parse(eqn);
		Object val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "2 * 2";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertEquals(eqn,4.0,val);

		eqn = "2 * null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null * null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "2 + 2";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertEquals(eqn,4.0,val);

		eqn = "null + 2";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "2 + null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null + null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "x = null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);
		val = jep.getVariableValue("x");
		myAssertNull(eqn,val);

		eqn = "sin(null)";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "sin(0.0)";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertEquals(eqn,val, 0.0);

		eqn = "atan2(null,1.0)";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "atan2(1.0,null)";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "if(null,1.0,2.0)";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);


	}

	@Test
	public void testNullLogical() throws JepException
	{
		String eqn = "null || null";
		Node n = jep.parse(eqn);
		Object val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null || true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "null || false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);


		eqn = "true || null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "true || true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "true || false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);


		eqn = "false || null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "false || true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "false || false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);


		eqn = "null && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null && true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null && false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "true && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "true && true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "true && false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);


		eqn = "false && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "false && true";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "false && false";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "null || null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null || 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "null || 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);


		eqn = "1 || null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "1 || 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "1 || 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);


		eqn = "0 || null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "0 || 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "0 || 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);


		eqn = "null && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null && 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null && 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "1 && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "1 && 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "1 && 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);


		eqn = "0 && null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "0 && 1";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "0 && 0";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

	}


	@Test
	public void testNullSafeEquals() throws JepException
	{
		String eqn = "null == null";
		Node n = jep.parse(eqn);
		Object val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "null == 5";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "5 == null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		myAssertNull(eqn,val);

		eqn = "5 == 5";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "5 == 6";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "null <=> null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "null <=> 5";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "5 <=> null";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

		eqn = "5 <=> 5";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.TRUE, val);

		eqn = "5 <=> 6";
		n = jep.parse(eqn);
		val = jep.evaluate(n);
		this.myAssertEquals(eqn, Boolean.FALSE, val);

	}

	/**
	 * Overridden as undeclared variable indistinguishable from null.
	 * Does nothing. 
	 */
	@Override
	public void testSetAllowUndeclared() throws Exception {
		jep.setAllowUndeclared(false);
		String s="x+y";
		try {
			jep.parse(s);
		}
		catch(ParseException e) {
			out.println("Expected exception caught "+e.toString());
		}
		try {
			Object val = jep.evaluate();
			myAssertNull(s,val);
		} catch(EvaluationException e) {
			fail(e.toString());
		}
	}


	@Override
	public void testNull() throws Exception {
	}

	@Test
	public void testIsNull() throws Exception
	{
		// check if null trapping is on by default
		printTestHeader("Testing for null values");
		jep.addFunction("isNull",new IsNull());
		jep.addConstant("mynull",null);
		//        try {
		//            valueTest("isNull(mynull)",myTrue);
		//            fail("Null value should have been trapped");
		//        } 
		//        catch(EvaluationException e) {
		//            System.out.println("Null value sucessfully trapped");
		//        }
		// check if isNull(5) returns false as expected
		valueTest("isNull(5)",myFalse);
		// try calling setTrapNullValues(true) with reflection
		try {
			Evaluator ev = jep.getEvaluator();
			Method meth;
			meth = ev.getClass().getMethod("setTrapNullValues",Boolean.TYPE);
			meth.invoke(ev,false);
			try {
				valueTest("isNull(mynull)",myTrue);
				valueTestNull("nnn=mynull");
				valueTest("isNull(nnn)",myTrue);
				valueTest("isNull(null)",myTrue);
			} catch (Exception e) {
				fail("With TrapNullValues=false "+e.getMessage());
				//                e.printStackTrace();
			}
		} catch (NoSuchMethodException e1) {
			System.out.println("No setTrapNullValues method, skipping tests.");
		}
	}

	@Test
	public void testListNull() throws Exception {
		jep.addConstant("null", null);
		valueTestString("x=[4,3,null,1]","[4.0, 3.0, null, 1.0]");
		valueTestNull("x[3]");
		valueTestNull("x[2]=null");
		valueTestString("x","[4.0, null, null, 1.0]");
		try {
			valueTestNull("x[null]");
			fail("'x[null]' should be an error");
		} catch(EvaluationException e) {
			out.println(e.toString());
		}
		try {
			valueTestNull("x[null]=4");
			fail("'x[null]=4' should be an error");
		} catch(EvaluationException e) {
			out.println(e.toString());
		}
		try {
			valueTestNull("null[1]");
			fail("'null[1]' should be an error");
		} catch(EvaluationException e) {
			out.println(e.toString());
		}
	}
	
	@Override
	@Test
	public void testListFunctions() throws Exception
	{
		printTestHeader("List functions");
		valueTestString("x=[[1,2],[3,4]]","[[1.0, 2.0], [3.0, 4.0]]");
		valueTestString("min(x)","1.0");
		valueTestString("max(x)","4.0");
		valueTestString("avg(x)","2.5");
		valueTestString("vsum(x)","10.0");
		valueTestString("y=[]","[]");

		this.valueTestFail("avg(y)");
		this.valueTestFail("min(y)");
		this.valueTestFail("max(y)");
		valueTestString("vsum(y)","0.0");
		FunctionTable ft = jep.getFunctionTable();
		((Average) ((NullWrappedFunctionI)ft.getFunction("avg")).getRoot()).setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
		((MinMax) ((NullWrappedFunctionI)ft.getFunction("min")).getRoot()).setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
		((MinMax) ((NullWrappedFunctionI)ft.getFunction("max")).getRoot()).setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
		((VSum) ((NullWrappedFunctionI)ft.getFunction("vsum")).getRoot()).setZeroLengthErrorBehaviour(Average.ZeroLengthErrorBehaviour.NAN);
		this.valueTestNaN("avg(y)");
		this.valueTestNaN("min(y)");
		this.valueTestNaN("max(y)");
		valueTestString("vsum(y)","0.0");
	}

	@Override
	@Test
	public void testCaseNull() throws Exception {
		jep.addConstant("null", null);
		jep.addFunction("case",new Case(Case.NullBehaviour.RETURN_NULL));
		jep.addFunction("cased",new Case(null,Case.NullBehaviour.TEST_ARG));
        jep.addFunction("switch",new Switch(Switch.NullBehaviour.RETURN_NULL));
        jep.addFunction("switchd",new SwitchDefault(SwitchDefault.NullBehaviour.RETURN_NULL));

        valueTestNull("case(\"a\",\"a\",null,\"b\",6,\"c\",7,8)");
        valueTestNull("case(null,\"a\",null,\"b\",6,\"c\",7,8)");
        valueTestNull("case(null,null,5,\"b\",6,\"c\",7,8)");
        valueTestNull("cased(null,\"a\",5,\"b\",6,\"c\",7)");
        valueTestNull("cased(null,\"a\",5,null,6,\"c\",7)");
        valueTestNull("switch(null,5,6,7,8)");
        valueTestNull("switchd(null,5,6,7,8)");

	}
	
	
    @Test
    public void testBitwise() throws Exception {
        printTestHeader("Testing Bitwise operations");
        setupExtended();
        this.valueTestString("0x09","9");
        this.valueTestString("0x0a","10");
        this.valueTestString("0x0f","15");
        this.valueTestString("0x10","16");
        this.valueTestString("0x11","17");

        this.valueTestString("0x18 | 0x09","25");
        this.valueTestString("0x18 & 0x09","8");
        this.valueTestString("0x18 ^^^ 0x09","17");

        this.valueTestString("0x05 << 1","10");
        this.valueTestString("0x05 << 3","40");
        this.valueTestString("-0x05 << 2","-20");

        this.valueTestString("0x05 >> 1","2");
        this.valueTestString("0x05 >>> 1","2");

        this.valueTestString("-50 >> 2","-13");
        this.valueTestString("-50 >>> 2","1073741811");

        this.valueTestNull("null | 0x09");
        this.valueTestNull("0x09 | null");
        this.valueTestNull("0x05 << null");
        this.valueTestNull("null << 3");

    }

	private void setupExtended() throws JepException {
		ConfigurableParser cp = new ConfigurableParser();
        cp.addHashComments();
        cp.addSlashComments();
        cp.addDoubleQuoteStrings();
        cp.addWhiteSpace();
        cp.addTokenMatcher(new HexNumberTokenMatcher());
        cp.addExponentNumbers();
        cp.addTokenMatcher(new UpperCaseOperatorTokenMatcher());
        cp.addSymbols("(",")","[","]",",","IF","THEN","ELSE");
        cp.setImplicitMultiplicationSymbols("(","[");
        cp.addIdentifiers();
        cp.addSemiColonTerminator();
        cp.addWhiteSpaceCommentFilter();
        cp.addBracketMatcher("(",")");
        cp.addFunctionMatcher("(",")",",");
        cp.addListMatcher("[","]",",");
        cp.addArrayAccessMatcher("[","]");
        /*cp.addGrammerMatcher(new IfThenElseGrammerMatcher(
			cp.getSymbolToken("IF"),
			cp.getSymbolToken("THEN"),
			cp.getSymbolToken("ELSE"),
			new If()));*/
        jep.setComponent(cp);
        jep.setComponent(new NullWrappedOperatorTable(new JavaOperatorTable("^","^^^"), false));
		jep.addConstant("null", null);
        jep.reinitializeComponents();
	}

    @Test
    public void testTernary() throws Exception {
        printTestHeader("Testing Ternary operations");
        jep.setComponent(new NullWrappedOperatorTable(new JavaOperatorTable(), false));

        this.valueTestString("1==1?10:12","10.0");
        this.valueTestString("1==2?10:12","12.0");
        this.valueTestString("(1==0)?2+3:4+5","9.0");
        this.valueTestString("x=3","3.0");
        this.valueTestString("x<8?x<4?1:2:x<12?3:4","1.0");
        this.valueTestString("x=5","5.0");
        this.valueTestString("x<8?x<4?1:2:x<12?3:4","2.0");
        this.valueTestString("x=9","9.0");
        this.valueTestString("x<8?x<4?1:2:x<12?3:4","3.0");
        this.valueTestString("x=13","13.0");
        this.valueTestString("x<8?x<4?1:2:x<12?3:4","4.0");
        this.valueTestString("y=x<0?-x:x","13.0");
        this.valueTestString("y","13.0");
        this.valueTestString("y=x<0?-x:x","13.0");
        this.valueTestNull("1==1?null:12");
        this.valueTestNull("1==2?10:null");
        this.valueTestNull("null?10:12");
        this.valueTestString("1==2?null:12","12.0");
        this.valueTestString("1==1?10:null","10.0");

    }

    @Test
    public void testIncrement() throws Exception {
        printTestHeader("Testing Increment and decrement operations");
        jep.setComponent(new NullWrappedOperatorTable(new JavaOperatorTable(), false));
        this.valueTestString("x=3","3.0");
        this.valueTestString("x++","3.0");
        this.valueTestString("x","4.0");
        this.valueTestString("++x","5.0");
        this.valueTestString("x","5.0");

        this.valueTestString("x--","5.0");
        this.valueTestString("x","4.0");
        this.valueTestString("--x","3.0");
        this.valueTestString("x","3.0");
        
        this.valueTestNull("z=null");
        this.valueTestNull("z++");
        
    }

    @Test
    public void testOpEquals() throws Exception {
        printTestHeader("Testing += etc");
        setupExtended();

        this.valueTestString("x=3","3.0");
        this.valueTestString("y=4","4.0");
        this.valueTestString("y+=x","7.0");
        this.valueTestString("y","7.0");
        this.valueTestString("y-=x","4.0");
        this.valueTestString("y*=x","12.0");
        this.valueTestString("y/=x","4.0");
        this.valueTestString("y%=x","1.0");
        this.valueTestString("a=0x18","24");
        this.valueTestString("b=0x09","9");
        this.valueTestString("a|=b","25");
        this.valueTestString("a=0x18","24");
        this.valueTestString("a&=b","8");
        this.valueTestString("a=0x18","24");
        this.valueTestString("a^=b","17");

        this.valueTestString("a=0x05","5");
        this.valueTestString("a<<=1","10");
        this.valueTestString("a=0x05","5");
        this.valueTestString("a<<=3","40");
        this.valueTestString("a=-0x05","-5.0");
        this.valueTestString("a<<=2","-20");

        this.valueTestString("a=0x05","5");
        this.valueTestString("a>>=1","2");
        this.valueTestString("a=0x05","5");
        this.valueTestString("a>>>=1","2");

        this.valueTestString("a=-50","-50.0");
        this.valueTestString("a>>=2","-13");
        this.valueTestString("a=-50","-50.0");
        this.valueTestString("a>>>=2","1073741811");

        this.valueTestString("x=3","3.0");
        this.valueTestString("y=4","4.0");
        this.valueTestString("z=5","5.0");
        this.valueTestString("z-=y+=x","-2.0");
        this.valueTestString("x","3.0");
        this.valueTestString("y","7.0");
        this.valueTestString("z","-2.0");
        
        this.valueTestNull("w=null");
        this.valueTestNull("z+=w");
        this.valueTestNull("w+=x");

    }

    class MyUnary extends UnaryFunction
    {
		private static final long serialVersionUID = 1L;

		@Override
        public Object eval(Object arg) throws EvaluationException {
			double val = this.asDouble(0, arg);
            return Double.valueOf(val);
        }
        
    }

    class MyBinary extends BinaryFunction
    {
		private static final long serialVersionUID = 1L;

		@Override
        public Object eval(Object l,Object r) throws EvaluationException {
			double val = this.asDouble(0, l);
			double val2 = this.asDouble(1, r);
            return Double.valueOf(val+val2);
        }
    }

    class MyNary extends NaryFunction
    {
		private static final long serialVersionUID = 1L;

		@Override
        public Object eval(Object[] vals) throws EvaluationException {
			double val = this.asDouble(0, vals[0]);
			double val2 = this.asDouble(1, vals[1]);
            return Double.valueOf(val+val2);
        }
    }
    
    class MyNaryBinary extends NaryBinaryFunction
    {
		private static final long serialVersionUID = 1L;

		@Override
        public Object eval(Object l,Object r) throws EvaluationException {
			double val = this.asDouble(0, l);
			double val2 = this.asDouble(1, r);
            return Double.valueOf(val+val2);
        }
    }

    class MyPfmc extends PostfixMathCommand
    {
		private static final long serialVersionUID = 1L;

		public MyPfmc() {
			super(-1);
		}
		@Override
		public boolean checkNumberOfParameters(int n) {
			return n==3;
		}

		@Override
		public void run(Stack<Object> s) throws EvaluationException {
			Object l = s.pop();
			double val = this.asDouble(0, l);
			s.push(val);
		}

//		@Override
//        public Object eval(Object l,Object r) throws EvaluationException {
//			double val = this.asDouble(0, l);
//			double val2 = this.asDouble(1, r);
//            return Double.valueOf(val+val2);
//        }
    }

    @Test
    public void testErrorMessages() throws Exception {
    	String s=null;
    	Node n=null;
    	jep.addFunction("myU", new MyUnary());
    	jep.addFunction("myB", new MyBinary());
    	jep.addFunction("myN", new MyNary());
    	jep.addFunction("myNB", new MyNaryBinary());
    	jep.addFunction("myPFMC", new MyPfmc());
    	try {
    		s="sin([1,2])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","sin",e.getFunctionName());
    	}

    	try {
    		s="myU([1,2])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","myU",e.getFunctionName());
    	}

    	try {
    		s="myB([1,2],[3,4])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","myB",e.getFunctionName());
    	}

    	try {
    		s="myN([1,2],[3,4],[5,6])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","myN",e.getFunctionName());
    	}

    	try {
    		s="myNB([1,2],[3,4],[5,6])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","myNB",e.getFunctionName());
    	}

    	try {
    		s="myPFMC([1,2],[3,4],[5,6])";
    		n=jep.parse(s);
    		jep.evaluate(n);
    		fail("Exception should have been thrown "+s);
    	} catch(IllegalParameterException e) {
    		myAssertEquals("function name","myPFMC",e.getFunctionName());
    	}

    }
}
