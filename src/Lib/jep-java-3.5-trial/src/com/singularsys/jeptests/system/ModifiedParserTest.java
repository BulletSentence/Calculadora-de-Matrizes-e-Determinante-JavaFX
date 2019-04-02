/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.assertEquals;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Vector;

import org.junit.Test;

import com.singularsys.jep.EmptyOperatorTable;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.PrintVisitor.PrintRulesI;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.GrammarParser;
import com.singularsys.jep.configurableparser.GrammarParserFactory;
import com.singularsys.jep.configurableparser.Lookahead2Iterator;
import com.singularsys.jep.configurableparser.ShuntingYard;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.matchers.GrammarException;
import com.singularsys.jep.configurableparser.matchers.GrammarMatcher;
import com.singularsys.jep.configurableparser.matchers.IdentifierTokenMatcher;
import com.singularsys.jep.configurableparser.matchers.MultiLineMatcher;
import com.singularsys.jep.configurableparser.matchers.TokenBuilder;
import com.singularsys.jep.configurableparser.matchers.TokenMatcher;
import com.singularsys.jep.configurableparser.tokens.FunctionToken;
import com.singularsys.jep.configurableparser.tokens.NumberToken;
import com.singularsys.jep.configurableparser.tokens.OperatorToken;
import com.singularsys.jep.configurableparser.tokens.StringToken;
import com.singularsys.jep.configurableparser.tokens.SymbolToken;
import com.singularsys.jep.configurableparser.tokens.Token;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.Cosine;
import com.singularsys.jep.functions.Divide;
import com.singularsys.jep.functions.SquareRoot;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.misc.OperatorAsFunctionGrammarMatcher;
import com.singularsys.jep.parser.Node;

/**
 * Tests for various custom grammars.
 */
public class ModifiedParserTest {

    
    public void testOperatorsAsFunctions() throws ParseException, Exception {
    	ConfigurableParser cp = new ConfigurableParser(); 
        cp.addHashComments(); 
        cp.addSlashComments(); 
        cp.addDoubleQuoteStrings(); 
        cp.addSingleQuoteStrings(); 
        cp.addWhiteSpace(); 
        cp.addExponentNumbers(); 
        cp.addOperatorTokenMatcher(); 
        cp.addSymbols("(",")","[","]",","); 
        cp.setImplicitMultiplicationSymbols("(", "["); 
        // Sets it up for identifiers with dots in them. 
        cp.addTokenMatcher(IdentifierTokenMatcher.dottedIdentifierMatcher()); 
        cp.addSemiColonTerminator(); 
        cp.addWhiteSpaceCommentFilter(); 
        cp.addBracketMatcher("(",")"); 
        cp.addFunctionMatcher("(",")",","); 
        cp.addListMatcher("[","]",","); 
        cp.addArrayAccessMatcher("[","]"); 

        // Construct the Jep instance and set the parser. 
        Jep jep = new Jep(cp); 
        
        // Access the operator table to make some modifications to the default settings. 
        OperatorTable2 ot = (OperatorTable2)jep.getOperatorTable(); 
        // Add alternatives of the logical or operator: 
        // - Get the default or operator (it uses the symbol "||"). 
        Operator defaultOr = ot.getOr(); 
        // - Based on the default operator, create new ones but use the symbols "|" and "or". 
        Operator newOr1 = new Operator("|", defaultOr.getPFMC(), defaultOr.getFlags()); 
        ot.addOperator(new EmptyOperatorTable.OperatorKey() {}, newOr1, defaultOr); 

        Operator newOr2 = new Operator("or", defaultOr.getPFMC(), defaultOr.getFlags()); 
        ot.addOperator(new EmptyOperatorTable.OperatorKey() {}, newOr2, defaultOr); 

        cp.addGrammarMatcher(new OperatorAsFunctionGrammarMatcher(
        		cp.getSymbolToken("("),
        		cp.getSymbolToken(")"),
        		cp.getSymbolToken(","),Arrays.asList(newOr2,ot.getAdd(),ot.getMultiply())));

        // Add logical functions as alternatives to the default operators. 
        //jep.getFunctionTable().addFunction("or", new Or()); 
        // Notify other components of change in operator and function table. 
        jep.reinitializeComponents(); 
        
        // Test it 
        jep.addVariable("x", 2); 
        List<String> formulas = new ArrayList<>(); 
        //formulas.add("sin(x)"); 
        formulas.add("x > 1 or x < -1"); 
        formulas.add("or(x > 1, x < -1)"); 
        formulas.add("x > 1 || x < -1"); 
        formulas.add("x > 1 | x < -1"); 
        formulas.add("+(1,2,3,4)"); 
        formulas.add("*(1,2,3,4)"); 
        for (String formula : formulas) { 
                Node n = jep.parse(formula); 
                Object result = jep.evaluate(n); 
                System.out.println(String.format("\"%s\" = \"%s\" = %s", formula, jep.toString(n), result)); 
        } 
    }


