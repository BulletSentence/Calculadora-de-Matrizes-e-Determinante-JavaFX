/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static java.lang.System.out;
import static org.junit.Assert.assertArrayEquals;
import static org.junit.Assert.assertEquals;
import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertNotSame;
import static org.junit.Assert.assertTrue;

import java.util.Vector;

import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeAssign;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeEle;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeEvaluator;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeRandom;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.walkers.ImportationVisitor;
import com.singularsys.jep.walkers.SerializableExpression;
import com.singularsys.jep.walkers.TreeAnalyzer;
import com.singularsys.jepexamples.diagnostics.Utils;

public class ThreadTest {

	@Test
	public void testThreads() throws Exception {
		Jep baseJep = new Jep();
 
		Jep lwj = new Jep(new LightWeightComponentSet(baseJep));
		assertFalse(lwj.getEvaluator() instanceof ThreadSafeEvaluator);
		assertTrue(lwj.getEvaluator() instanceof FastEvaluator);
		assertNotSame(baseJep.getEvaluator(),lwj.getEvaluator());
		assertNotSame(baseJep.getVariableTable(),lwj.getVariableTable());
		Node n = baseJep.parse("y=x^2;");
		SerializableExpression se = new SerializableExpression(n);
		Node m = se.toNode(lwj);
		baseJep.addVariable("x", 3.0);
		lwj.addVariable("x", 5.0);
		Object val1 = baseJep.evaluate(n);
		Object val2 = lwj.evaluate(m);
		assertEquals(9.0,(Double) val1,1e-6);
		assertEquals(25.0,(Double) val2,1e-6);

		Node n2 = baseJep.parse("z=[x,y];");
		Node m2 = (new SerializableExpression(n2)).toNode(lwj);
		Object val3 = baseJep.evaluate(n2);
		Object val4 = lwj.evaluate(m2);
		Object[] ar1 = new Object[] { Double.valueOf(3.0), Double.valueOf(9.0) };
		Object[] ar2 = ((Vector<?>) val3).toArray();
		assertArrayEquals(ar1,ar2);
		Object[] ar3 = new Object[] { Double.valueOf(5.0), Double.valueOf(25.0) };
		Object[] ar4 = ((Vector<?>) val4).toArray();
		assertArrayEquals(ar3,ar4);

		Node n3 = baseJep.parse("z[1]=7;");
		Node m3 = (new SerializableExpression(n3)).toNode(lwj);
		Object val5 = baseJep.evaluate(n3);
		Object val6 = lwj.evaluate(m3);
		assertEquals(7.0, val5);
		assertEquals(7.0, val6);

		Object val7 = baseJep.getVariableValue("z");
		Object val8 = lwj.getVariableValue("z");
		Object[] ar5 = new Object[] { Double.valueOf(7.0), Double.valueOf(9.0) };
		Object[] ar6 = ((Vector<?>) val7).toArray();
		Object[] ar7 = new Object[] { Double.valueOf(7.0), Double.valueOf(25.0) };
		Object[] ar8 = ((Vector<?>) val8).toArray();
		assertArrayEquals(ar5,ar6);
		assertArrayEquals(ar7,ar8);

	}

	@Test
	public void testImportation() throws Exception {
		Jep baseJep = new Jep();
		Jep lwj = new Jep(new LightWeightComponentSet(baseJep));
		assertFalse(lwj.getEvaluator() instanceof ThreadSafeEvaluator);
		assertTrue(lwj.getEvaluator() instanceof FastEvaluator);
		assertNotSame(baseJep.getEvaluator(),lwj.getEvaluator());
		assertNotSame(baseJep.getVariableTable(),lwj.getVariableTable());
		ImportationVisitor iv = new ImportationVisitor(lwj);
		Node n = baseJep.parse("y=x^2;");
		Node m = iv.deepCopy(n);
		baseJep.addVariable("x", 3.0);
		lwj.addVariable("x", 5.0);
		Object val1 = baseJep.evaluate(n);
		Object val2 = lwj.evaluate(m);
		assertEquals(9.0,(Double) val1,1e-6);
		assertEquals(25.0,(Double) val2,1e-6);

		Node n2 = baseJep.parse("z=[x,y];");
		Node m2 = (new SerializableExpression(n2)).toNode(lwj);
		Object val3 = baseJep.evaluate(n2);
		Object val4 = lwj.evaluate(m2);
		Object[] ar1 = new Object[] { Double.valueOf(3.0), Double.valueOf(9.0) };
		Object[] ar2 = ((Vector<?>) val3).toArray();
		assertArrayEquals(ar1,ar2);
		Object[] ar3 = new Object[] { Double.valueOf(5.0), Double.valueOf(25.0) };
		Object[] ar4 = ((Vector<?>) val4).toArray();
		assertArrayEquals(ar3,ar4);

		Node n3 = baseJep.parse("z[1]=7;");
		Node m3 = (new SerializableExpression(n3)).toNode(lwj);
		Object val5 = baseJep.evaluate(n3);
		Object val6 = lwj.evaluate(m3);
		assertEquals(7.0, val5);
		assertEquals(7.0, val6);

		Object val7 = baseJep.getVariableValue("z");
		Object val8 = lwj.getVariableValue("z");
		Object[] ar5 = new Object[] { Double.valueOf(7.0), Double.valueOf(9.0) };
		Object[] ar6 = ((Vector<?>) val7).toArray();
		Object[] ar7 = new Object[] { Double.valueOf(7.0), Double.valueOf(25.0) };
		Object[] ar8 = ((Vector<?>) val8).toArray();
		assertArrayEquals(ar5,ar6);
		assertArrayEquals(ar7,ar8);

	}
	
