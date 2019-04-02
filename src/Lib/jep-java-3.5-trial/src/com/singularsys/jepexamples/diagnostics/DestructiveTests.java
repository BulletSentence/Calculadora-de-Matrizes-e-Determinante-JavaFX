/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.diagnostics;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.walkers.DeepCopyVisitor;
import com.singularsys.jep.walkers.DoNothingVisitor;
import com.singularsys.jep.walkers.PostfixEvaluator;
import com.singularsys.jep.walkers.TreeAnalyzer;

import static java.lang.System.out;
/**
 * Test various features until failure.
 * Tests parsing with standard and configurable parser
 * and evaluation with FastEvaluator, StandardEvaluator and PostfixEvaluator.
 * @author Richard Morris
 * @since Jep 3.5
 */
public class DestructiveTests {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		String expr = Utils.hornerExpression("x", 5);
		out.println("Parsing and evaluating expressions of the form "+expr);
		out.println();
		checkParseSize();
		checkCPParseSize();
		checkEval();
		checkPostfixEval();
		checkStandardEval();

	}

	public static void checkParseSize() {
		out.println("Parsing with standard parser");
		int nterms=100;
		Jep jep = new Jep();
		while(true) {
			String expr = Utils.hornerExpression("x", nterms);
			try {
				long t1 = System.currentTimeMillis();
				Node n = jep.parse(expr);
				long t2 = System.currentTimeMillis();
				TreeAnalyzer ta = new TreeAnalyzer(n);
				out.format("Parse %d terms %d nodes time %dms%n", nterms,ta.getNumNodes(),t2-t1);
			} catch (Throwable e) {
				out.println("Stack overflow encountered\n");
				break;
			}
			nterms+=100;
		}
	}

	public static void checkCPParseSize() {
		out.println("Parsing with configurable parser");
		int nterms=100;
		Jep jep = new Jep(new StandardConfigurableParser());
		while(true) {
			String expr = Utils.hornerExpression("x", nterms);
			try {
				long t1 = System.currentTimeMillis();
				Node n = jep.parse(expr);
				long t2 = System.currentTimeMillis();
				TreeAnalyzer ta = new TreeAnalyzer(n);
				out.format("Parse %d terms %d nodes time %dms%n", nterms,ta.getNumNodes(),t2-t1);
			} catch (Throwable e) {
//				StackTraceElement[] frames = e.getStackTrace();
				out.println("Stack overflow encountered");
				//length "+frames.length+"\n");
				//out.println(frames[0]);
//				for(int i=frames.length-1;i>frames.length-10;--i)
//				    out.println(frames[i]);
				break;
			}
			nterms+=100;
		}
		out.println();
	}
	
	static class NonCopySubVisitor extends DoNothingVisitor {

		Node replacement;
		String name;
		private static final long serialVersionUID = 1L;
		
		
		public NonCopySubVisitor(Jep j) {
			super(j);
		}
		
		@SuppressWarnings("hiding") 
		Node sub(Node main,String name,Node replacement) throws JepException {
			this.name = name;
			this.replacement = replacement;
			return this.visit(main);
		}

		@Override
		public Object visit(ASTVarNode node, Object data) throws JepException {
	        
	        if(name.equals(node.getName()))
	                return replacement;
	        
	        return node;
		}
		
	}

	public static void checkEval() {
		out.println("Evaluation with FastEvaluator");
		Jep jep = new Jep(new StandardConfigurableParser());
		checkEval(jep,100);
	}
	
	public static void checkPostfixEval() {
		out.println("Evaluation with PostfixEvaluator");
		Jep jep = new Jep(new StandardConfigurableParser(),new PostfixEvaluator());
		checkEval(jep,500);
	}

	public static void checkStandardEval() {
		out.println("Evaluation with StandardEvaluator");
		Jep jep = new Jep(new StandardConfigurableParser(),new StandardEvaluator());
		checkEval(jep,100);
	}

	public static void checkEval(Jep jep,int step) {
		jep.setVariable("x", Math.PI);
		String expr = Utils.recursiveHornerExpression("x","y", step);
		String expr2 = Utils.hornerExpression("x", step);
		try {
			Node main = jep.parse(expr2);
			Node sub  = jep.parse(expr);
			NonCopySubVisitor sv= new NonCopySubVisitor(jep);
			DeepCopyVisitor dcv= new DeepCopyVisitor(jep);
			while(true) {

			    	try {
					Node node = dcv.deepCopy(sub);
					main = sv.sub(node,"y",main);
				} catch(java.lang.StackOverflowError soe) {
					out.println("Stack overflow encountered in building expression\n");
					break;
				}
				long t1 = System.currentTimeMillis();
				try {
					jep.evaluate(main);
				} catch(java.lang.StackOverflowError soe) {
					out.println("Stack overflow encountered\n");
					break;
				}
				long t2 = System.currentTimeMillis();
				TreeAnalyzer ta = new TreeAnalyzer(main);
				out.format("Evaluate %d nodes, depth %d, time %dms%n",ta.getNumNodes(),ta.getMaxDepth(),t2-t1);
				if(ta.getNumNodes()>100000)
				{
					out.println("Stopped without error\n");
					return;
				}
			}
		} catch (Throwable e) {
			StackTraceElement[] frames = e.getStackTrace();
			for(int i=0;i<10;++i)
				out.println(frames[i]);
			out.println(e.toString());
			for(int i=frames.length-10;i<frames.length;++i)
				out.println(frames[i]);
		}
	}


}