    /** Tests a Latex like grammar
     * 
     * @throws Exception
     */
    @Test
    public void testLatex() throws Exception {
    	
    	GrammarMatcher latexFunctionGrammerMatcher = new GrammarMatcher(){
			private static final long serialVersionUID = 1L;

			Token sqOpen = new SymbolToken("[");
    		Token sqClose = new SymbolToken("]");
    		Token cOpen = new SymbolToken("{");
    		Token cClose = new SymbolToken("}");
    		NodeFactory nf;
    		PostfixMathCommandI sqrtn;
    		
			@Override
			public Node match(Lookahead2Iterator<Token> it, GrammarParser parser) throws ParseException {
				Token t2 = it.peekNext();
				if(t2 == null) return null;
				if(!t2.isFunction()) return null;
		        String name = t2.getSource();
		        PostfixMathCommandI pfmc = ((FunctionToken) t2).getPfmc();
				it.consume();

				Token t = it.peekNext();
				if(t==null)
					throw new GrammarException("Expected argument to function "+name,it.peekNext());
					
				Node optionArg = null;
				Node arg1 = null;
				Node arg2 = null;
				
				if(sqOpen.equals(t) && name.equals("\\sqrt")) {
					it.consume();
					optionArg = parser.parseSubExpression();
					if(!sqClose.equals(it.peekNext()))
						throw new GrammarException("Unclosed square bracket in sqrt.",it.peekNext());
					it.consume();
					t = it.peekNext();
				}
				if(cOpen.equals(t)) {
					it.consume();
					arg1 = parser.parseSubExpression();
					if(!cClose.equals(it.peekNext()))
						throw new GrammarException("Unclosed curly bracket in function "+name,it.peekNext());
					it.consume();
					t = it.peekNext();
				} else if(t.isNumber()) {
					arg1 = nf.buildConstantNode(((NumberToken) t).getValue());
					it.consume();
				} else if(t.isIdentifier())  {
					arg1 = nf.buildVariableNode(t.getSource());
					it.consume();
				} else {
					throw new GrammarException("Bad token following function "+name,it.peekNext());
				}
				if(name.equals("\\sqrt")) {
					if(optionArg!=null) {
						Node res = nf.buildFunctionNode("\\sqrtn", sqrtn, arg1, 
								optionArg);
						return res;
					}
				}
				if(pfmc.getNumberOfParameters()==1) {
					Node res = nf.buildFunctionNode(name, pfmc, arg1);
					return res;
				}
				if(cOpen.equals(t)) {
					it.consume();
					arg2 = parser.parseSubExpression();
					if(!cClose.equals(it.peekNext()))
						throw new GrammarException("Unclosed curly bracket in function "+name,it.peekNext());
					it.consume();
					t = it.peekNext();
				} else if(t.isNumber()) {
					arg2 = nf.buildConstantNode(((NumberToken) t).getValue());
					it.consume();
				} else if(t.isIdentifier())  {
					arg2 = nf.buildVariableNode(t.getSource());
					it.consume();
				} else {
					throw new GrammarException("Bad token following function "+name,it.peekNext());
				}
				if(pfmc.getNumberOfParameters()==2) {
					Node res = nf.buildFunctionNode(name, pfmc, arg1,arg2);
					return res;
				}
				
				
				return null;
			}

			@Override
			public void init(Jep j) {
				nf = j.getNodeFactory();
				sqrtn = j.getFunctionTable().getFunction("\\sqrtn");
				
			}};
    	
		ConfigurableParser cp = new ConfigurableParser();
			cp.addHashComments();
	        cp.addSlashComments();
	        cp.addSingleQuoteStrings();
	        cp.addDoubleQuoteStrings();
	        cp.addWhiteSpace();
	        cp.addExponentNumbers();
	        cp.addOperatorTokenMatcher();
	        cp.addSymbols("{","}","[","]"); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$ //$NON-NLS-4$
//	        cp.setImplicitMultiplicationSymbols("(","["); //$NON-NLS-1$ //$NON-NLS-2$
//	        cp.addIdentifiers();
	        cp.addTokenMatcher(
	        		new IdentifierTokenMatcher("\\\\?[a-zA-Z]+"));
	        cp.addSemiColonTerminator();
	        cp.addWhiteSpaceCommentFilter();
	        //cp.addBracketMatcher("(",")"); //$NON-NLS-1$ //$NON-NLS-2$
	        //cp.addFunctionMatcher("(",")",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        //cp.addListMatcher("[","]",","); //$NON-NLS-1$ //$NON-NLS-2$ //$NON-NLS-3$
	        //cp.addArrayAccessMatcher("[","]"); //$NON-NLS-1$ //$NON-NLS-2$
	    
	        cp.addGrammarMatcher(latexFunctionGrammerMatcher);
	        BinaryFunction sqrtn = new BinaryFunction(){
				private static final long serialVersionUID = 1L;

				@Override
				public Object eval(Object l, Object r) throws EvaluationException {
					double dl = this.asDouble(0, l);
					double dr = this.asDouble(1, r);
					return Math.pow(dl, 1/dr);
				}};

		Jep jep = new Jep(cp,new FunctionTable(),new VariableTable());
		jep.addFunction("\\cos", new Cosine());
		jep.addFunction("\\sqrt", new SquareRoot());
		jep.addFunction("\\sqrtn", sqrtn);
		jep.addFunction("\\frac",	 new Divide());
		jep.addConstant("\\pi", Math.PI);
		jep.reinitializeComponents();

		{//          123456789012345678901234567890
	    	String s = "\\frac{-b+\\sqrt{b^2-4 a c}}{2a}";
	    	Node n = jep.parse(s);
	    	jep.println(n);
		}
		{//          123456789012345678901234567890
	    	String s = "\\sqrt[3]{27}";
	    	Node n = jep.parse(s);
	    	jep.println(n);
	    	Object res = jep.evaluate(n);
	    	assertEquals(s,3.0,res);
		}
		{//          123456789012345678901234567890
	    	String s = "\\cos \\pi";
	    	Node n = jep.parse(s);
	    	jep.println(n);
	    	Object res = jep.evaluate(n);
	    	assertEquals(s,-1.0,res);
		}
    	
    }   

