/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 1 Jul 2011 - Richard Morris
 */
package com.singularsys.jeptests.system;

import static java.lang.System.out;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.text.NumberFormat;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Locale;
import java.util.Stack;
import java.util.Vector;

import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.EmptyOperatorTable.OperatorKey;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.NumberFactory;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Parser;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.TernaryOperator;
import com.singularsys.jep.configurableparser.matchers.StringTokenMatcher;
import com.singularsys.jep.functions.Average;
import com.singularsys.jep.functions.LazyLogical;
import com.singularsys.jep.functions.PostfixMathCommand;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.misc.MacroFunction;
import com.singularsys.jep.misc.NullParser;
import com.singularsys.jep.misc.StringFunctionSet;
import com.singularsys.jep.misc.VariableTableObserver;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.ConstantFunction;
import com.singularsys.jep.misc.functions.Factorial;
import com.singularsys.jep.misc.functions.FromBase;
import com.singularsys.jep.misc.functions.LogTwoArg;
import com.singularsys.jep.misc.functions.RoundSF;
import com.singularsys.jep.misc.functions.Switch;
import com.singularsys.jep.misc.functions.SwitchDefault;
import com.singularsys.jep.misc.functions.ToBase;
import com.singularsys.jep.misc.javaops.JavaOperatorTable;
import com.singularsys.jep.misc.javaops.TernaryConditional;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.ASTOpNode;
import com.singularsys.jep.parser.ASTStart;
import com.singularsys.jep.parser.JccParserTreeConstants;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealEvaluator;
import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.walkers.DoNothingVisitor;
import com.singularsys.jep.walkers.SerializableExpression;
import com.singularsys.jep.walkers.SubstitutionVisitor;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * This class should print out all messages produced by Jep. 
 * It also checks that the appropriate exceptions are thrown and the test will fail if they are not.
 * @author rich
 */
public class MessagesTest {

    @Test
    public void testLocale() {
    	out.println("The default locale is "+Locale.getDefault());
//    	out.println(System.getenv().toString().replace(", ", "\n"));
//    	out.println(System.getProperties().toString().replace(", ", "\n"));
    	out.println();
    }

