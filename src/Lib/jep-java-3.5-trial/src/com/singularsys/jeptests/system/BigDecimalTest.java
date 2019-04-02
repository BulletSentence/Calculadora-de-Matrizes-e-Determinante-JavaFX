/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;


import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.Stack;

import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.bigdecimal.BigDecComponents;
import com.singularsys.jep.bigdecimal.functions.BigDecAbs;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.misc.StringFunctionSet;
import com.singularsys.jep.parser.Node;

/**
 * Tests the BigDecComponents through a set of test expressions that are evaluated.
 * @author singularsys
 */
public class BigDecimalTest {
    Jep jep;
    //	PrefixTreeDumper dumper;
    int testCount = 0;

    //	public BigDecimalTest() {
    //		dumper = new PrefixTreeDumper();
    //	}

    @SuppressWarnings("unused")
    @Test
    public void unlimitedPrecisionTest() {
        jep = new Jep(new BigDecComponents());
        //		BigDecimal bd, bd2;
        //		bd = new BigDecimal("1.000000001");
        //		System.out.println(bd);
        //		bd = new BigDecimal("1.000000000000001");
        //		System.out.println(bd);
        //		bd = new BigDecimal("1.0000000000000000000000000000001");
        //		System.out.println(bd);
        //		bd = new BigDecimal("1.000000000000000000000000000000000000000000000001");
        //		System.out.println(bd);
        //		bd = new BigDecimal("1.00000000000000000000000000000000000000000000000000000000000000001");
        //		System.out.println(bd);
        //		bd = new BigDecimal("1.000000000000000000000000000000000000000000000000000000000000000000000000000000001");
        //		System.out.println(bd);
        //		bd2 = new BigDecimal("1e-300");
        //		System.out.println(bd.add(bd2));

        //		BigDecimal x = new BigDecimal(1);
        //		BigDecimal y = new BigDecimal(3);
        //		BigDecimal result = x.divide(y,MathContext.DECIMAL128);
        //		System.out.println(result);
        // init testCount
        testCount = 0;
        // addition
        test("1+1", "2");
        test("2+2", "4");
        test("1/2+1/2", "1.0");

        // subtraction
        test("1-1", "0");

        // unary minus
        test("-1", "-1");
        test("1+(-1)", "0");

        // multiplication
        test("10 * 0.09", "0.90");
        test("0.1 * 0.1", "0.01");

        // division
        test("1/2", "0.5");
        test("1/10", "0.1");
        test("3/3", "1");

        // power
        test("1^1", "1");
        test("1^2", "1");
        test("2^2", "4");
        test("2.01^1", "2.01");
        test(".1^2", "0.01");

        // equal
        test("1==1", true);
        test("1==2", false);
        test("1==-1", false);
        test("1.333333333333333333333333==1.333333333333333333333333", true);
        test("0 == 1-1.000000000000000000000000000000000000000000000000000000000000000001", false);
        test("1 == 1+1e-300", false);

        // not equal
        test("1 != 1", false);
        test("1 != 2", true);
        test("1 != -1", true);
        test("1.333 != 1.333", false);
        test("0 != 1-1.000000000000000000000000000000000000000000000000000000000000000001", true);
        test("1 != 1+1e-300", true);

        // less or equal
        test("1 <= 1", true);
        test("1 <= 2", true);
        test("1 <= -1", false);
        test("1.333333333333333333333333 <= 1.333333333333333333333333", true);
        test("1-1.000000000000000000000000000000000000000000000000000000000000000001 <= 0", true);
        test("1 <= 1+1e-300", true);
        test("1+1e-300 <= 1", false);

        // less than
        test("1 < 1", false);
        test("1 < 2", true);
        test("1 < -1", false);
        test("1.333 < 1.333", false);
        test("0 < 1-1.000000000000000000000000000000000000000000000000000000000000000001", false);
        test("1 < 1+1e-300", true);

        // greater or equal
        test("1 >= 1", 1 >= 1);
        test("1 >= 2", 1 >= 2);
        test("1 >= -1", 1 >= -1);
        test("1.333333333333333333333333 >= 1.333333333333333333333333", true);
        test("1.333333333333333333333333 > 1.333333333333333333333333", false);
        test("1.333333333333333333333333 < 1.333333333333333333333333", false);

        test("1-1.000000000000000000000000000000000000000000000000000000000000000001 >= 0", false);
        test("1 >= 1+1e-300", false);
        test("1+1e-300 >= 1", true);

        // greater than
        test("1 > 1", false);
        test("1 > 2", false);
        test("1 > -1", true);
        test("1.333 > 1.333", false);
        test("0 > 1-1.000000000000000000000000000000000000000000000000000000000000000001", true);
        test("1 > 1+1e-300", false);

        // and
        test("(1 > 1) && (1==1)", false);
        test("1 && 0", false);
        test("0 && 1", false);
        test("0 && 0", false);
        test("1 && 1", true);
        test("3 && 4", true);
        test("1<3 && 3<4", true);
        test("(1+1e-300)-1 && 1", true);

        // or
        test("(1 > 1) || (1==1)", true ); // (1 > 1) || (1==1));
        test("1 || 0", true);
        test("0 || 1", true);
        test("0 || 0", false);
        test("1 || 1", true);
        test("3 || 0", true);
        test("1<3 || 3<1", 1<3 || 3<1);
        test("(1+1e-300)-1 || 0", true);

        // not
        test("!1", false);
        test("!0", true);
        test("!(1>0)", false);
        test("!(1<0)", true);

        

        // print summary
        System.out.println("\n------------------------------------------------");
        System.out.println(testCount + " tests performed successfully.");
    }

