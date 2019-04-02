/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.matchers.GrammarException;
import com.singularsys.jep.configurableparser.matchers.HexNumberTokenMatcher;
import com.singularsys.jep.configurableparser.matchers.SingleArgFunctionMatcher;
import com.singularsys.jep.configurableparser.matchers.UpperCaseOperatorTokenMatcher;
import com.singularsys.jep.configurableparser.tokens.Token;
import com.singularsys.jep.functions.ArcCosine;
import com.singularsys.jep.functions.ArcCosineH;
import com.singularsys.jep.functions.ArcSine;
import com.singularsys.jep.functions.ArcTanH;
import com.singularsys.jep.functions.Comparative;
import com.singularsys.jep.functions.LogBase2;
import com.singularsys.jep.functions.Logarithm;
import com.singularsys.jep.functions.Power;
import com.singularsys.jep.functions.SquareRoot;
import com.singularsys.jep.functions.StrictNaturalLogarithm;
import com.singularsys.jep.misc.ExtendedOperatorSet;
import com.singularsys.jep.misc.functions.ElementOf;
import com.singularsys.jep.misc.functions.FromBase;
import com.singularsys.jep.misc.functions.ToBase;
import com.singularsys.jep.misc.javaops.JavaOperatorTable;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealComponents;


/**
 * Tests for modification to the configurable parser.
 * Including adding new operators.
 * @see JavaOperatorTable
 */
public class ExtendedParserTest extends JepTest {

	public enum InOperators implements EmptyOperatorTable.OperatorKey {
        IN,NOTIN
    }
	
	@Override
    @Before
    public void setUp() {
        this.jep = new Jep();
        JavaOperatorTable jot = new JavaOperatorTable("^","^^^");
        jot.getAnd().setSymbol("AND");
        jot.getOr().setSymbol("OR");
        jot.getNot().setSymbol("NOT");
        jep.getOperatorTable().getOr().setPrintSymbol(" OR "); 
        jep.getOperatorTable().getAnd().setPrintSymbol(" AND "); 
        jep.getOperatorTable().getNot().setPrintSymbol(" NOT "); 
        Operator eqOp = jot.getEQ();
        Operator inOp = new Operator("IN",new ElementOf((Comparative) eqOp.getPFMC(),true),
        		Operator.BINARY+Operator.RIGHT);
        inOp.setPrintSymbol(" IN ");
        Operator notInOp = new Operator("NOTIN",new ElementOf((Comparative) eqOp.getPFMC(),false),
        		Operator.BINARY+Operator.RIGHT);
        notInOp.setPrintSymbol(" NOTIN ");
        jot.addOperator(InOperators.IN, inOp, jot.getEQ());
        jot.addOperator(InOperators.NOTIN, notInOp, jot.getEQ());
        ExtendedOperatorSet eos = new ExtendedOperatorSet(jot);
        jep.setComponent(eos);
        jep.addFunction("ORGINIZE",null);
        jep.addFunction("inset", new ElementOf((Comparative) jot.getEQ().getPFMC(),true));
        jep.addFunction("notinset", new ElementOf((Comparative) jot.getEQ().getPFMC(),false));

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
        //cp.addGrammarMatcher(new SimpleSingleArgFunctionMatcher());
        cp.addGrammarMatcher(new SingleArgFunctionMatcher(cp.getSymbolToken("(")));
        cp.addFunctionMatcher("(",")",",");
        cp.addListMatcher("[","]",",");
        cp.addArrayAccessMatcher("[","]");
        /*cp.addGrammerMatcher(new IfThenElseGrammerMatcher(
			cp.getSymbolToken("IF"),
			cp.getSymbolToken("THEN"),
			cp.getSymbolToken("ELSE"),
			new If()));*/
        jep.setComponent(cp);
        jep.reinitializeComponents();
    }

    @Test
    public void testBitwise() throws Exception {
        printTestHeader("Testing Bitwise operations");
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

    }

    @Test
    public void testTernary() throws Exception {
        printTestHeader("Testing Ternary operations");

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
    }