	@Test
	public void testThreadSafeEvaluator() throws Exception {
		Jep baseJep = new Jep(new ThreadSafeEvaluator());
		baseJep.getOperatorTable().getAssign().setPFMC(new ThreadSafeAssign());
		baseJep.getOperatorTable().getEle().setPFMC(new ThreadSafeEle());
		baseJep.addFunction("random",new ThreadSafeRandom());
		
		Jep lwj = new Jep(new LightWeightComponentSet(baseJep));
		assertTrue(lwj.getEvaluator() instanceof ThreadSafeEvaluator);
		assertNotSame(baseJep.getEvaluator(),lwj.getEvaluator());
		assertNotSame(baseJep.getVariableTable(),lwj.getVariableTable());
		Node n = baseJep.parse("y=x^2;");
		baseJep.addVariable("x", 3.0);
		lwj.addVariable("x", 5.0);
		Object val1 = baseJep.evaluate(n);
		Object val2 = lwj.evaluate(n);
		assertEquals(9.0,(Double) val1,1e-6);
		assertEquals(25.0,(Double) val2,1e-6);
		
		Node n2 = baseJep.parse("z=[x,y];");
		Object val3 = baseJep.evaluate(n2);
		Object val4 = lwj.evaluate(n2);
		Object[] ar1 = new Object[] { Double.valueOf(3.0), Double.valueOf(9.0) };
		Object[] ar2 = ((Vector<?>) val3).toArray();
		assertArrayEquals(ar1,ar2);
		Object[] ar3 = new Object[] { Double.valueOf(5.0), Double.valueOf(25.0) };
		Object[] ar4 = ((Vector<?>) val4).toArray();
		assertArrayEquals(ar3,ar4);
		
		Node n3 = baseJep.parse("z[1]=7;");
		Object val5 = baseJep.evaluate(n3);
		Object val6 = lwj.evaluate(n3);
		assertEquals(7.0, val5);
		assertEquals(7.0, val6);
		
		Object val7 = baseJep.getVariableValue("z");
		Object val8 = lwj.getVariableValue("z");
		Object[] ar5 = new Object[] { Double.valueOf(7.0), Double.valueOf(9.0) };
		Object[] ar6 = ((Vector<?>) val7).toArray();
		Object[] ar7 = new Object[] { Double.valueOf(7.0), Double.valueOf(25.0) };
		Object[] ar8 = ((Vector<?>) val8).toArray();
		assertArrayEquals(ar5,ar6);
		assertArrayEquals(ar7,ar8);
		
	}
	
	@Test
	public void testConvertSpeed() throws Exception {
		out.println("Speeds for conversion of expression using ImportationVisitor/SerializableExpression");
		String expression = Utils.hornerExpression("x", 100);
		Jep baseJep = new Jep(new StandardConfigurableParser());
		Jep childJep = new Jep(new LightWeightComponentSet(baseJep));
		ImportationVisitor iv = new ImportationVisitor(childJep);

		long t0 = System.currentTimeMillis();
		Node n1 = baseJep.parse(expression);
		long t1 = System.currentTimeMillis();
		out.format("Parse %d%n",t1-t0);
		out.println((new TreeAnalyzer(n1)).summary());
		
		SerializableExpression se = new SerializableExpression(n1);
		long t2 = System.currentTimeMillis();

		@SuppressWarnings("unused")
        Node n2 = se.toNode(childJep);
		long t3 = System.currentTimeMillis();
		out.format("Serialize %d deserialize %d both %d%n",t2-t1,t3-t2,t3-t1);
		
		@SuppressWarnings("unused")
        Node n3 = iv.deepCopy(n1);
		long t4 = System.currentTimeMillis();
		out.format("Import %d%n",t4-t3);
	}