    /**
     * Check for problems where MinMax used comparision which worked by
     * first coverting to doubles.
     */
    @Test
    public void cmpBug() {
        jep = new Jep(new BigDecComponents(MathContext.DECIMAL128));

        // Should be good for 34 digits
        
        //           1234567890123456780
        String s1 = "12345678900012345678";
        String s2 = "12345678900012345679";

        test(s1 + "==" + s2, false);
        test(s1 + "<" + s2, true);
        test("min("+s1+","+s2+")",s1);

    }
    
    @Test
    public void limitedPrecisionTest() {
        jep = new Jep(new BigDecComponents(MathContext.DECIMAL64));
        // init testCount
        testCount = 0;
        System.out.println("\n-- Limited precision test ----------------------------------------------");
        test("10^-1", "0.1");
        test("10^-2", "0.01");
        test("1/3", "0.3333333333333333");
        test("2/3", "0.6666666666666667");

        // print summary
        System.out.println("\n------------------------------------------------");
        System.out.println(testCount + " tests performed successfully.");
    }

    /**
     * Test expression against boolean
     * @param str
     * @param value
     */
    public void test(String str, boolean value) {
        boolean bresult;
        Object result = eval(str);

        if (result instanceof Boolean) {
            bresult = (Boolean)result;
        } else if (result instanceof Double) {
            bresult = ((Double)result).doubleValue() != 0;
        } else {
            fail("Result isn't Boolean or Double");
            bresult = false;
        }
        assertEquals(bresult, value);
        testCount++;		
    }

    public void test(String str, String value) {
        BigDecimal trueVal = new BigDecimal(value);
        Object result = eval(str);
        // fail if result isn't a BigDecimal
        if (!(result instanceof BigDecimal)) fail("Result isn't BigDecimal");
        BigDecimal bdresult = (BigDecimal)result;
        assertEquals(trueVal,bdresult);
        testCount++;
    }

    public Object eval(String str) {
        System.out.print("\"" + str + "\"  ->  ");
        // try parsing
        try {
            jep.parse(str);
        } catch (ParseException e) {
            // parsing failed
            fail("Parsing failed");
            return null;
        }

        // try evaluating
        try {
            // evaluate
            Object result = jep.evaluate();
            System.out.println(result);
            //			// dump the tree
            //			try {
            //				dumper.walk(jep.getLastRootNode());
            //			} catch (Exception e) {
            //				e.printStackTrace();
            //			}
            return result;
        } catch (EvaluationException e) {
            // evaluation failed, so fail
            fail("Evaluation failed "+e.toString());
            return null;
        }
    }

    @Test
    public void testCP() throws Exception
    {
        jep = new Jep(new BigDecComponents(MathContext.DECIMAL64));
        jep.setComponent(new StandardConfigurableParser());
        Node n = jep.parse("+1");
        Object val = jep.evaluate(n);
        System.out.println(val);
    }

    public void exprEquals(String expr,Object expected) throws JepException {
        Node eqn = jep.parse(expr);
        Object res = jep.evaluate(eqn);
        if(expected.equals(res)) {
            System.out.println("\""+expr+"\" -> "+res.toString());
        }
        else
            System.out.println("ERROR \""+expr+"\" -> "+res.toString() + " expected "+expected.toString());
        
        assertEquals(expr,expected,res); 
    }
    