    /** Not implemented here */
    @Override
    @Test
    public void testComplex() throws Exception { /* empty */ }

    /** Not implemented here */
    @Override
    @Test
    public void testFunction() throws Exception { /* empty */ }

    /** Not implemented here */
    @Override
    @Test
    public void testPlusPlus() throws Exception { /* empty */ }

    @Test
    public void testIncrement() throws Exception {
        printTestHeader("Testing Increment and decrement operations");
        this.valueTestString("x=3","3.0");
        this.valueTestString("x++","3.0");
        this.valueTestString("x","4.0");
        this.valueTestString("++x","5.0");
        this.valueTestString("x","5.0");

        this.valueTestString("x--","5.0");
        this.valueTestString("x","4.0");
        this.valueTestString("--x","3.0");
        this.valueTestString("x","3.0");
    }

    @Test
    public void testOpEquals() throws Exception {
        printTestHeader("Testing += etc");
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
    }

    /** Not implemented here */
    @Override
    @Test
    public void testLogical() throws Exception { /* empty */   }

    @Override
    public void testNumberAsBooleanLogical() throws Exception {
    }

    /** Not implemented here */
    @Override
    @Test
    public void testLazyLogical() throws Exception { /* empty */   }

    @Override
    @Test
    public void testLazyLogical2() throws Exception {
    }

    /** Not implemented here */
    @Override
    @Test
    public void testNumParam() throws Exception { /* empty */  }

    /** Not implemented here */
    @Override
    @Test
    public void testX2Y() throws Exception { /* empty */ }

    /** Not implemented here */
    @Override
    @Test
    public void testStrings() throws Exception { /* empty */  }

    /**
     * Tests case-intensative operator names AND, And, and.
     * @throws Exception
     */
    @Test
    public void testUpperCaseOperator() throws Exception
    {
        valueTest("T=1",1.0);
        valueTest("F=0",0.0);
        valueTest("NOT T",myFalse);
        valueTest("NOT F",myTrue);
        valueTest("NOT 5",myFalse);
        valueTest("-0==0",myTrue);
        valueTest("NOT -5",myFalse);
        //valueTest("-!5==0",myTrue);
        //valueTest("-!0",-1.0);
        valueTest("T AND T",myTrue);
        valueTest("T AND F",myFalse);
        valueTest("F AND T",myFalse);
        valueTest("F AND F",myFalse);
        valueTest("T OR T",myTrue);
        valueTest("T OR F",myTrue);
        valueTest("F OR T",myTrue);
        valueTest("F OR F",myFalse);

        valueTest("T and T",myTrue);
        valueTest("T And F",myFalse);
        valueTest("F And T",myFalse);
        valueTest("F and F",myFalse);
        valueTest("T or T",myTrue);
        valueTest("T Or F",myTrue);
        valueTest("F oR T",myTrue);
        valueTest("F OR F",myFalse);
    }
    
    
    @Override
    public void testParseException() { /* empty */ }

    public void testIfThenElse() throws Exception {
	    valueTest("x=5",5.0);
	    Node n = jep.parse("IF x==5 THEN y=6 ELSE y=7");
	    Object res = jep.evaluate(n);
	    assertEquals(6.0,res);
	}
    
    /**
     * Tests if we have a conflict between OR and ORGANIZE
     * @throws Exception
     */
    @Test
    public void testOverlappingNames() throws Exception {
        ConfigurableParser cp = (ConfigurableParser) jep.getParser();
        List<Token> toks = cp.scan(new StringReader("A ORGINIZE()"));
        Token t = toks.get(2);
        assertEquals("ORGINIZE",t.getSource());
    }

    @Test
    public void test2010_03_01() throws Exception {
        //ConfigurableParser cp = (ConfigurableParser) jep.getParser();
        Node n = jep.parse("OR_4=\"KO\"");
        assertEquals("OR_4=\"KO\"","OR_4",n.jjtGetChild(0).getName());
        n = jep.parse("OR5=\"KO\"");
        assertEquals("OR5=\"KO\"","OR5",n.jjtGetChild(0).getName());
    }

    
    @Override
    @Test
    public void testLazyLogicalBug() throws Exception {
        valueTest("true AND 1",myTrue);
    }

