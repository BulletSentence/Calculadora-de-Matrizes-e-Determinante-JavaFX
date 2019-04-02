/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 19 Aug 2006 - Richard Morris
 */
package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertNotNull;
import static org.junit.Assert.assertNull;
import static org.junit.Assert.assertTrue;
import static org.junit.Assert.fail;

import java.io.StringReader;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTable2;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.VariableFactory;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.configurableparser.Lookahead2Iterator;
import com.singularsys.jep.configurableparser.LookaheadNIterator;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.Tokenizer;
import com.singularsys.jep.configurableparser.tokens.CommentToken;
import com.singularsys.jep.configurableparser.tokens.IdentifierToken;
import com.singularsys.jep.configurableparser.tokens.NumberToken;
import com.singularsys.jep.configurableparser.tokens.OperatorToken;
import com.singularsys.jep.configurableparser.tokens.StringToken;
import com.singularsys.jep.configurableparser.tokens.Token;
import com.singularsys.jep.configurableparser.tokens.WhiteSpaceToken;
import com.singularsys.jep.functions.Logical;
import com.singularsys.jep.misc.ExtendedOperatorSet;
import com.singularsys.jep.standard.DoubleNumberFactory;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.standard.StandardFunctionTable;
public class TokenizerTest {
    Jep jep;
    ConfigurableParser cp;

    public void checkIdentifier(Token expected,Token actual)
    {
        assertTrue("Token should be an identifier",actual instanceof IdentifierToken);
        assertEquals("Value for IndetifierToken does not match",expected.getSource(),actual.getSource());
    }
    public void checkNumber(Token expected,Token actual)
    {
        assertTrue("Token should be a number",actual instanceof NumberToken);
        assertEquals("Value for NumberToken does not match",expected.getSource(),actual.getSource());
    }
    public void checkString(Token expected,Token actual)
    {
        assertTrue("Token should be a string",actual instanceof StringToken);
        assertEquals("Value for StringToken does not match",expected.getSource(),actual.getSource());
    }
    public void checkComment(Token expected,Token actual)
    {
        assertTrue("Token should be an comment",actual instanceof CommentToken);
        assertEquals("Value for StringToken does not match",expected.getSource(),actual.getSource());
    }
    public void checkWhiteSpace(Token expected,Token actual)
    {
        assertTrue("Token should be a white space",actual instanceof WhiteSpaceToken);
        assertEquals("Value for StringToken does not match",expected.getSource(),actual.getSource());
    }
    public void checkOperator(Token expected,Token actual)
    {
        assertTrue("Token should be an operator",actual instanceof OperatorToken);
        OperatorToken expOpTok = (OperatorToken) expected;
        OperatorToken actualOpTok = (OperatorToken) actual;
        assertEquals("Binary ops don't match",expOpTok.getBinaryOp(),actualOpTok.getBinaryOp());
        assertEquals("Prefix ops don't match",expOpTok.getPrefixOp(),actualOpTok.getPrefixOp());
        assertEquals("Suffix ops don't match",expOpTok.getSuffixOp(),actualOpTok.getSuffixOp());
        assertEquals("Ternary ops don't match",expOpTok.getTernaryOp(),actualOpTok.getTernaryOp());
    }

    public void check(List<Token> tokens,List<Token> expected)
    {
        if(tokens.size() != expected.size())
            fail("Length of token sequence "+tokens.size()+" does not match expected size "+expected.size());
        for(int i=0;i<tokens.size();++i) {
            Token test = tokens.get(i);
            Token expect = expected.get(i);
            if(expect instanceof IdentifierToken)
                checkIdentifier(expect,test);
            else if(expect instanceof NumberToken)
                checkNumber(expect,test);
            else if(expect instanceof StringToken)
                checkString(expect,test);
            else if(expect instanceof OperatorToken)
                checkOperator(expect,test);
            else if(expect instanceof CommentToken)
                checkComment(expect,test);
            else if(expect instanceof WhiteSpaceToken)
                checkWhiteSpace(expect,test);
            else
                fail("Unknown token type");
        }

    }

    public void check(List<Token> tokens,Object[] expected) throws ParseException
    {
        List<Token> fullExpected = new ArrayList<>();
        for(Object obj:expected) {
            if(obj instanceof String)
            {
                List<Token> toks = cp.scan(new StringReader((String) obj));
                if(toks.size()!=1)
                    fail("Each string in expected should be an individual token");
                fullExpected.add(toks.get(0));
            }
            else if(obj instanceof Operator)
            {
                Token t = new OperatorToken(Collections.singletonList((Operator) obj),((Operator)obj).getSymbol());
                fullExpected.add(t);
            }
        }
        check(tokens,fullExpected);
    }