    // Setup and run multiple threads using the same expression
    public void go(String expression, int nThreads) throws JepException {
        // create a Jep instance with the ThreadSafeEvaluator
        Jep baseJep = new Jep(new ThreadSafeEvaluator());
        
        // use thread-safe versions of the assignment and element-of operators
        baseJep.getOperatorTable().getAssign().setPFMC(new ThreadSafeAssign());
        baseJep.getOperatorTable().getEle().setPFMC(new ThreadSafeEle());
        
        // Parse a node in the base Jep instance
        Node baseNode = baseJep.parse(expression);
             
        // create and run a number of threads each with a different value for x
        EvaluationThread threads[] = new EvaluationThread[nThreads];
        for(int i=0; i < nThreads; ++i) {
            threads[i] = new EvaluationThread(baseJep,baseNode,"x", Math.PI * i / nThreads);
        }

        // create and run a number of threads each with a different value for x
        for(int i=0; i < nThreads; ++i) {
            threads[i].start();
        }

        // wait for all threads to finish and print results
        for(int i=0; i < nThreads; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
            System.out.println("Thread "+i+" value "+ threads[i].varValue+" result "+threads[i].result);
        }
    }
        
    // Class to evaluate an expression in a thread
    static class EvaluationThread extends Thread {
        Jep childJep;
        Node childNode;
        Variable childVar;
        double varValue;
        double result;
        
        // set up the tread before running 
        EvaluationThread(Jep baseJep, Node baseNode, String varName, double value) {
            // create a child Jep instance
            childJep = new Jep(new LightWeightComponentSet(baseJep));
            // just use the baseNode node
            childNode = baseNode;
            // child copy of variable
            childVar = childJep.addVariable(varName);
            varValue = value;
        }
        
        // Run the thread
        @Override
        public void run() {
            try {
            	childVar.setValue(varValue);
                // Evaluate the expression            	
                Object res = childJep.evaluate(childNode);
                result = ((Double) res);
            } catch (JepException e) {
                System.out.println(e.getMessage());
            }
        }
   }


	@Test
	public void testDoc1() throws Exception {
		this.go("x - x^3/ (3*2) + x^5 / (5*4*3*2) ", 4);
	}
	
    // Setup and run multiple threads using the same expression
    public void go2(String expression, int nThreads) throws JepException {
        // create a standard Jep
        Jep baseJep = new Jep();
                
        // Parse a node in the base Jep instance
        Node baseNode = baseJep.parse(expression);
             
        // create a number of threads each with a different value for x
        EvaluationThread2 threads[] = new EvaluationThread2[nThreads];
        for(int i=0; i<nThreads; ++i) {
            threads[i] = new EvaluationThread2(baseJep,baseNode,"x", Math.PI * i / nThreads);
        }

        // run the threads each with a different value for x
        for(int i=0; i<nThreads; ++i) {
            threads[i].start();
        }
        
        // wait for all threads to finish and print results
        for(int i=0; i<nThreads; ++i) {
            try {
                threads[i].join();
            } catch (InterruptedException e) {
            }
            System.out.println("Thread "+i+" result "+threads[i].result);
        }
    }
        
    // Class to evaluate an expression in a thread
    class EvaluationThread2 extends Thread {
        Jep childJep;
        Node childNode;
        Variable childVar;
        double varValue;
        double result;
        
        // set up the tread before running 
        EvaluationThread2(Jep baseJep, Node baseNode, String varName, double value) throws JepException {
            // create a child Jep instance
            childJep = new Jep(new LightWeightComponentSet(baseJep));
            // use a child copy of expression
            childNode = (new ImportationVisitor(childJep)).deepCopy(baseNode);
            // child copy of variable
            childVar = childJep.addVariable(varName);
            varValue = value;
        }
        
        // Run the thread
        @Override
        public void run() {
            try {
                // set the variable value
                childVar.setValue(varValue);
                // Evaluate the expression
                Object res = childJep.evaluate(childNode);
                result = ((Double) res);
            } catch (EvaluationException e) {
                System.out.println(e.getMessage());
            }
        }
   }
    
	@Test
	public void testDoc2() throws Exception {
		this.go2("x - x^3/ (3*2) + x^5 / (5*4*3*2) ", 4);
	}

}