    /**
     * Test for string constants which can be broken over multiple lines.
     * @see MultiLineMatcher
     * @throws Exception
     */
    @Test
    public void testMultLineString() throws Exception
    {
    	TokenMatcher start = new TokenMatcher() {
			private static final long serialVersionUID = 1L;

			@Override
			public Token match(String s) throws ParseException {
				if(s.startsWith("'")
						&& s.indexOf('\'', 1) == -1) 
					return new Token(s){

						/**
						 * 
						 */
						private static final long serialVersionUID = 1L;};
				return null;
			}

			@Override
			public void init(Jep jep) {
			}
    	};

    	TokenMatcher end = new TokenMatcher() {
			private static final long serialVersionUID = 1L;

			@Override
			public Token match(String s) throws ParseException {
				int pos = s.indexOf('\'');
				if(pos >= 0)
					return new Token(s.substring(0, pos+1)){
						private static final long serialVersionUID = 1L;};
						return null;
			}

			@Override
			public void init(Jep jep) {
			}
    	};
    	
    	TokenBuilder tb = new TokenBuilder() {
			private static final long serialVersionUID = 1L;

			@Override
			public Token match(String s) throws ParseException {
				return null;
			}

			@Override
			public void init(Jep jep) {
				
			}

			@Override
			public Token buildToken(String s) {
				return new StringToken(s,s.substring(1,s.length()-1),'\'',false);
			}};

		TokenMatcher m =  new MultiLineMatcher(start,end,tb);
        ConfigurableParser cp = new StandardConfigurableParser();
        cp.addTokenMatcher(m);
        Jep jep = new Jep(cp);
		
        String s1 = "'line\nbreak'";
        Node n1 = jep.parse(s1);
        jep.println(n1);

        String s2 = "'line\r\nbreak'";
        Node n2 = jep.parse(s2);
        jep.println(n2);
    
        String s3 = "'line\rbreak'";
        Node n3 = jep.parse(s3);
        jep.println(n3);

        String s4 = "if(true, 'line\nbreak', 'else')";
        Node n4 = jep.parse(s4);
        jep.println(n4);

    }
    
    
    /**
     * A subclass of the ShuntingYard which can parse various forms of list element access.
     * So <code>seq(2,5)[3]</code> <code>[5,6,7][2]</code> and <code>([1,2]+[3,4])[2]</code> are all possible.
     * 
     * If works by overriding the {@link com.singularsys.jep.configurableparser.ShuntingYard.prefixSuffix()} method
     * to treat array access as a general suffix operator which can be applied to all
     * prefix expressions. 
     */