    public void check(String str,String[] expected) throws ParseException
    {
        check(scan(str),expected);
    }

    @Before
    public void setUp() throws Exception {
        jep = new Jep(new JepComponent[]{
                new DoubleNumberFactory(),
                new VariableFactory(),
                new NodeFactory(),
                new StandardFunctionTable(),
                new VariableTable(),
                new ExtendedOperatorSet(),
                new StandardConfigurableParser(),
                new StandardEvaluator(),
                new PrintVisitor()
        });
        cp = (ConfigurableParser) jep.getParser();
        //		tk = cp.getTk();
    }

    List<Token> scan(String s) throws ParseException {
        return cp.scan(new StringReader(s));
    }

    /** Tests individual tokens */
    @Test
    public final void testTokens() throws ParseException {
        List<Token> tokens = scan("a");
        check(tokens,Collections.singletonList((Token) new IdentifierToken("a")));
        tokens = scan("\"aaa\"");
        check(tokens,Collections.singletonList((Token) new StringToken("\"aaa\"","aaa",'"',false)));
        tokens = scan("'aaa'");
        check(tokens,Collections.singletonList((Token) new StringToken("'aaa'","aaa",'\'',false)));
        tokens = scan("1");
        check(tokens,Collections.singletonList((Token) new NumberToken("1",jep.getNumberFactory())));
        tokens = scan("1.2");
        check(tokens,Collections.singletonList((Token) new NumberToken("1.2",jep.getNumberFactory())));
        tokens = scan("1.");
        check(tokens,Collections.singletonList((Token) new NumberToken("1.",jep.getNumberFactory())));
        tokens = scan(".2");
        check(tokens,Collections.singletonList((Token) new NumberToken(".2",jep.getNumberFactory())));
        tokens = scan("//aa a");
        check(tokens,Collections.singletonList((Token) new CommentToken("//aa a")));
        tokens = scan("/*aa a*/");
        check(tokens,Collections.singletonList((Token) new CommentToken("/*aa a*/")));
        tokens = scan("#aa a");
        check(tokens,Collections.singletonList((Token) new CommentToken("#aa a")));
        tokens = scan("*");
        check(tokens,Collections.singletonList((Token) new OperatorToken(
                Collections.singletonList(jep.getOperatorTable().getMultiply()),"*")));
    }

    @Test
    public  final void testScan() throws ParseException {
        check("a!=b",new String[]{"a","!=","b"});
        check("a&&b",new String[]{"a","&&","b"});
        check("a<=b",new String[]{"a","<=","b"});
        check("a==b",new String[]{"a","==","b"});
        check("a>=b",new String[]{"a",">=","b"});
        check("a^^b",new String[]{"a","^^","b"});
        check("a||b",new String[]{"a","||","b"});
        check("a!b",new String[]{"a","!","b"});
        check("a%b",new String[]{"a","%","b"});
        check("a*b",new String[]{"a","*","b"});
        check("a+b",new String[]{"a","+","b"});
        check("a-b",new String[]{"a","-","b"});
        check("a.b",new String[]{"a",".","b"});
        check("a/b",new String[]{"a","/","b"});
        check("a<b",new String[]{"a","<","b"});
        check("a=b",new String[]{"a","=","b"});
        check("a>b",new String[]{"a",">","b"});
        check("a^b",new String[]{"a","^","b"});
        check("-a",new String[]{"-","a"});
        check("+a",new String[]{"+","a"});
        check("!a",new String[]{"!","a"});
        check("a%",new String[]{"a","%"});
        check(" a",new String[]{" ","a"});
        check("a b",new String[]{"a"," ","b"});
        check("a ",new String[]{"a"," "});
        check("a-1",new String[]{"a","-","1"});
        check("-1",new String[]{"-","1"});
        check("a/*zzz*/b",new String[]{"a","/*zzz*/","b"});
    }