    /**
     * Tests operators and functions which test if an element is in array or list.
     * @see com.singularsys.jep.misc.functions.ElementOf
     * @throws Exception
     */
    @Test
    public void testElementOf() throws Exception {
        valueTest("\"west\" IN [\"north\",\"south\",\"east\",\"west\"]",myTrue);
        valueTest("\"north\" IN [\"north\",\"south\",\"east\",\"west\"]",myTrue);
        valueTest("\"up\" IN [\"north\",\"south\",\"east\",\"west\"]",myFalse);

        valueTest("\"west\" NOTIN [\"north\",\"south\",\"east\",\"west\"]",myFalse);
        valueTest("\"north\" NOTIN [\"north\",\"south\",\"east\",\"west\"]",myFalse);
        valueTest("\"up\" NOTIN [\"north\",\"south\",\"east\",\"west\"]",myTrue);

        valueTest("inset(\"west\",\"north\",\"south\",\"east\",\"west\")",myTrue);
        valueTest("inset(\"north\",\"north\",\"south\",\"east\",\"west\")",myTrue);
        valueTest("inset(\"up\",\"north\",\"south\",\"east\",\"west\")",myFalse);

        valueTest("notinset(\"west\",\"north\",\"south\",\"east\",\"west\")",myFalse);
        valueTest("notinset(\"north\",\"north\",\"south\",\"east\",\"west\")",myFalse);
        valueTest("notinset(\"up\",\"north\",\"south\",\"east\",\"west\")",myTrue);

    }

    /**
     * Test the various type conversion functions.
     * @see ToBase
     * @see FromBase
     * @throws Exception
     */
    @Test
    public void testToBase() throws Exception {
        jep.addFunction("toBin", new ToBase(2));
        jep.addFunction("toHex", new ToBase(16,"0x"));
        jep.addFunction("toBase", new ToBase());
        jep.addVariable("x",Integer.valueOf(-11));
        jep.addVariable("y",Double.valueOf(-11));

        valueTestString("toBin(x)","-1011");
        valueTestString("toBase(x,2)","-1011");
        valueTestString("toHex(x)","-0xb");
        valueTestString("toBase(x,16)","-b");
        valueTestString("toBase(y,10,2)","-11.00");
        valueTestString("toBase(11,10,2)","11.00");
        valueTestString("toBase(11.5,10)","12");
        valueTestString("toBase(pi,10,3)","3.142");
        valueTestString("toBase(0,10,3)","0.000");
        
        jep.addFunction("fromDec",new FromBase(10));
        jep.addFunction("fromHex",new FromBase(16,"0x"));
        jep.addFunction("fromBase",new FromBase());
               
        valueTestString("fromDec(\"123.45\")","123.45");
        valueTestString("fromHex(\"-0xff\")","-255.0");
        valueTestString("fromBase(\"0377\",8)","255.0");
        
        
    }

    /**
     * Tests the suffix % operation
     * @see ExtendedOperatorSet
     * @throws Exception
     */
    @Test
    public void testPercent() throws Exception {
        valueTestString("10%","0.1");
        valueTestString("-10%","-0.1");
        valueTestString("2300+10%","2300.1");
    }
    
    /**
     * Test the suffix factorial operation
     * @see com.singularsys.jep.misc.functions.Factorial
     * @throws Exception
     */
    @Test
    public void testFactorial() throws Exception {
        valueTestString("5!","120");
        valueTestString("20!","2432902008176640000");
    }  
    
