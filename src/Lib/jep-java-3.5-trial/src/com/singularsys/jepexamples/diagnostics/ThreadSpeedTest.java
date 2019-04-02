/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 2 Aug 2006 - Richard Morris
 */
package com.singularsys.jepexamples.diagnostics;

import com.singularsys.jep.ComponentSet;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeRandom;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.ImportationVisitor;
import com.singularsys.jep.walkers.SerializableExpression;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * Tests evaluation using multiple threads.
 * Subclasses can override methods to change how threads are constructed.
 * 
 * @author Richard Morris
 */
public class ThreadSpeedTest {
	/** Number of terms in the expressions */
	public static final int N_TERMS = 20;
	/** Number of loops to run */
	public static final int MAX_LOOPS = 5;
	/** Total number of iterations for each step */
	public static final int TOTAL_ITTS = 500000;
	/** Maximum number of threads to test. */
	public static final int MAX_THREADS=8;
	
	/**
	 * Base Jep instance
	 */
	protected Jep baseJep;
	/**
	 * Parsed expression
	 */
	protected Node base;
	/**
	 * Name of variable
	 */
	protected String varName;
	

	/**
	 * Do nothing constructor for use by subclasses
	 */
	protected ThreadSpeedTest() { 
		// does nothing	
	}
	
	 /**
	  * @param expression the expression
	  * @param varName name of the variable
	  */
	public ThreadSpeedTest(String expression,String varName) throws ParseException {
		baseJep = new Jep();
		baseJep.addFunction("rand", new ThreadSafeRandom());
		base = baseJep.parse(expression);
		this.varName = varName;
		baseJep.println(base);
		analyize();
	}


	/**
	 * Create and run threads.
	 * Calls <code>makeThread</code> to construct threads, 
	 * then runs the threads simultaneously and calculated total time of execution. 
	 * @param nThreads number of threads to run
	 * @param nItts number of iterations per thread
	 * @param minValue minimum variable value
	 * @param maxValue maximum variable value
	 * @return time taken to evaluate, does not include construction time
	 * @throws JepException
	 */
	public int go(int nThreads,int nItts,double minValue,double maxValue) throws JepException
	{
//		System.gc();
		long t0 = System.currentTimeMillis();
		Thread et[] = new Thread[nThreads];

		// create the threads
		for(int i=0;i<nThreads;++i)
		{
			et[i] = makeThread(i,minValue,maxValue,nItts);
		}

		long t1 = System.currentTimeMillis();
		System.out.println("Build time "+(t1-t0));
		System.gc();
		printMem("start      ");

		// Start all the threads
		for(int i=0;i<nThreads;++i)
			et[i].start();

		// now wait for threads to finish
		for(int i=0;i<nThreads;++i) {
			try {
				et[i].join();
			} catch (InterruptedException e) {
				System.out.println("thread "+i+" interrupted "+e.getMessage());
			}
		}

		long t2 = System.currentTimeMillis();
		printMem("done                  ");

		for(int i=0;i<nThreads;++i)
			et[i]=null;

		System.gc();
		printMem("Garbage collected     ");

		return (int) (t2-t1);
	}

	/**
	 * Make a thread to evaluate an expression multiple times.
	 * Can be overridden to change how threads are created.
	 * This version uses {@link EvaluationThread}
	 * @param index the number of this thread
	 * @param min minimum value for variable
	 * @param max maximum value for variable
	 * @param nItts  number of iterations
	 * @return the thread
	 * @throws JepException 
	 */
	public Thread makeThread(int index,double min,double max,int nItts) throws JepException {
		EvaluationThread et = new EvaluationThread(index,baseJep,base,varName,min,max,nItts);
		return et;
	}

	/**
	 * Repeatedly execute threads. 
	 * First run with 1 thread, then with 2 thread and so on upto maxThreads threads.
	 * The total iterations are divided among threads.
	 * Repeats the whole process nLoops times. 
	 * Time statistics are gathered from all but the first loop and summary statistics 
	 * printed on completion.
	 * @param maxThreads
	 * @param totalItts
	 * @param nLoops number of times 
	 */
	public void loop(int maxThreads, int totalItts,int nLoops) {
		int[] totals=new int[maxThreads+1];

		for(int loop=0;loop<nLoops;++loop) {
			System.out.println("Loop "+loop);
			for(int nthreads=1;nthreads<=maxThreads;++nthreads) {
				int ittsPerThread = totalItts / nthreads;
				int time=0;
				try {
					time = go(nthreads, ittsPerThread, -1, 1);
				} catch (JepException e) {
					System.out.println(e);
				}
				System.out.format("Number of threads %d itts per thread %d total time %d%n%n", nthreads,ittsPerThread, time);
				if(loop>0)
					totals[nthreads] += time;
			}
		}

		for(int nthreads=1;nthreads<=maxThreads;++nthreads) {
			System.out.format("Number of threads %d  total time %d%n", nthreads, totals[nthreads]);
		}
	}