    /*
	@Test
	public final void testRemoveWhitespace() throws ParseException {
		Operator binaryminus = jep.getOperatorTable().getSubtract(); 
		Operator unaryminus = jep.getOperatorTable().getUMinus(); 
		Operator binarymul = jep.getOperatorTable().getMultiply(); 
	}

	@Test
	public final void testScanOps() throws ParseException {
		Operator binaryminus = jep.getOperatorTable().getSubtract(); 
		Operator unaryminus = jep.getOperatorTable().getUMinus(); 
		Operator binarymul = jep.getOperatorTable().getMultiply(); 
//		check(tk.scanOperatorFix(tk.scan("a-b")), new Object[]{"a",binaryminus,"b"});
//		check(tk.scanOperatorFix(tk.scan("-b")), new Object[]{unaryminus,"b"});
//		check(tk.scanOperatorFix(tk.scan("a*-b")), new Object[]{"a","*",unaryminus,"b"});
	}
     */
    @Test
    public final void testIt() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        Lookahead2Iterator<Integer> myIt = new Lookahead2Iterator<>(list);
        assertNotNull(myIt.peekNext());
        assertEquals((Integer) 1,myIt.peekNext());
        assertEquals((Integer) 1,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 2,myIt.peekNext());
        assertEquals((Integer) 2,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 3,myIt.peekNext());
        assertEquals((Integer) 3,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 4,myIt.peekNext());
        assertEquals((Integer) 4,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 5,myIt.peekNext());
        assertEquals((Integer) 5,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertNull(myIt.peekNext());
    }