    @Test
    public void testConfigurableParserMessages() {
        out.println("ConfigurableParser messages");

        Jep jep = new Jep(new StandardConfigurableParser());

        try {
            Node n = jep.parse("");
            fail("Did not catch empty input"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("atan2(1,2,3,4,5)");
            fail("too many arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("if(1,2,3,4,5)");
            fail("too many arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        out.println();
    }

    @Test
    public void testMatchersMessages() {
        out.println("Matchers messages");

        Jep jep = new Jep(new StandardConfigurableParser());
        try {
            Node n = jep.parse("x[2");
            fail("Did not catch array access"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("x[2)");
            fail("Did not catch array access"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("sin(2");
            fail("Did not catch function"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("sin(2]");
            fail("Did not catch function"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("sin()");
            fail("Did not catch function arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("sin(1,2)");
            fail("Did not catch function arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("[1,2)");
            fail("Did not catch list arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("(1,2)");
            fail("Did not catch brackets arguments"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("\"\\z\"");
            fail("Did not catch illegal escape"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        out.println();

    }

    @Test
    public void testShuntingYardMessages() {
        out.println("ShuntingYard messages");

        Jep jep = new Jep(new StandardConfigurableParser());
        //((StandardConfigurableParser) jep.getParser()).setGrammarParserFactory(new LNShuntingYard.LNShuntingYardGrammarParserFactory());
        jep.setImplicitMul(false);
        try {
            Node n = jep.parse("x y");
            fail("Did not catch no imp mul"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        TernaryOperator to = new TernaryOperator("conditional", "?", ":", 
                new TernaryConditional(), Operator.TERNARY+Operator.NARY+Operator.LEFT,9);

        EmptyOperatorTable eot = (EmptyOperatorTable) jep.getOperatorTable();
        eot.insertOperator(new OperatorKey(){/* */},to,eot.getAssign());
        jep.reinitializeComponents();
        try {
            Node n = jep.parse("x+y:z");
            fail("Did not catch unmatched ternary op"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("sin(x+y:z)");
            fail("Did not catch unmatched ternary op"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("x?y=z");
            fail("Did not catch unexpected ternary op"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("x!");
            fail("Did not catch unexpected ternary op"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("+*");
            fail("Did not catch unexpected EOF"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("!");
            fail("Did not catch unexpected EOF"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("! /* multiline\ncomment*/");
            fail("Did not catch unexpected EOF"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        out.println();
    }

    @Test
    public void testTokenizerMessages() {
        out.println("Tokenizer messages");
        Jep jep = new Jep(new StandardConfigurableParser());

        String s = "/* unclosed comment";
        try {
            Node n = jep.parse(s);
            fail("Didn't catch unclosed comment"+n);

        } catch(ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("@");
            fail("Didn't catch unmatched text"+n);

        } catch(ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = jep.parse("\"\\u000\"");
            fail("Did not catch illegal escape"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            Node n = jep.parse("\"\\u000u\"");
            fail("Did not catch illegal escape"+n);
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            StringTokenMatcher stm = StringTokenMatcher.doubleQuoteStringMatcher();
            stm.buildToken("a");
            fail("Did not catch bad call");
        } catch (Exception e) {
            out.println(e.toString());
        }
        
        out.println();
    }

    //@SuppressWarnings("unchecked")
	@Test
    public void testFunctionEleMessages() {
        Jep jep = new Jep(/*new StandardConfigurableParser()*/);
        out.println("Ele messages");
        NodeFactory nf = jep.getNodeFactory();
        OperatorTableI ot = jep.getOperatorTable();
        try {
            Node n = nf.buildOperatorNode(ot.getAssign(),
                    nf.buildOperatorNode(ot.getEle(),
                            nf.buildConstantNode(3.),
                            nf.buildConstantNode(4.)),
                            nf.buildConstantNode(5.));
            jep.evaluate(n);
            fail("Didn't catch ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
			jep.addVariable("null", null);
		} catch (JepException e1) {
		}
        ((FastEvaluator)jep.getEvaluator()).setTrapNullValues(false);
        

        try {
            jep.parse("x[1,2]=3");
            jep.evaluate();
            fail("Didn't catch ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            jep.parse("x[null]=3");
            jep.evaluate();
            fail("Didn't catch ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
            jep.parse("x[[1,2]]=3");
            jep.evaluate();
            fail("Didn't catch ele problem");
        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            jep.parse("x[2 i]=3");
            jep.evaluate();
            fail("Didn't catch ele problem");
        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            jep.parse("x[1]=3");
            jep.addVariable("x", 5.);
            jep.evaluate();
            fail("Didn't catch ele problem");
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }
        try {
            jep.parse("x[]=3");
            jep.addVariable("x", 5.);
            jep.evaluate();
            fail("Didn't catch ele problem");
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getAssign(),
                    nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("x"),
                              nf.buildConstantNode(new Complex(2.,3.)),
                              nf.buildConstantNode(4.) ),
                            nf.buildConstantNode(5.));
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getAssign(),
                    nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("x"),
                              nf.buildConstantNode(3.),
                              nf.buildConstantNode(4.) ),
                            nf.buildConstantNode(5.));
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                            nf.buildConstantNode(3.),
                            nf.buildConstantNode(4.));
                     
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(ParseException e) {
            fail(e.toString());
        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            jep.parse("x[1,2]");
            jep.addVariable("x", new Vector<Object>(Arrays.asList(1.,2.)));
            jep.evaluate();
            fail("Didn't catch ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("x[5]");
            jep.addVariable("x", new Vector<Object>(Arrays.asList(1.,2.)));
            jep.evaluate();
            fail("Didn't catch ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("x[5]=7");
            jep.addVariable("x", new Vector<Object>(Arrays.asList(1.,2.)));
            jep.evaluate();
            fail("Didn't catch ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildConstantNode(2.),
                              nf.buildConstantNode(3.),
                              nf.buildConstantNode(4.) );

            jep.addVariable("m", new Vector<>(Arrays.asList(new Vector<Object>(Arrays.asList(1.,2.)),new Vector<Object>(Arrays.asList(1.,2.)))));
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("m"),
                              nf.buildConstantNode(3.),
                              nf.buildConstantNode(4.) );

            jep.addVariable("m", new Vector<>(Arrays.asList(new Vector<>(Arrays.asList(1.,2.)),new Vector<>(Arrays.asList(1.,2.)))));
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("m"),
                              nf.buildConstantNode(1.),
                              nf.buildConstantNode(1.) );

            jep.addVariable("m", new Vector<Object>(Arrays.asList(1.,2.)));
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("m"),
                              nf.buildConstantNode(1.),
                              nf.buildConstantNode(3.) );

            jep.addVariable("m", new Vector<>(Arrays.asList(new Vector<Object>(Arrays.asList(1.,2.)),new Vector<Object>(Arrays.asList(1.,2.)))));
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("m"),
                              nf.buildConstantNode(1.),
                              nf.buildConstantNode(2.),
                              nf.buildConstantNode(3.) );

            jep.addVariable("m", 5.);
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }

        try {
            Node n = nf.buildOperatorNode(ot.getEle(),
                              nf.buildVariableNode("m"),
                              nf.buildConstantNode(5.),
                              nf.buildConstantNode(6.),
                              nf.buildConstantNode(7.) );
            Vector<Object> r1 = new Vector<Object>(Arrays.asList(1.,2.));
            Vector<Object> r2 = new Vector<Object>(Arrays.asList(1.,2.));
            jep.addVariable("m", new Vector<>(Arrays.asList(r1,r2)));
            
            jep.evaluate(n);
            fail("Didn't ele problem");

        } catch(EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch(JepException e) {
            fail(e.toString());
        }
       
        out.println();
    }

    @Test
    public void testFunctionMessages() {
        Jep jep = new Jep();
        FunctionTable ft = jep.getFunctionTable();
        NodeFactory nf = jep.getNodeFactory();
        out.println("Specific function messages");
        //out.print(ft.toString());
        
        try {
            jep.parse("signum(\"abc\")");
            jep.evaluate();
            fail("Did not catch signum");
        } catch (EvaluationException e) {
        	
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }
        
        try {
            Average avg = (Average) ft.getFunction("avg");
            List<Object> vals = new ArrayList<>();
            avg.average(vals);
            fail("Did not catch average");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            PostfixMathCommandI sum =  ft.getFunction("sum");
            Stack<Object> vals = new Stack<>();
            sum.setCurNumberOfParameters(0);
            sum.run(vals);
            fail("Did not catch average");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            //jep.parse("if(1)");
        	//ASTFunNode n = nf.buildFunctionNode("if", ft.getFunction("if"), new Node[]{});
        	ASTFunNode n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
        	n.setFunction("if", ft.getFunction("if"));
            jep.evaluate(n);
            fail("Did not catch average");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
        	Node n = jep.parse("if(1,2,3)");
        	n.getPFMC().run(new Stack<>());
        	jep.evaluate(n);
        	fail("Did not catch average");
        } catch (EvaluationException e) {
        	out.println(e.getLocalizedMessage());

        } catch (JepException e) {
        	fail(e.toString());
        }

        try {
            jep.parse("if([1,4],2,3)");
            jep.evaluate();
            fail("Did not catch if");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[2,3]^^[1,2,3]");
            jep.evaluate();
            fail("Did not catch cross");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[1,2,3,4]^^[1,2,3,4]");
            jep.evaluate();
            fail("Did not catch cross");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[ ]^^[ ]");
            jep.evaluate();
            fail("Did not catch cross");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[2,3].[1,2,3]");
            jep.evaluate();
            fail("Did not catch dot");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[ ].[ ]");
            jep.evaluate();
            fail("Did not catch dot");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[2,3]-[1,2,3]");
            jep.evaluate();
            fail("Did not catch average");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("(3+2i)<(4+5i)");
            jep.evaluate();
            fail("Did not catch compare");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[3,2]<[4,5]");
            jep.evaluate();
            fail("Did not catch compare");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("[3,2]||[4,5]");
            jep.evaluate();
            fail("Did not catch compare");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }
        try {
            jep.parse("1&&[4,5]");
            jep.evaluate();
            fail("Did not catch compare");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        jep.getOperatorTable().getAnd().setPFMC(new LazyLogical(LazyLogical.AND,true));
        try {
            jep.parse("1&&[4,5]");
            jep.evaluate();
            fail("Did not catch compare");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("round(2+3i)");
            jep.evaluate();
            fail("Did not catch round");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("binom(2,3)");
            jep.evaluate();
            fail("Did not catch binom");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
        	Node n = nf.buildOperatorNode(jep.getOperatorTable().getAssign(),
        			nf.buildConstantNode(3.),
        			nf.buildConstantNode(5.));
            jep.evaluate(n);
            fail("Did not catch assign");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("pi=5");
            jep.evaluate();
            fail("Did not catch assign");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        jep.setComponent(new StringFunctionSet());
        try {
            jep.parse("left(\"abcdef\",-1)");
            jep.evaluate();
            fail("Did not catch left");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("right(\"abcdef\",-1)");
            jep.evaluate();
            fail("Did not catch right");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("mid(\"abcdef\",7,2)");
            jep.evaluate();
            fail("Did not catch mid");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("mid(\"abcdef\",2,-7)");
            jep.evaluate();
            fail("Did not catch mid");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("substr(\"abcdef\",7,2)");
            jep.evaluate();
            fail("Did not catch substring");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("substr(\"abcdef\",2,7)");
            jep.evaluate();
            fail("Did not catch substring");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
        	jep.addFunction("fact", new Factorial());
            jep.parse("fact(20)");
            jep.evaluate();
        } catch (EvaluationException e) {
            fail(e.toString());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("fact(21)");
            jep.evaluate();
            
            fail("Did not catch factorial(21)");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        out.println();
    }

    @Test
    public void testMiscMessages() {
        Jep jep = new Jep(new StandardConfigurableParser(),new JavaOperatorTable());
        out.println("Misc function and operator messages");

        try {
            jep.parse("pi+=5");
            jep.evaluate();
            fail("Did not catch op equals");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("6+=5");
            jep.evaluate();
            fail("Did not catch op equals");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("x+=[5,3]");
            jep.addVariable("x", 5.);
            jep.evaluate();
            fail("Did not catch op equals");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        jep.addFunction("switch",new Switch());
        jep.addFunction("case",new Case());
        jep.addFunction("switchd",new SwitchDefault());
        jep.addFunction("toBase",new ToBase());
        jep.addFunction("log2",new LogTwoArg());

        try {
            jep.parse("switch(5,1,2,3)");
            jep.evaluate();
            fail("Did not catch switch");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("switch(1.5,1,2,3)");
            jep.evaluate();
            fail("Did not catch switch");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("switchd(-5,1,2,3)");
            jep.evaluate();
            fail("Did not catch switchd");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("case(1.5,1,2,3,4)");
            jep.evaluate();
            fail("Did not catch case");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        try {
            jep.parse("toBase(15,-3)");
            jep.evaluate();
            fail("Did not catch toBase");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        jep.addFunction("log2",new LogTwoArg());
        jep.addFunction("fromHex",new FromBase(16,"0x"));
        try {
            jep.parse("fromHex(\"15\")");
            jep.evaluate();
            fail("Did not catch fromHex");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }

        MacroFunction fact = new MacroFunction(
                "fact",new String[]{"x"}, 
                "if(x>1,x*fact(x-1),1)");
        jep.addFunction("fact",fact);
        try {
            jep.parse("fact(5)");
            jep.evaluate();
            fail("Did not catch MacroFunction");
        } catch (EvaluationException e) {
            out.println(e.getLocalizedMessage());
        } catch (JepException e) {
            fail(e.toString());
        }
        
        Parser p = jep.getParser();
        jep.getFunctionTable().remove("fact"); // Need to remove this before setting a null parser
        jep.setComponent(NullParser.NULL_PARSER);
        try {
            jep.parse("sin(5)");
            fail("Did not catch NullParser");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        jep.setComponent(p);
        
        jep.getVariableTable().addObserver(new VariableTableObserver(jep));
        try {
            jep.addVariable("x", 2*Math.PI);
            jep.parse("x=7");
            jep.evaluate();
            jep.addVariable("w", Math.PI);
            jep.getVariableTable().remove("w");
            jep.getVariableTable().clear();
        } catch (JepException e) {
            fail(e.toString());
        }
        out.println();
    }
    
    @Test
    public void testFunctionTableMessages() {
        Jep jep = new Jep();
        FunctionTable ft = jep.getFunctionTable();
        out.println("FunctionTable messages");
        out.print(ft.toString());
        out.println();
    }

    @Test
    public void testJepMessages() {
        Jep jep = new Jep();
        out.println("Jep messages");

        try {
            jep.parse("2+3 i");
            jep.evaluateD();
            fail("Did not catch bad double conversion");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }
        out.println();
    }

    @Test
    public void testNodeFactoryMessages() {
        out.println("NodeFactory messages");
        Jep jep = new Jep();
        NodeFactory nf = jep.getNodeFactory();
        FunctionTable ft = jep.getFunctionTable();
        jep.setAllowUndeclared(false);

        Node n=null;

        try {
            n = nf.buildFunctionNode("sin", ft.getFunction("sin"), 
                   nf.buildConstantNode(1.0),nf.buildConstantNode(2.0));
            fail("Did not catch too many arguments"+n.toString());
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = nf.buildVariableNodeCheckUndeclared("x");
            fail("Did not catch undeclared variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        out.println();
    }

    @Test
    public void testOperatorMessages() {
        out.println("OperatorTable messages");
        Jep jep = new Jep(new StandardConfigurableParser());
        OperatorTableI ot = jep.getOperatorTable();
        ot.getSubtract().addAltSymbol("\u2013");
        ot.getUMinus().addAltSymbol("\u2013");
        ot.getSubtract().addAltSymbol("\u2212");
        ot.getUMinus().addAltSymbol("\u2212");
        TernaryOperator to = new TernaryOperator("conditional", "?", ":", 
                new TernaryConditional(), Operator.TERNARY+Operator.NARY+Operator.LEFT,9);
        ((EmptyOperatorTable) ot).insertOperator(new OperatorKey(){/* */},to,ot.getAssign());

        jep.reinitializeComponents();

        out.print(jep.getOperatorTable());

        out.println();
    }

    @Test
    public void testASTNodeMessages() {
        out.println("parser.AST Node messages");
        Jep jep = new Jep();
        NodeFactory nf = jep.getNodeFactory();
        Node cn=null,vn=null,on=null,fn=null;
        try {
            cn= nf.buildConstantNode(1.0);
            out.println(cn.toString());
        } catch (ParseException e) {
            fail(e.toString());
        }
        try {
            vn= nf.buildVariableNode("x");
            out.println(vn.toString());
        } catch (ParseException e) {
            fail(e.toString());
        }
        try {
            on= nf.buildOperatorNode(jep.getOperatorTable().getAdd(), cn, vn);
            out.println(on.toString());
        } catch (ParseException e) {
            fail(e.toString());
        }
        try {
            fn= nf.buildFunctionNode("sin",jep.getFunctionTable().getFunction("sin"),cn);
            out.println(fn.toString());
        } catch (ParseException e) {
            fail(e.toString());
        }
        if(fn!=null)
        	((ASTFunNode)fn).dump("");
        Node n = new ASTStart(JccParserTreeConstants.JJTSTART);
        try {
            jep.evaluate(n);
            fail("Did not catch bad node type");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }
        out.println();
    }

    @Test
    public void testPrintVisitorMessages() {
        out.println("FastEvaluator messages");
        Jep jep = new Jep();
        Node n=null;
        n = new ASTOpNode(JccParserTreeConstants.JJTOPNODE);

        try {
            n.jjtAccept(jep.getPrintVisitor(), null);
        } catch (JepException e) {
            out.println(e.getLocalizedMessage());
        }
        out.println();
    }

    @Test
    public void testRealEvaluatorMessages() {
        out.println("RealEvaluator messages");
        Jep jep = new Jep(new RealEvaluator());

        Node n = new ASTStart(JccParserTreeConstants.JJTSTART);
        try {
            jep.evaluate(n);
            fail("Did not catch bad node type");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("complex(1.0,2.0)");
            jep.evaluate(n);
            fail("Did not catch non real number");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        } catch (ParseException e) {
            fail(e.toString());
        }

        jep.addFunction("nullFun", new UnaryFunction(){
            private static final long serialVersionUID = 1L;
            @Override
            public Object eval(Object arg) throws EvaluationException {
                return null;
            }});

        try {
            n = jep.parse("nullFun(1.0)");
            jep.evaluate(n);
            fail("Did not catch null result");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        } catch (ParseException e) {
            fail(e.toString());
        }

        try {
            n = jep.parse("x");
            jep.evaluate(n);
            fail("Did not catch null result");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        } catch (ParseException e) {
            fail(e.toString());
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode) n).setFunction("nullPFMC", null);
            jep.evaluate(n);
            fail("Did not catch null function");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        out.println();
    }

    @Test
    public void testComplexMessage() {
        out.println("Complex messages");
        Complex one = new Complex(1, 0);
        Complex negOne = new Complex(-1, 0);
        Complex i = new Complex(0, 1);
        Complex pi4 = Complex.polarValueOf(1,Math.PI/4);

        out.println(one.toString());
        out.println(negOne.toString());
        out.println(i.toString());
        out.println(pi4.toString());

        NumberFormat numf = NumberFormat.getNumberInstance();
        out.println(one.toString(numf));
        out.println(negOne.toString(numf));
        out.println(i.toString(numf));
        out.println(pi4.toString(numf));

        out.println(one.toString(numf,true));
        out.println(negOne.toString(numf,true));
        out.println(i.toString(numf,true));
        out.println(pi4.toString(numf,true));

        out.println(one.toString(numf,false));
        out.println(negOne.toString(numf,false));
        out.println(i.toString(numf,false));
        out.println(pi4.toString(numf,false));

        assertEquals("(1.0, 0.0)",one.toString());
        assertEquals("(-1.0, 0.0)",negOne.toString());
        assertEquals("(0.0, 1.0)",i.toString());
        assertEquals("("+pi4.re()+", "+pi4.im()+")",pi4.toString());

        out.println();
    }

    @Test
    public void testDoubleNumberFactoryMessages() {
        out.println("DoubleNumberFactory messages");
        Jep jep = new Jep();
        NumberFactory nf = jep.getNumberFactory();
        try {
            nf.createNumber(new Complex(1,2));
            fail("Did not catch fialed conversion");
        } catch (ParseException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }
        out.println();
    }
    
    @Test
    public void testFastEvaluatorMessages() {
        out.println("FastEvaluator messages");
        Jep jep = new Jep();
        NodeFactory nf = jep.getNodeFactory();
        FastEvaluator fe = (FastEvaluator) jep.getEvaluator();
        fe.setTrapNullValues(true);
        fe.setTrapNaN(true);
        fe.setTrapInfinity(true);

        jep.addFunction("funnull", new UnaryFunction() {
            private static final long serialVersionUID = 1L;
            @Override
            public Object eval(Object arg) throws EvaluationException {
                return null;
            }});
        jep.addFunction("breakstack",new PostfixMathCommand(1) {
            private static final long serialVersionUID = 1L;
            @Override
            public void run(Stack<Object> aStack) throws EvaluationException {
                Double d = (Double) aStack.peek();
                if(d>0) 
                    aStack.pop();
                else
                    aStack.push(d);

            }});


        try {
            jep.addConstant("mynull", null);
            jep.addConstant("myinf",Double.POSITIVE_INFINITY);
            jep.addConstant("myneginf",Double.NEGATIVE_INFINITY);
            jep.addConstant("myNaN",Double.NaN);
        } catch (JepException e) {
            fail(e.toString());
        }

        Node n;

        try {
            n = nf.buildConstantNode((Object) null);
            jep.evaluate(n);
            fail("Did not catch null constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //						"Could not evaluate variable mynull no value set. See com.singularsys.jep.standard.FastEvaluator.setTrapNullValues(boolean).",
            //						s);
        }

        try {
            n = nf.buildConstantNode(Double.NaN);
            jep.evaluate(n);
            fail("Did not catch NaN constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //						"Could not evaluate variable mynull no value set. See com.singularsys.jep.standard.FastEvaluator.setTrapNullValues(boolean).",
            //						s);
        }

        try {
            n = nf.buildConstantNode(Double.POSITIVE_INFINITY);
            jep.evaluate(n);
            fail("Did not catch infinite constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //						"Could not evaluate variable mynull no value set. See com.singularsys.jep.standard.FastEvaluator.setTrapNullValues(boolean).",
            //						s);
        }

        //		try {
        //    	n = jep.parse("breakstack(1.0)");
        //    	jep.evaluate();
        //    	fail("Did not catch corrupted stack");
        //	} catch (JepException e) {
        //		String s = e.getLocalizedMessage();
        //		out.println(s);
        //	}

        try {
            n = jep.parse("breakstack(-1.0)");
            jep.evaluate();
            fail("Did not catch corrupted stack");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("mynull");
            jep.evaluate();
            fail("Did not catch null variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //						"Could not evaluate variable mynull no value set. See com.singularsys.jep.standard.FastEvaluator.setTrapNullValues(boolean).",
            //						s);
        }

        try {
            n = jep.parse("myNaN");
            jep.evaluate();
            fail("Did not catch NaN variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"NaN value detected for variable myNaN. See com.singularsys.jep.standard.FastEvaluator.setTrapNaN(boolean).",
            //					s
            //					);
        }

        try {
            n = jep.parse("myinf");
            jep.evaluate();
            fail("Did not catch infinite variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"Infinite value, Infinity, detected for variable myinf. See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).",
            //					s
            //					);
        }

        try {
            n = jep.parse("myneginf");
            jep.evaluate();
            fail("Did not catch infinite variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"Infinite value, -Infinity, detected for variable myneginf. See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).",
            //					s
            //					);
        }

        try {
            n = jep.parse("0/0");
            jep.evaluate();
            fail("Did not catch NaN function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"Infinite value, -Infinity, detected for variable myneginf. See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).",
            //					s
            //					);
        }

        try {
            n = jep.parse("1/0");
            jep.evaluate();
            fail("Did not catch infinite function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"Infinite value, -Infinity, detected for variable myneginf. See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).",
            //					s
            //					);
        }

        try {
            n = jep.parse("funnull(1)");
            jep.evaluate();
            fail("Did not catch null function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
            //			assertEquals(
            //					"Infinite value, -Infinity, detected for variable myneginf. See com.singularsys.jep.standard.FastEvaluator.setTrapInfinity(boolean).",
            //					s
            //					);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("undefinedFun",null);
            jep.evaluate(n);
            fail("Did not catch undefined function");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("cos", jep.getFunctionTable().getFunction("cos"));
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("sin", jep.getFunctionTable().getFunction("sin"));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtAddChild(nf.buildConstantNode(2.0), 1);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("atan2", jep.getFunctionTable().getFunction("atan2"));
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("atan2", jep.getFunctionTable().getFunction("atan2"));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }


        try {
            n = nf.buildUnfinishedOperatorNode(jep.getOperatorTable().getAdd());
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("const", new ConstantFunction(Math.PI));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("round", new RoundSF());
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("test0", new PostfixMathCommand(0){
                private static final long serialVersionUID = 1L;
                @Override
                public void run(Stack<Object> aStack) throws EvaluationException {/*empty*/
                }});
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        out.println();
    }

    @Test
    public void testStandardEvaluatorMessages() {
        out.println("StandardEvaluator messages");

        Jep jep = new Jep(new StandardEvaluator());
        jep.addFunction("funnull", new UnaryFunction() {
            private static final long serialVersionUID = 1L;
            @Override
            public Object eval(Object arg) throws EvaluationException {
                return null;
            }});
        jep.addFunction("breakstack",new PostfixMathCommand(1) {
            private static final long serialVersionUID = 1L;
            @Override
            public void run(Stack<Object> aStack) throws EvaluationException {
                Double d = (Double) aStack.peek();
                if(d>0) 
                    aStack.pop();
                else
                    aStack.push(d);

            }});
        NodeFactory nf = jep.getNodeFactory();

        StandardEvaluator fe = (StandardEvaluator) jep.getEvaluator();
        fe.setTrapNullValues(true);
        fe.setTrapNaN(true);
        fe.setTrapInfinity(true);
        try {
            jep.addConstant("mynull", null);
            jep.addConstant("myinf",Double.POSITIVE_INFINITY);
            jep.addConstant("myneginf",Double.NEGATIVE_INFINITY);
            jep.addConstant("myNaN",Double.NaN);
        } catch (JepException e) {
            fail(e.toString());
        }

        Node n;

        try {
            n = nf.buildConstantNode((Object) null);
            jep.evaluate(n);
            fail("Did not catch null constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = nf.buildConstantNode(Double.NaN);
            jep.evaluate(n);
            fail("Did not catch NaN constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = nf.buildConstantNode(Double.POSITIVE_INFINITY);
            jep.evaluate(n);
            fail("Did not catch infinite constant");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }


        //	try {
        //    	n = jep.parse("breakstack(1.0)");
        //    	jep.evaluate();
        //    	fail("Did not catch corrupted stack");
        //	} catch (JepException e) {
        //		String s = e.getLocalizedMessage();
        //		out.println(s);
        //	}

        try {
            n = jep.parse("breakstack(-1.0)");
            jep.evaluate();
            fail("Did not catch corrupted stack");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("mynull");
            jep.evaluate();
            fail("Did not catch null variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("myNaN");
            jep.evaluate();
            fail("Did not catch NaN variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("myinf");
            jep.evaluate();
            fail("Did not catch infinite variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("myneginf");
            jep.evaluate();
            fail("Did not catch infinite variable");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("0/0");
            jep.evaluate();
            fail("Did not catch NaN function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("1/0");
            jep.evaluate();
            fail("Did not catch infinite function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = jep.parse("funnull(1)");
            jep.evaluate();
            fail("Did not catch null function result");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("undefinedFun",null);
            jep.evaluate(n);
            fail("Did not catch undefined function");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("cos", jep.getFunctionTable().getFunction("cos"));
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("sin", jep.getFunctionTable().getFunction("sin"));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtAddChild(nf.buildConstantNode(2.0), 1);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("atan2", jep.getFunctionTable().getFunction("atan2"));
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (EvaluationException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("atan2", jep.getFunctionTable().getFunction("atan2"));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }


        try {
            n = nf.buildUnfinishedOperatorNode(jep.getOperatorTable().getAdd());
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("const", new ConstantFunction(Math.PI));
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("round", new RoundSF());
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        try {
            n = new ASTFunNode(JccParserTreeConstants.JJTFUNNODE);
            ((ASTFunNode)n).setFunction("test0", new PostfixMathCommand(0){
                private static final long serialVersionUID = 1L;
                @Override
                public void run(Stack<Object> aStack) throws EvaluationException { /* empty */
                }});
            n.jjtOpen();
            n.jjtAddChild(nf.buildConstantNode(1.0), 0);
            n.jjtClose();
            jep.evaluate(n);
            fail("Did not catch wrong number of parameters");
        } catch (JepException e) {
            String s = e.getLocalizedMessage();
            out.println(s);
        }

        out.println();
    }

    @Test
    public void testVariableMessages() {
        out.println("Variable messages");

        Jep jep = new Jep();
        jep.addVariable("x");
        try {
            jep.addVariable("y", 1.0);
        } catch (JepException e) {
            fail(e.toString());
        }
        out.println(jep.getVariableTable().toString());

        try {
            jep.addVariable("pi", 2.0);
            fail("Didn't catch setting of constant variable");
        } catch (JepException e) {
            out.println(e.getLocalizedMessage());
        }
        out.println();
    }
    
    @Test
    public void testParserMessages() {
        out.println("Parser messages");

        Jep jep = new Jep();

        try {
            jep.parse("\\ugly");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        } catch (Error e) {
            fail(e.toString());
        }

        try {
            jep.parse("\"hel");
        fail("Didn't catch parser exception");
    } catch (ParseException e) {
        out.println(e.getLocalizedMessage());
    }

        try {
        	jep.parse("\"\\g\"");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
        	jep.parse("\\g");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
            jep.parse("\\ug");
        fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        } catch (Error e) {
        fail(e.toString());
    }


        try {
        	jep.parse("5+");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
        	jep.parse("/* comment ");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        try {
        	jep.parse("");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }

        jep.setAllowAssignment(false);
        try {
        	jep.parse("x=3");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        jep.setAllowUndeclared(false);
        try {
        	jep.parse("7*foo");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
        	jep.parse("sin(2,3)");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
        	jep.parse("avg()");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        try {
        	jep.parse("bar(1)");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        jep.setImplicitMul(false);
        try {
        	jep.parse("x y");
            fail("Didn't catch parser exception");
        } catch (ParseException e) {
            out.println(e.getLocalizedMessage());
        }
        out.println();
    }


	@Test
	public void testWalkerMessages() throws JepException, IOException, ClassNotFoundException {
	    out.println("Walker messages");
	    Jep jep = new Jep();
	    
	    try {
	    	Node start = new ASTStart(0);
	    	(new DoNothingVisitor()).visit(start);
	    } catch(JepException e) {
            out.println(e);
	    }
	    
	    Node n = jep.parse("1+sin(x)");
	    SerializableExpression se = new SerializableExpression(n);
	    out.println(se.toString());
	    out.println();
	    
	    Node n2 = jep.parse("yyyy+zzzz");
	    SerializableExpression se2 = new SerializableExpression(n2);
	    
	    ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
	    ObjectOutputStream oos = new ObjectOutputStream(baos);
	    oos.writeObject(se2);
	    oos.close();
	 // extract the bytes
	    byte bytes[] = baos.toByteArray();
	    // make a fake stream with just two variables and no operator
	    byte fakebytes[] = new byte[89];
	    System.arraycopy(bytes, 0, fakebytes, 0, 87);
	    System.arraycopy(bytes, 95, fakebytes, 87, 2);
	    fakebytes[72]=15;
	    ByteArrayInputStream bais = new ByteArrayInputStream(fakebytes); 
	    ObjectInputStream ois = new ObjectInputStream(bais);
	    // Deserialize the SerializableExpression
	    SerializableExpression se3 = (SerializableExpression) ois.readObject();
	    try {
	    	se3.toNode(jep);
	    } catch(JepException e) {
            out.println(e.getLocalizedMessage());
	    }
	    ois.close();
	    
	    Node n5 = jep.parse("2 cos(a+b) sin(a-b)");

	    TreeAnalyzer ta = new TreeAnalyzer(n5);
	    out.println(ta.summary());
	    out.println(ta.toString());
	    
	    SubstitutionVisitor sv = new SubstitutionVisitor(jep);
	    try {
	    sv.substitute(n, n5);
	    } catch(JepException e) {
            out.println(e.getLocalizedMessage());
	    }
	    
        out.println();
	}

}