	/**
	 * Print the used memory
	 * @param string message to display with memory.
	 */
	void printMem(String string) {
		Runtime rt = Runtime.getRuntime();
		System.out.println(string + " Used mem "+ (rt.totalMemory()-rt.freeMemory()));
	}
	
	/**
	 * Prints some basic info about the expression
	 */
	public void analyize() {
		try {
			TreeAnalyzer ta = new TreeAnalyzer(base);
			System.out.println(ta.summary());
		} catch (JepException e) {
			System.out.println(e);
		}
	}


	/**
	 * A thread which evaluates an expression multiple times.
	 * This version uses LightWeightComponentSet and SerializableExpression.
	 * @see SerializableExpression
	 * @see LightWeightComponentSet
	 */
	public static class EvaluationThread extends Thread {
		final Jep localJep;
		final Node myExpression;
		final double min,max;
		final int itts;
		final Variable myVar;
		//        double results[];
		int n;

		/**
		 * Construct a thread.
		 * Uses <code>new Jep(new LightWeightComponentSet(baseJep))</code>
		 * to create a local Jep instance and
		 * <code>(new ImportationVisitor(localJep)).deepCopy(baseExpression)</code>
		 * to create a local copy of the expression.
		 * 
		 * @param index the number of this thread
		 * @param baseJep the base Jep instance
		 * @param baseExpression the expression
		 * @param varName name of the variable
		 * @param minValue minimum value for variable
		 * @param maxValue maximum value for variable
		 * @param numItts  number of iterations
		 * @throws JepException
		 */
		public EvaluationThread(int index,Jep baseJep,Node baseExpression,String varName,
				double minValue,double maxValue,int numItts) 
						throws JepException
		{
			ComponentSet cs = new LightWeightComponentSet(baseJep);
			localJep = new Jep(cs);

//			SerializableExpression se = new SerializableExpression(baseExpression);
//			myExpression = se.toNode(localJep);
			myExpression = (new ImportationVisitor(localJep)).deepCopy(baseExpression);
			myVar = localJep.addVariable(varName);

			min = minValue;
			max = maxValue;
			itts = numItts;
			n=index;
		}

		/**
		 * Evaluates the expression numItts times.
		 */
		@Override
		public void run() {
			System.out.println("Thread "+n+" started");
			try {
				for(int i=0;i<itts;++i)
				{
					double value = min+(max-min)*(i)/(itts-1);
					myVar.setValue(value);
					// Object resObj = 
					localJep.evaluate(myExpression);
					// results[i] = ((Double) resObj);
				}
			} catch (EvaluationException e) {
				e.printStackTrace();
			}
			System.out.println("Thread "+n+" finished");
		}

		//        public double[] getResults() {
		//            return results;
		//        }
	}



	/**
	 * Gets a expression to evaluate using an approximation to log "x^1/1-x^2/2+x^3/3-..."
	 * @param nTerms number of terms
	 * @return a string with the expression
	 */
	static public String getExpression(int nTerms) {
		StringBuilder sb = new StringBuilder();
		for(int i=1;i<=nTerms;++i)
		{
			if(i%2==0)
				sb.append("-");
			else
				sb.append("+");
			sb.append("x^"+i+"/"+i);
		}
		return sb.toString();
	}

    /**
	 * Run the thread test program.
	 * @param args If specified args[0] is maximum number of threads, args[1] is total number of iterations
	 */
	public static void main(String[] args) {

		int maxThread = MAX_THREADS;
		int totalItts = TOTAL_ITTS;
		if(args.length>=2) {
			maxThread = Integer.parseInt(args[0]);
			totalItts = Integer.parseInt(args[1]);
		}
		String s = Utils.hornerExpression("x",N_TERMS);
		//s = "rand()";
		ThreadSpeedTest tt=null;
		try {
			tt = new ThreadSpeedTest(s,"x");
		} catch (ParseException e1) {
			e1.printStackTrace();
			return;
		}
		
		tt.loop(maxThread, totalItts,MAX_LOOPS);
	}
}