    @Test
    public final void testItN() {
        List<Integer> list = new ArrayList<>();
        list.add(1);
        list.add(2);
        list.add(3);
        list.add(4);
        list.add(5);
        LookaheadNIterator<Integer> myIt = new LookaheadNIterator<>(list.listIterator());
        assertNotNull(myIt.peekNext());
        assertEquals((Integer) 1,myIt.peekNext());
        assertEquals((Integer) 1,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 2,myIt.peekNext());
        assertEquals((Integer) 2,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 3,myIt.peekNext());
        assertEquals((Integer) 3,myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 4,myIt.peekNext());
        assertEquals((Integer) 4,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 5,myIt.peekNext());
        assertEquals((Integer) 5,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertNull(myIt.peekNext());

        myIt = new LookaheadNIterator<>(list.listIterator());
        
        
        assertNotNull(myIt.peekNext());
        assertEquals((Integer) 1,myIt.peekNext());
        assertEquals((Integer) 1,myIt.lookahead(0));
        assertEquals((Integer) 2,myIt.lookahead(1));
        assertEquals((Integer) 3,myIt.lookahead(2));
        assertEquals((Integer) 4,myIt.lookahead(3));
        assertEquals((Integer) 5,myIt.lookahead(4));
        assertNull(myIt.lookahead(5));
        
        
        
        myIt.consume();
        assertEquals((Integer) 2,myIt.peekNext());
        assertEquals((Integer) 2,myIt.lookahead(0));
        assertEquals((Integer) 3,myIt.lookahead(1));
        assertEquals((Integer) 4,myIt.lookahead(2));
        assertEquals((Integer) 5,myIt.lookahead(3));
        assertNull(myIt.lookahead(4));

        myIt.consume();
        assertEquals((Integer) 3,myIt.peekNext());
        assertNull(myIt.lookahead(3));
        assertEquals((Integer) 5,myIt.lookahead(2));
        assertEquals((Integer) 4,myIt.lookahead(1));
        assertEquals((Integer) 3,myIt.lookahead(0));

        myIt.consume();
        assertEquals((Integer) 4,myIt.peekNext());
        assertEquals((Integer) 4,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertEquals((Integer) 5,myIt.peekNext());
        assertEquals((Integer) 5,myIt.peekNext());
        assertNotNull(myIt.peekNext());
        myIt.consume();
        assertNull(myIt.peekNext());

    
    
    }

    @Test
    public void testMultiLineComments() throws Exception {
        String comment = "/* y + \n" +
        " a b\n" +
        " c */";
        String s = "x+" + comment + "d";
        List<Token> tokens = cp.scan(new StringReader(s));
        System.out.print(Tokenizer.toString(tokens));
        assertTrue(tokens.get(0) instanceof IdentifierToken);
        assertEquals(tokens.get(0).getSource(),"x");
        assertTrue(tokens.get(1) instanceof OperatorToken);
        assertEquals(tokens.get(1).getSource(),"+");
        assertTrue(tokens.get(2) instanceof CommentToken);
        assertEquals(tokens.get(2).getSource(),comment);
        assertTrue(tokens.get(3) instanceof IdentifierToken);
        assertEquals(tokens.get(3).getSource(),"d");
    }

    @Test
    public void testMultiLineWhiteSpace() throws Exception {
        String s = "x+y+\nz";
        List<Token> tokens = cp.scan(new StringReader(s));
        System.out.print(Tokenizer.toString(tokens));
        assertTrue(tokens.get(0) instanceof IdentifierToken);
        assertEquals(tokens.get(0).getSource(),"x");
        assertTrue(tokens.get(1) instanceof OperatorToken);
        assertEquals(tokens.get(1).getSource(),"+");
        assertTrue(tokens.get(2) instanceof IdentifierToken);
        assertEquals(tokens.get(2).getSource(),"y");
        assertTrue(tokens.get(3) instanceof OperatorToken);
        assertEquals(tokens.get(3).getSource(),"+");
        assertTrue(tokens.get(4) instanceof WhiteSpaceToken);
        assertEquals(tokens.get(4).getSource(),System.getProperty("line.separator", "\n"));
        assertTrue(tokens.get(5) instanceof IdentifierToken);
        assertEquals(tokens.get(5).getSource(),"z");
    }

    @Test
    public void testSemiColon() throws Exception {
        String s = "1;2;3;\n" +
        "4;5;";
        List<Token> tokens = cp.scan(new StringReader(s));
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("1",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("2",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("3",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof WhiteSpaceToken);
        assertEquals(System.getProperty("line.separator", "\n"),tokens.get(0).getSource());
        assertTrue(tokens.get(1) instanceof NumberToken);
        assertEquals("4",tokens.get(1).getSource());
        assertTrue(tokens.size()==2);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("5",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertNull(tokens);
    }

    @Test
    public void testSemiColon2() throws Exception {
        String s = "1;2;x+\n" +
        "y;5;";
        List<Token> tokens = cp.scan(new StringReader(s));
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("1",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("2",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof IdentifierToken);
        assertEquals("x",tokens.get(0).getSource());
        assertTrue(tokens.get(1) instanceof OperatorToken);
        assertEquals("+",tokens.get(1).getSource());
        assertTrue(tokens.get(2) instanceof WhiteSpaceToken);
        assertEquals(System.getProperty("line.separator", "\n"),tokens.get(2).getSource());
        assertTrue(tokens.get(3) instanceof IdentifierToken);
        assertEquals("y",tokens.get(3).getSource());
        assertTrue(tokens.size()==4);

        tokens = cp.scan();
        assertTrue(tokens.get(0) instanceof NumberToken);
        assertEquals("5",tokens.get(0).getSource());
        assertTrue(tokens.size()==1);

        tokens = cp.scan();
        assertNull(tokens);
    }

    public void testExtended() throws Exception
    {
        cp.scan(new StringReader("5!"));
    }

    /**
     * Tests operators with alphabet characters, such as the "OR" operator
     * @throws Exception
     */
    @Test
    public void alphabeticOpsTest() throws Exception {
        // alter operator table
        OperatorTable2 ot = (OperatorTable2) jep.getOperatorTable();
        Operator orOp = new Operator("OR", new Logical(0), Operator.BINARY+Operator.LEFT+Operator.ASSOCIATIVE);
        ot.replaceOperator(ot.getAnd(), orOp );
        // informs the parser and other components about the new operator
        jep.reinitializeComponents();

        List<Token> tokens = cp.scan(new StringReader("1 OR 1"));
        printTokenList(tokens); System.out.println();
        check(tokens, new Object[]{"1", " ", orOp, " ", "1"});

        tokens = cp.scan(new StringReader("1ORGANISE"));
        printTokenList(tokens); System.out.println();
        check(tokens, new Object[]{"1", "ORGANISE"});
        
        tokens = cp.scan(new StringReader("1OR_4"));
        printTokenList(tokens);System.out.println();
        check(tokens, new Object[]{"1", "OR_4"});

        tokens = cp.scan(new StringReader("1OR-4"));
        printTokenList(tokens);System.out.println();
        check(tokens, new Object[]{"1", orOp,"-","4"});

        tokens = cp.scan(new StringReader("1OR1"));
        printTokenList(tokens);System.out.println();
        //check(tokens, new Object[]{"1", orOp, "1"});
        check(tokens, new Object[]{"1", "OR1"});

    }

    public void printTokenList(List<Token> tokens) {
        for (Token t:tokens) {
            System.out.print(t.toString() + ", ");
        }
    }

    @Test
    public void testTwoString() throws Exception
    {
        String sd = "(a==\"1\")&&(b==\"2\")";
        List<Token> tokens = scan(sd);
        assertEquals("Number of tokens for "+sd,11,tokens.size());
        String ss = "(a=='1')&&(b=='2')";
        tokens = scan("(a=='1')&&(b=='2')");
        assertEquals("Number of tokens for "+ss,11,tokens.size());

    }

    @Test
    public void testEmptyEquations() throws Exception {
        String s = "1;;2";
        List<Token> tokens = cp.scan(new StringReader(s));
        assertEquals("Num tokens for 1",1,tokens.size());
        tokens = cp.scan();
        assertEquals("Num tokens for ;;",0,tokens.size());
        tokens = cp.scan();
        assertEquals("Num tokens for 2",1,tokens.size());
        tokens = cp.scan();
        assertNull("EOF",tokens);

        s = "1;;2;";
        tokens = cp.scan(new StringReader(s));
        assertEquals("Num tokens for 1",1,tokens.size());
        tokens = cp.scan();
        assertEquals("Num tokens for ;;",0,tokens.size());
        tokens = cp.scan();
        assertEquals("Num tokens for 2",1,tokens.size());
        tokens = cp.scan();
        assertNull("EOF",tokens);

        s = ";";
        tokens = cp.scan(new StringReader(s));
        assertEquals("Num tokens for ;",0,tokens.size());
        tokens = cp.scan();
        assertNull("EOF",tokens);

        s = "";
        tokens = cp.scan(new StringReader(s));
        assertNull("EOF",tokens);
    }

    @Test
    public void testLineNumbers() throws Exception
    {
        String sd = "x=1\n+ 1\n \n+12/*foo\nbar*/+1";
        int posn[][]=new int[][]{{1,1},{1,2},{1,3},{1,4},
        		{2,1},{2,2},{2,3},{2,4},
        		{3,1},{3,2},
        		{4,1},{4,2},{4,4},
        		{5,6},{5,7}};
        
        List<Token> tokens = scan(sd);
        System.out.println(tokens);
        assertEquals("Number of tokens for "+sd,posn.length,tokens.size());
        for(int i=0;i<posn.length;++i) {
            assertEquals("Line number for '"+tokens.get(i).getSource()+"'",posn[i][0],tokens.get(i).getLineNumber());
            assertEquals("Column for '"+tokens.get(i).getSource()+"'",posn[i][1],tokens.get(i).getColumnNumber());
        	
        }

    }

    @Test
    public void testAltSymbols() throws Exception {
    	OperatorTable2 ot = (OperatorTable2) jep.getOperatorTable();
    	ot.getUMinus().addAltSymbol("~");
    	ot.getSubtract().addAltSymbol("~");
    	ot.getEQ().addAltSymbol("=");
    	ot.removeOperator(ot.getAssign());
    	jep.reinitializeComponents();
    	String s = "~1=2~3";
        List<Token> tokens = scan(s);
        System.out.println(tokens);
        assertEquals(((OperatorToken) tokens.get(0)).getPrefixOp(),ot.getUMinus());
        assertEquals(((OperatorToken) tokens.get(0)).getBinaryOp(),ot.getSubtract());
        assertEquals(((OperatorToken) tokens.get(0)).getSource(),"~");

        assertEquals(((OperatorToken) tokens.get(2)).getBinaryOp(),ot.getEQ());
        assertEquals(((OperatorToken) tokens.get(2)).getSource(),"=");

        assertEquals(((OperatorToken) tokens.get(4)).getPrefixOp(),ot.getUMinus());
        assertEquals(((OperatorToken) tokens.get(4)).getBinaryOp(),ot.getSubtract());
        assertEquals(((OperatorToken) tokens.get(4)).getSource(),"~");

    	String s2 = "-1==2-3";
        List<Token> tokens2 = scan(s2);
        System.out.println(tokens2);
        assertEquals(((OperatorToken) tokens2.get(0)).getPrefixOp(),ot.getUMinus());
        assertEquals(((OperatorToken) tokens2.get(0)).getBinaryOp(),ot.getSubtract());
        assertEquals(((OperatorToken) tokens2.get(0)).getSource(),"-");

        assertEquals(((OperatorToken) tokens2.get(2)).getBinaryOp(),ot.getEQ());
        assertEquals(((OperatorToken) tokens2.get(2)).getSource(),"==");

        assertEquals(((OperatorToken) tokens2.get(4)).getPrefixOp(),ot.getUMinus());
        assertEquals(((OperatorToken) tokens2.get(4)).getBinaryOp(),ot.getSubtract());
        assertEquals(((OperatorToken) tokens2.get(4)).getSource(),"-");

    }
}