    /**
     * Test a combination of suffix operations
     * @throws Exception
     */
    @Test
    public void testSuffix() throws Exception {
        Node n = jep.parse("5!%");
        OperatorTable2 ot = (OperatorTable2)jep.getOperatorTable();
        assertEquals(ot.getOperator(ExtendedOperatorSet.ExtendedOperators.PERCENTKEY),
        		n.getOperator());
        assertEquals(ot.getOperator(ExtendedOperatorSet.ExtendedOperators.FACTKEY),
        		n.jjtGetChild(0).getOperator());
        // can't actually evaluate this a factorial returns a long
    }
    
 
	/**
	 * Tests for other symbols used.
	 * Minus sign is actually  unicode U+2013
	 * 
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
		String formula = "S = \u22121";
		jep.parse(formula);
		Object result = jep.evaluate();
		myAssertEquals(formula,-1.0,result);
		String s2 = "5−1";
		Node n2 = jep.parse(s2);
		Object res2 = jep.evaluate(n2);
		myAssertEquals(s2,4.0,res2);
		assertEquals("5.0-1.0",jep.toString(n2));
		String s3 = "5\u00d76";
		Node n3 = jep.parse(s3);
		Object res3 = jep.evaluate(n3);
		myAssertEquals(s3,30.0,res3);
		assertEquals("5.0*6.0",jep.toString(n3));

	}

	/**
	 * Tests mathematical style "cos pi" functions without brackets.
	 * @see SingleArgFunctionMatcher 
	 * @throws Exception
	 */
    @Test
    public void testSingleArgFun() throws Exception {
    	valueTest("-5",-5.0);
        valueTest("cos -pi",-1.0);
    	valueTest("sqrt 25", 5.0);
        valueTest("sqrt 25+7",12.0);
        valueTest("sqrt 25*7",35.0);
        valueTest("x=49",49.0);
        valueTest("sqrt x",7.0);
        valueTest("cos pi",-1.0);
    	valueTest("sqrt 36+sqrt 25", 11.0);
    	valueTest("sqrt 36-sqrt 25", 1.0);
    	valueTest("sqrt 36*sqrt 25", 30.0);
    	valueTest("sqrt 36/sqrt 25", 1.2);
    	valueTest("sqrt sqrt 81", 3.0);
    	try {
    		valueTest("sqrt / 81", 3.0);
    		fail("Exception should have been thrown");
    	} catch(GrammarException e) {
    		assertEquals(6,e.getColumnNumber());  
    	}
    }   

    @Test
    public void testNoComplex() throws JepException {
	jep = new Jep();
	jep.getVariableTable().remove("i");
	FunctionTable ft = jep.getFunctionTable();
	ft.addFunction("sqrt", new SquareRoot(true));
        ft.addFunction("log",   new Logarithm(true));
        ft.addFunction("ln",    new StrictNaturalLogarithm());
        ft.addFunction("lg",    new LogBase2(true));
        ft.addFunction("pow",   new Power(true));

        ft.addFunction("asin",  new ArcSine(true));
        ft.addFunction("acos",  new ArcCosine(true));
        ft.addFunction("acosh", new ArcCosineH(true));
        ft.addFunction("atanh", new ArcTanH(true));
        
        ft.remove("re");
        ft.remove("im");
        ft.remove("arg");
        ft.remove("cmod");
        ft.remove("complex");
        ft.remove("polar");
        ft.remove("conj");
        
        jep.getOperatorTable().getPower().setPFMC(new Power(true));

        System.out.println(ft);
        System.out.println(jep.getVariableTable());
	jep.parse("sqrt(-1)");
	Object res = jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("log(-1)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("ln(-1)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
		
    }
    
    @Test
    public void testRealConfig() throws JepException {
	jep = new Jep(new RealComponents());
	jep.parse("sqrt(-1)");
	Object res = jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("log(-1)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("ln(-1)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("lg(-1)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());

	jep.parse("asin(2)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("acos(2)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("acosh(0.5)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
	jep.parse("atanh(2)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());	
	
	assertNull(jep.getVariable("i"));

	jep.parse("i = (-1)^(.5)");
	res=jep.evaluate();
	org.junit.Assert.assertTrue(((Double) res).isNaN());
    }
}