	static class SuffixArrayAccessShuntingYard extends ShuntingYard {
		private final Token open = new SymbolToken("[");
		private final Token close = new SymbolToken("]");

		public SuffixArrayAccessShuntingYard(Jep jep, List<GrammarMatcher> gm) {
			super(jep, gm);
		}

		@Override
		protected void prefixSuffix() throws ParseException {
	        //		if(DUMP) dumpState("PS");
	        prefix();
	        Token t;
	        while(true) {
	            t = it.peekNext();
	            if(open.equals(t)) {
	            	
	    			List<Node> arguments = new ArrayList<>();
	    			Node lhs = nodes.pop();
	    			arguments.add(lhs);
	    			while (open.equals(it.peekNext())) {

	    				// Process opening square bracket.
	    				it.consume();

	    				// Process index and check whether it is a positive integer
	    				// number.
	    				Token nextToken = it.peekNext();
	    				Node indexNode = this.parseSubExpression();

	    				// Ensure the closing square bracket comes next.
	    				nextToken = it.peekNext();
	    				if (!close.equals(it.peekNext())) {
	    					throw new GrammarException(String.format("Index array expects '%s'.", close.getSource()),
	    							nextToken);
	    				}
	    				arguments.add(indexNode);
	    				// Process closing square bracket.
	    				it.consume();

	    			}

	    			// Create and return the node representing the function as well
	    			// as the indexed access.
	    			Node inner = jep.getNodeFactory().buildOperatorNode(jep.getOperatorTable().getEle(),
	    					arguments.toArray(new Node[arguments.size()]));

	    			nodes.push(inner);
	            	
	            } else if(t==null || !t.isSuffix()) {
	            	break;
	            } else {
	            	pushOp(((OperatorToken)t).getSuffixOp(),t);
	            	it.consume();
	            }
	        }
		}
	}

	/**
	 * Factory to create SuffixArrayAccessShuntingYard
	 * @author richard
	 *
	 */
	public static class SuffixArrayAccessShuntingYardGrammarParserFactory implements GrammarParserFactory {
		private static final long serialVersionUID = 340L;
		/**
		 * Create a new ShuntingYard instance.
		 */
		@Override
		public GrammarParser newInstance(ConfigurableParser cp) {
			return new SuffixArrayAccessShuntingYard(cp.getJep(),cp.getGrammarMatchers());
		}
		@Override
		public void init(Jep jep) {
		}
		@Override
		public JepComponent getLightWeightInstance() {
			return null;
		}
	}

	/**
	 * Simple test class of a function which returns an array.
	 */
	class SeqFun extends UnaryFunction {
		private static final long serialVersionUID = 1L;

		@Override
		public Object eval(Object arg) throws EvaluationException {
			Vector<Object> res = new Vector<>();
			int n = this.asInt(0, arg);
			for (int i = 1; i <= n; ++i) {
				res.add(Double.valueOf(i));
			}
			return res;
		}
	}

	/** 
	 * A rule to correctly print suffix array access expressions
	 */
    public static final class SuffixElePrintRule implements PrintRulesI {
        private static final long serialVersionUID = 300L;
		private Operator ListOp;
		
		

        public SuffixElePrintRule(Operator listOp) {
			super();
			ListOp = listOp;
		}

