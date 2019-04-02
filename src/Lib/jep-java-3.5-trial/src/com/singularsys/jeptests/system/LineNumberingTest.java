/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.*;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.configurableparser.ConfigurableParser;
import com.singularsys.jep.misc.lineNumbering.LineNumberingNodeFactory;
import com.singularsys.jep.misc.lineNumbering.LineNumberingShuntingYard;
import com.singularsys.jep.parser.Node;

public class LineNumberingTest {
	Jep jep;
	
	class Position {
		int line;
		int col;

		public Position(int line, int col) {
			this.line = line;
			this.col = col;
		}
		
		public Position(Node n) {
			this.line = (Integer) n.getHook(LineNumberingNodeFactory.Numbering.LINENUMBER);
			this.col = (Integer) n.getHook(LineNumberingNodeFactory.Numbering.COLUMNNUMBER);
		}

//		@Override
//		public boolean equals(Object arg) {
//			if(arg instanceof Position) {
//				Position that = (Position) arg;
//				return this.line == that.line && this.col == that.col;
//			}
//			return false;
//		}

		@Override
		public String toString() {
			return "{"+line+":"+col+"}";
		}

		@Override
		public int hashCode() {
			final int prime = 31;
			int result = 1;
			result = prime * result + col;
			result = prime * result + line;
			return result;
		}

		@Override
		public boolean equals(Object obj) {
			if (this == obj)
				return true;
			if (obj == null)
				return false;
			if (getClass() != obj.getClass())
				return false;
			Position other = (Position) obj;
			if (col != other.col)
				return false;
			if (line != other.line)
				return false;
			return true;
		}

	}

	@Before
	public void setUp() throws Exception {
		jep = new Jep(new StandardConfigurableParser(),new LineNumberingNodeFactory());
		((ConfigurableParser) jep.getParser()).setGrammarParserFactory(
				new LineNumberingShuntingYard.LineNumberGrammarParserFactory());
	}

	@Test
	public void testSimple() throws ParseException {
		Node n = jep.parse("1+x");
		checkPosition(n,                1, 2); // plus operator should be at 2
		checkPosition(n.jjtGetChild(0), 1, 1); // 1 (should be at 1)
		checkPosition(n.jjtGetChild(1), 1, 3); // x (should be at 3)

		//                   1234567890123 
		Node n2 = jep.parse("aa+bb*(cc - dd)");
		checkPosition(n2.jjtGetChild(0), 1, 1); // aa
		checkPosition(n2,                1, 3); // +
		checkPosition(n2.jjtGetChild(1).jjtGetChild(0), 1, 4); // bb
		checkPosition(n2.jjtGetChild(1), 1, 6); // *
		checkPosition(n2.jjtGetChild(1).jjtGetChild(1).jjtGetChild(0), 1, 8); // cc
		checkPosition(n2.jjtGetChild(1).jjtGetChild(1), 1, 11); // -
		checkPosition(n2.jjtGetChild(1).jjtGetChild(1).jjtGetChild(1), 1, 13); // dd
	}

	@Test
	public void testMultiLine() throws ParseException {
		//                  123456789   123456789012
		jep.initMultiParse("x=5; y=6; \nx+y;");
		Node n1 = jep.continueParsing();
		checkPosition(n1.jjtGetChild(0), 1, 1); // x
		Node n2 = jep.continueParsing();
		checkPosition(n2.jjtGetChild(0), 1, 6); // y
		Node n3 = jep.continueParsing();
		checkPosition(n3,                2, 2); // +
	}	

	@Test
	public void testFunction() throws ParseException {
		//                  123456789
		Node n = jep.parse("atan2(y,x)");
		checkPosition(n,                1, 1);
		checkPosition(n.jjtGetChild(0), 1, 7);
		checkPosition(n.jjtGetChild(1), 1, 9);

		//                   1234567890123456789012
		Node n2 = jep.parse("atan2(sin(th),cos(th))");
		checkPosition(n2,                1, 1); // atan
		checkPosition(n2.jjtGetChild(0), 1, 7); // sin
		checkPosition(n2.jjtGetChild(0).jjtGetChild(0), 1, 11); // th
		checkPosition(n2.jjtGetChild(1), 1, 15); // cos
		checkPosition(n2.jjtGetChild(1).jjtGetChild(0), 1, 19); // th
	}

	@Test
	public void testArray() throws ParseException {
		//                  123456789
		Node n = jep.parse("u=[1,2,3]");
		checkPosition(n.jjtGetChild(0), 1, 1); // u
		checkPosition(n.jjtGetChild(1), 1, 3); // [ ]
		checkPosition(n.jjtGetChild(1).jjtGetChild(0), 1, 4); // 1
		checkPosition(n.jjtGetChild(1).jjtGetChild(1), 1, 6); // 2
		checkPosition(n.jjtGetChild(1).jjtGetChild(2), 1, 8); // 3
		
		//                   123
		Node n2 = jep.parse("u[2]");
		checkPosition(n2, 1, 1); // u[
		checkPosition(n2.jjtGetChild(0), 1, 1); // u[
		checkPosition(n2.jjtGetChild(1), 1, 3); // u[
	}

	@Test
	public void testMatrix() throws ParseException {
		//                  123456789012
		Node n = jep.parse("[[1,2],[3,4]]");
		checkPosition(n,                               1,  1); // [[
		checkPosition(n.jjtGetChild(0),                1,  2); // [1,2]
		checkPosition(n.jjtGetChild(0).jjtGetChild(0), 1,  3); // 1
		checkPosition(n.jjtGetChild(0).jjtGetChild(1), 1,  5); // 2
		checkPosition(n.jjtGetChild(1),                1,  8); // [1,2]
		checkPosition(n.jjtGetChild(1).jjtGetChild(0), 1,  9); // 1
		checkPosition(n.jjtGetChild(1).jjtGetChild(1), 1, 11); // 2
	}

	@Test
	public void testBrackets() throws ParseException {
		//                  123456789
		Node n = jep.parse("(((x)))");
		checkPosition(n, 1, 4);
	}

	@Test
	public void testExpressionsSpanningLines() throws ParseException {
		Node n = jep.parse("1\n+2");
		checkPosition(n,                2, 1); //+
		checkPosition(n.jjtGetChild(0), 1, 1); //1
		checkPosition(n.jjtGetChild(1), 2, 2); //2
	}

	/**
	 * Utility method to check whether the node has the expected column and row.
	 * @param n
	 * @param expectedRow
	 * @param expectedCol
	 */
	private void checkPosition(Node n, int expectedRow, int expectedCol)
	{
		Position actual = new Position(n);
		assertEquals(new Position(expectedRow, expectedCol), actual);
	}
}
