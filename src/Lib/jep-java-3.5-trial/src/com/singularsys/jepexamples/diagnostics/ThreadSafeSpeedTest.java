/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.diagnostics;

import com.singularsys.jep.ComponentSet;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeEvaluator;
import com.singularsys.jep.parser.Node;

/**
 * A diagnostic application using the {@link ThreadSafeEvaluator}
 * and a variable number of threads.
 */
public class ThreadSafeSpeedTest extends ThreadSpeedTest {

	public ThreadSafeSpeedTest(String expression, String varName) throws ParseException {
		super(expression, varName);
		baseJep.setComponent(new ThreadSafeEvaluator());
	}

	@Override
	public Thread makeThread(int index, double min, double max, int nItts)
			throws JepException {
		EvaluationThread et = new EvaluationThread(index,baseJep,base,varName,min,max,nItts);

		return et;
	}
	
	/**
	 * A thread which evaluates an expression multiple times.
	 * @see LightWeightComponentSet
	 */
	public static class EvaluationThread extends Thread {
		final Jep localJep;
		final Node myExpression;
		final double min,max;
		final int itts;
		final Variable myVar;
		//        double results[];
		double sum;
		double sumsq;
		int n;

		/**
		 * Construct a thread.
		 * Uses <code>new Jep(new LightWeightComponentSet(baseJep))</code>
		 * to create a local Jep instance.
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

			myExpression = baseExpression;
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
			sum = 0;
			sumsq = 0;
			try {
				for(int i=0;i<itts;++i)
				{
					double value = min+(max-min)*(i)/(itts-1);
					myVar.setValue(value);
					// Object resObj = 
					double val = (Double) localJep.evaluate(myExpression);
					sum += val;
					sumsq += val*val;
				}
			} catch (EvaluationException e) {
				e.printStackTrace();
			}
			double var = sumsq / itts - (sum/itts)*(sum/itts);
			System.out.println("Thread "+n+" finished. Mean "+(sum/itts)+" sd "+Math.sqrt(var));
		}
	}

	
	/**
	 * @param args
	 */
	public static void main(String[] args) {
		int maxThread = MAX_THREADS;
		int totalItts = TOTAL_ITTS;
		if(args.length>=2) {
			maxThread = Integer.parseInt(args[0]);
			totalItts = Integer.parseInt(args[1]);
		}
		String expr = Utils.hornerExpression("x",N_TERMS);
		//expr = "rand()";
		ThreadSafeSpeedTest ts=null;
		try {
			ts = new ThreadSafeSpeedTest(expr,"x");
			ts.loop(maxThread, totalItts,MAX_LOOPS);
		} catch (ParseException e1) {
			e1.printStackTrace();
			return;
		}
		
	}

}