		@Override
		public void append(Node node, PrintVisitor pv)
        throws JepException {
        	
        	Operator lhsOp = node.jjtGetChild(0).getOperator();
			if(lhsOp!= null && lhsOp != ListOp) {
        		pv.append("(");
        		node.jjtGetChild(0).jjtAccept(pv, null);
        		pv.append(")");            
        	} else
        		node.jjtGetChild(0).jjtAccept(pv, null);

            for(int i=1;i<node.jjtGetNumChildren();++i) {
                Node child = node.jjtGetChild(i);
                if(child.getOperator() == ListOp)
                    node.jjtGetChild(i).jjtAccept(pv, null);
                else
                {
                    pv.append("[");  //$NON-NLS-1$
                    node.jjtGetChild(i).jjtAccept(pv, null);
                    pv.append("]"); //$NON-NLS-1$
                }
            }
        }
    }

    
	@Test
	public void testSuffixArrayAccess() throws JepException {
		ConfigurableParser cp = new ConfigurableParser();
		cp.addHashComments();
		cp.addSlashComments();
		cp.addDoubleQuoteStrings();
		cp.addSingleQuoteStrings();
		cp.addWhiteSpace();
		cp.addExponentNumbers();
		cp.addOperatorTokenMatcher();
		cp.addSymbols("(", ")", "[", "]", ",");
		cp.setImplicitMultiplicationSymbols("(", "[");
		cp.addTokenMatcher(IdentifierTokenMatcher.basicIdentifierMatcher());
		cp.addSemiColonTerminator();
		cp.addWhiteSpaceCommentFilter();
		cp.addBracketMatcher("(", ")");
		cp.addFunctionMatcher("(", ")", ",");
		cp.addListMatcher("[", "]", ",");
		
		cp.setGrammarParserFactory(new SuffixArrayAccessShuntingYardGrammarParserFactory());
//		cp.addArrayAccessMatcher("[", "]");

		Jep jep = new Jep(cp);
		jep.addFunction("seq", new SeqFun());
		jep.getPrintVisitor().addSpecialRule(jep.getOperatorTable().getEle(),new SuffixElePrintRule(jep.getOperatorTable().getList()) );
		
		{
			Node n2 = jep.parse("sin(pi)");
			Object o2 = jep.evaluate(n2);
			assertEquals(0.0, (Double) o2, 1e-9);
		}
		{
			Node n2 = jep.parse("3 (4+5)");
			Object o2 = jep.evaluate(n2);
			assertEquals(27.0, (Double) o2, 1e-9);
		}
		{
			Node n2 = jep.parse("x=[7,8,9]");
			jep.println(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals("[7.0, 8.0, 9.0]", o2.toString());
		}
		{
			Node n2 = jep.parse("x=[7,8,9]");
			jep.println(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals("[7.0, 8.0, 9.0]", o2.toString());
		}
		{
			Node n2 = jep.parse("x[3]");
			jep.println(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(9.0, o2);
		}
		{
			Node n2 = jep.parse("x[3]+x[2]");
			jep.println(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(17.0, o2);
		}

		{
			Node n2 = jep.parse("seq(3)[2]");
			jep.println(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(2.0, o2);
		}
		{
			Node n2 = jep.parse("[4, 5, 6][2]");
			assertEquals("[4.0,5.0,6.0][2.0]",jep.toString(n2));
			jep.println(n2);
//			(new PrefixTreeDumper()).dump(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(5.0, o2);
		}
		{
			Node n2 = jep.parse("[3, [4, 5], 6][3]");
			jep.println(n2);
//			(new PrefixTreeDumper()).dump(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(6.0, o2);
		}
		{
			Node n2 = jep.parse("[[1, 2], [3, 4], [5, 6]][3][1]");
			jep.println(n2);
//			(new PrefixTreeDumper()).dump(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(5.0, o2);
		}
		{
			Node n2 = jep.parse("(x+[1,2,3])[2]");
			jep.println(n2);
//			(new PrefixTreeDumper()).dump(n2);
			Object o2 = jep.evaluate(n2);
			assertEquals(10.0, o2);
			assertEquals("(x+[1.0,2.0,3.0])[2.0]",jep.toString(n2));
		}
	}
    
}