    public void evalExceptionTest(String expr) throws JepException {
        Node eqn = jep.parse(expr);
        try 
        {
            Object res = jep.evaluate(eqn);
            System.out.println("ERROR: \""+expr+"\" an evaluation should have been thrown. Result "+res);
            fail("ERROR: \""+expr+"\" an evaluation should have been thrown. Result "+res);
        }
        catch(EvaluationException e) {
            System.out.println("\""+expr+"\" raised expected exception "+e.toString());
            
        }
    }
    
    @Test
    public void testSetAllowStrings() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64);
        jep = new Jep(compSet);
        // test default (should not allow)
        evalExceptionTest("\"ABCD\"==\"ABCD\"");
        
        // try setting to allow
        compSet.setAllowStrings(true);
        exprEquals("\"ABCD\"==\"ABCD\"",Boolean.TRUE);
    }
    
    @Test
    public void testBoolean() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64,true);
        jep = new Jep(compSet);
        jep.addConstant("true",Boolean.TRUE);
        jep.addConstant("false",Boolean.FALSE);

        exprEquals("!true",Boolean.FALSE);
        exprEquals("!false",Boolean.TRUE);

        exprEquals("true==true",Boolean.TRUE);
        exprEquals("true!=true",Boolean.FALSE);
        exprEquals("true<true",Boolean.FALSE);
        exprEquals("true<=true",Boolean.TRUE);
        exprEquals("true>true",Boolean.FALSE);
        exprEquals("true>=true",Boolean.TRUE);
      
        exprEquals("false==false",Boolean.TRUE);
        exprEquals("false!=false",Boolean.FALSE);
        exprEquals("false<false",Boolean.FALSE);
        exprEquals("false<=false",Boolean.TRUE);
        exprEquals("false>false",Boolean.FALSE);
        exprEquals("false>=false",Boolean.TRUE);
      
        exprEquals("true == false",Boolean.FALSE);
        exprEquals("true != false",Boolean.TRUE);
        exprEquals("true <  false",Boolean.FALSE);
        exprEquals("true <= false",Boolean.FALSE);
        exprEquals("true >  false",Boolean.TRUE);
        exprEquals("true >= false",Boolean.TRUE);

        exprEquals("false == true",Boolean.FALSE);
        exprEquals("false != true",Boolean.TRUE);
        exprEquals("false <  true",Boolean.TRUE);
        exprEquals("false <= true",Boolean.TRUE);
        exprEquals("false >  true",Boolean.FALSE);
        exprEquals("false >= true",Boolean.FALSE);

        evalExceptionTest("123==false");
        evalExceptionTest("false==123");
        evalExceptionTest("123+false");
        evalExceptionTest("false+123");

    }
    /**
     * Tests inter-operability of big decimals and strings.
     * @throws Exception
     */
    @Test
    public void testBDString() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);
        jep.setComponent(new StringFunctionSet());
        jep.addConstant("true",Boolean.TRUE);
        jep.addConstant("false",Boolean.FALSE);

        test("1+1","2");
        test("1+1==2",true);
        test("1+1==3",false);
        
        String expr = "left(\"abcdef\",3)";
        exprEquals(expr,"abc");
        
        exprEquals("\"ABCD\"==\"ABCD\"",Boolean.TRUE);
        exprEquals("\"ABCD\"==\"ABCDE\"",Boolean.FALSE);
        exprEquals("left(\"abcdef\",3) == \"abc\"",Boolean.TRUE);
        
        evalExceptionTest("\"ABCD\"==123");
        evalExceptionTest("123==\"ABCD\"");
        exprEquals("123==123",Boolean.TRUE);
        exprEquals("123==423",Boolean.FALSE);
        

        exprEquals("\"ABCD\"!=\"ABCD\"",Boolean.FALSE);
        exprEquals("\"ABCD\"!=\"ABCDE\"",Boolean.TRUE);
        evalExceptionTest("\"ABCD\"!=123");
        evalExceptionTest("123!=\"ABCD\"");
        exprEquals("123!=123",Boolean.FALSE);
        exprEquals("123!=423",Boolean.TRUE);

        exprEquals("\"ABCD\"+\"ABCD\"","ABCDABCD");
        evalExceptionTest("\"ABCD\"+123");
        evalExceptionTest("123+\"ABCD\"");
        
        exprEquals("\"ABC\" < \"DEF\"",Boolean.TRUE);
        exprEquals("\"ABC\" <= \"DEF\"",Boolean.TRUE);
        exprEquals("\"ABC\" > \"DEF\"",Boolean.FALSE);
        exprEquals("\"ABC\" >= \"DEF\"",Boolean.FALSE);
        evalExceptionTest("123<\"ABCD\"");
        evalExceptionTest("\"ABCD\"<123");
        evalExceptionTest("false<\"ABCD\"");
        evalExceptionTest("\"ABCD\"<false");
    }
    
    
    @Test
    public void Abs() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);
        jep.getFunctionTable().addFunction("bdabs",new BigDecAbs());
        
        // using a seperate function
        test("bdabs(2)","2");
        test("bdabs(-3)","3");
        test("bdabs(2.222)","2.222");
        test("bdabs(-3.333)","3.333");

        // using function from the component set
        test("abs(2)","2");
        test("abs(-3)","3");
        test("abs(2.222)","2.222");
        test("abs(-3.333)","3.333");

    }

    @Test
    public void Round() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);
        
        test("round(8-7.9)","0");
        test("round(8-7.9,3)","0.100");
        test("round(12.3456,1)","12.3");
        test("round(12.3456,2)","12.35");
        test("round(12.3456,3)","12.346");
        test("round(2.5)","3"); // Default is half up
        test("round(3.5)","4");
        test("round(1.25,1)","1.3");
        test("round(1.35,1)","1.4");
        test("round(-2.5)","-2"); 
        test("round(-3.5)","-3");
        test("round(-1.25,1)","-1.2");
        test("round(-1.35,1)","-1.3");
        test("rint(2.5)","2"); // Set with half even
        test("rint(3.5)","4");
        test("rint(1.25,1)","1.2");
        test("rint(1.35,1)","1.4");
        test("rint(-2.5)","-2"); // Set with half even
        test("rint(-3.5)","-4");
        test("rint(-1.25,1)","-1.2");
        test("rint(-1.35,1)","-1.4");

        test("roundSF(123.456,3)","123");
        test("roundSF(123.456,4)","123.5");
        test("roundSF(123.456,5)","123.46");
        test("roundSF(123.456,6)","123.456");
        test("roundSF(123.456,7)","123.4560");
        test("roundSF(123456,3)","1.23E+5");
        test("roundSF(123456,7)","123456.0");

        test("roundSF(-123.456,3)","-123");
        test("roundSF(-123.456,4)","-123.5");
        test("roundSF(-123.456,5)","-123.46");
        test("roundSF(-123.456,6)","-123.456");
        test("roundSF(-123.456,7)","-123.4560");
        test("roundSF(-123456,3)","-1.23E+5");
        test("roundSF(-123456,7)","-123456.0");

        test("roundSF(1.25,2)","1.3");
        test("roundSF(1.35,2)","1.4");
        test("roundSF(-1.25,2)","-1.3");
        test("roundSF(-1.35,2)","-1.4");
    }
    @Test
    public void Signum() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);


        test("signum(1)","1");
        test("signum(1.0)","1");
        test("signum(100)","1");
        test("signum(0)","0");
        test("signum(0.0)","0");
        test("signum(-0)","0");
        test("signum(-1)","-1");
        test("signum(-100)","-1");

    }

    @Test
    public void MinMax() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);


        test("min(1)","1");
        test("min(1,2,3)","1");
        test("min(3,2,1)","1");
        test("min(2.0,2,2.00)","2");
        test("min(2.00,2.0,2)","2");
        test("max(2.0,2,2.00)","2");
        test("max(2.00,2.0,2)","2");
    }

    @Test
    public void Avg() throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);
        
        test("avg(1.0,2.0,3.0)","2.0");
        test("avg(3.0,4.0,5.0,6.0)","4.5");
        test("avg(3.0,4.0,5.00,6.00)","4.50");
        test("avg(3.0,4.0,5.0,6.1)","4.525");
    }
    
    
    @Test
    public void testIf()  throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);

        test("if(1,2,3)","2");		
        test("if(1.0,2.0,3.0)","2.0");		
        test("if(-1,2,3)","3");		
        test("if(0,2,3)","3");		
        test("if(3>4,2,3)","3");		
        test("if(3<4,2,3)","2");		
        test("if(1,2,3,4)","2");		
        test("if(-1,2,3,4)","3");		
        test("if(0,2,3,4)","4");		
        test("if(0>=0,2,3,4)","2");		
        test("x=3","3");		
        test("if(x==3,1,-1)","1");		
        test("if(x!=3,1,-1)","-1");		
        test("if(x>=3,1,-1)","1");		
        test("if(x>3,1,-1)","-1");		
        test("if(x<=3,1,-1)","1");		
        test("if(x<3,1,-1)","-1");		
    }

    @Test
    public void testCoerce()  throws Exception
    {
        BigDecComponents compSet = new BigDecComponents(MathContext.DECIMAL64, true);
        jep = new Jep(compSet);

        Stack<Object> stack = new Stack<>(); 
        PostfixMathCommandI pfmc =  jep.getFunctionTable().getFunction("min");
        stack.push(Double.valueOf(3));
        stack.push(BigDecimal.valueOf(2));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(BigDecimal.valueOf(2),stack.pop());

        stack.push(BigDecimal.valueOf(5));
        stack.push(Double.valueOf(4));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(Double.valueOf(4),stack.pop());

        stack.push(Double.valueOf(6));
        stack.push(BigDecimal.valueOf(7));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(Double.valueOf(6),stack.pop());

        stack.push(Double.valueOf(2));
        stack.push(BigDecimal.valueOf(2));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(BigDecimal.valueOf(2),stack.pop());

        stack.push(BigDecimal.valueOf(2));
        stack.push(Double.valueOf(2));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(BigDecimal.valueOf(2),stack.pop());

        stack.push(new BigDecimal("2.00"));
        stack.push(Double.valueOf(2));
        pfmc.setCurNumberOfParameters(2);
        pfmc.run(stack);
        assertEquals(Double.valueOf(2),stack.pop());

        Object res;
        
        NaryBinaryFunction add = (NaryBinaryFunction) jep.getOperatorTable().getAdd().getPFMC(); 
        res = add.eval(BigDecimal.valueOf(2.1), Double.valueOf(1));
        assertEquals(BigDecimal.valueOf(3.1),res);
        
        res = add.eval(Double.valueOf(2.1), BigDecimal.valueOf(1));
        assertEquals(BigDecimal.valueOf(3.1),res);
        
        Object[] vals = new Object[]{
        		Double.valueOf(1),
        		BigDecimal.valueOf(2),
        		Double.valueOf(3)
        };
        res = add.eval(vals);
        assertEquals(BigDecimal.valueOf(6.0),res);
        
        BinaryFunction sub = (BinaryFunction) jep.getOperatorTable().getSubtract().getPFMC(); 
        res = sub.eval(BigDecimal.valueOf(2.1), Double.valueOf(1));
        assertEquals(BigDecimal.valueOf(1.1),res);
        
        res = sub.eval(Double.valueOf(2.1), BigDecimal.valueOf(1));
        assertEquals(BigDecimal.valueOf(1.1),res);
        
        NaryBinaryFunction mul = (NaryBinaryFunction) jep.getOperatorTable().getMultiply().getPFMC(); 
        res = mul.eval(BigDecimal.valueOf(2.1), Double.valueOf(2.0));
        assertEquals(new BigDecimal("4.20"),res);
        
        res = mul.eval(Double.valueOf(2.1), BigDecimal.valueOf(2.0));
        assertEquals(new BigDecimal("4.20"),res);
        
        vals = new Object[]{
        		Double.valueOf(2),
        		BigDecimal.valueOf(3),
        		Double.valueOf(4)
        };
        res = mul.eval(vals);
        assertEquals(new BigDecimal("24.00"),res);
        
        BinaryFunction div = (BinaryFunction) jep.getOperatorTable().getDivide().getPFMC(); 
        res = div.eval(BigDecimal.valueOf(2.2), Double.valueOf(2));
        assertEquals(BigDecimal.valueOf(1.1),res);
        
        res = div.eval(Double.valueOf(2.2), BigDecimal.valueOf(2));
        assertEquals(BigDecimal.valueOf(1.1),res);

        BinaryFunction mod = (BinaryFunction) jep.getOperatorTable().getMod().getPFMC(); 
        res = mod.eval(BigDecimal.valueOf(3.2), Double.valueOf(2));
        assertEquals(BigDecimal.valueOf(1.2),res);

        res = mod.eval(BigDecimal.valueOf(3.2), Integer.valueOf(2));
        assertEquals(BigDecimal.valueOf(1.2),res);

        res = mod.eval(Double.valueOf(3.2), BigDecimal.valueOf(2));
        assertEquals(BigDecimal.valueOf(1.2),res);

        BinaryFunction pow = (BinaryFunction) jep.getOperatorTable().getPower().getPFMC(); 
        res = pow.eval(BigDecimal.valueOf(3.0), Double.valueOf(2));
        assertEquals(new BigDecimal("9.00"),res);

        res = pow.eval(BigDecimal.valueOf(3.0), Integer.valueOf(2));
        assertEquals(new BigDecimal("9.00"),res);

        res = pow.eval(Double.valueOf(3.0), BigDecimal.valueOf(2));
        assertEquals(new BigDecimal("9.00"),res);

    }
}
