/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jepexamples.diagnostics;

import com.singularsys.jep.ComponentSet;
import com.singularsys.jep.Evaluator;
import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.NodeFactory;
import com.singularsys.jep.NumberFactory;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Parser;
import com.singularsys.jep.PrintVisitor;
import com.singularsys.jep.VariableFactory;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.misc.NullParser;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.StandardParser;
import com.singularsys.jep.standard.DoubleNumberFactory;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.standard.StandardFunctionTable;
import com.singularsys.jep.standard.StandardOperatorTable2;
import com.singularsys.jep.standard.StandardVariableTable;
import com.singularsys.jep.walkers.SerializableExpression;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * Used to examine the memory used to initialise a Jep instance.
 * Typical values are
 * <ul>
 * <li>With a StandardParser: 56661
 * <li>With a ConfigurableParser: 14033
 * <li>With a NullParser: 5265
 * </ul>
 */
public class ExamineSizes {
	static final int N=100;
	enum ParserType { STANDARD, CONFIG, NULL }
	static ParserType pt = ParserType.CONFIG;

	void run() {
		printMem("Memory footprint before any Jep classes ");

		NumberFactory nuf[]=new NumberFactory[N];
		VariableFactory vf[]=new VariableFactory[N];
		NodeFactory nf[] = new NodeFactory[N];
		FunctionTable[] ft = new FunctionTable[N];
		FunctionTable[] ft2 = new FunctionTable[N];
		VariableTable vt[] = new VariableTable[N];
		OperatorTableI ot[] = new OperatorTableI[N];
		Parser p[] = new Parser[N];
		Parser p1[] = new Parser[N];
		Parser p2[] = new Parser[N];
		Evaluator e[] = new Evaluator[N];
		Evaluator e2[] = new Evaluator[N];
		PrintVisitor pv[] = new PrintVisitor[N];
		Jep j[] = new Jep[N];
		Jep j2[] = new Jep[N];
		Jep j3[] = new Jep[N];
		Jep j4[] = new Jep[N];
		Jep j5[] = new Jep[N];
		ComponentSet cs = new ComponentSet();

		printMem("Init arrays to store "+N+" instances of each component");

		System.out.println("\nIndividual components:\n");
		for(int i=0;i<N;++i)
			nuf[i] = new DoubleNumberFactory();
		printMem("DoubleNumberFactory");

		for(int i=0;i<N;++i)
			vf[i] = new VariableFactory();
		printMem("VariableFactory");

		for(int i=0;i<N;++i)
			nf[i] = new NodeFactory();
		printMem("NodeFactory");

		for(int i=0;i<N;++i)
			ft[i] = new StandardFunctionTable();
		printMem("StandardFunctionTable");

		for(int i=0;i<N;++i)
			ft2[i] = new FunctionTable();
		printMem("FunctionTable");

		for(int i=0;i<N;++i)
			vt[i] = new StandardVariableTable(vf[i]);
		printMem("StandardVariableTable");

		for(int i=0;i<N;++i)
			ot[i] = new StandardOperatorTable2();
		printMem("StandardOperatorTable2");


		for(int i=0;i<N;++i)
			p[i] = new StandardParser();
		printMem("StandardParser");

		for(int i=0;i<N;++i)
			p1[i] = new StandardConfigurableParser();
		printMem("ConfigurableParser");

		for(int i=0;i<N;++i)
			p2[i] = new NullParser();
		printMem("NullParser");


		for(int i=0;i<N;++i)
			e[i] = new StandardEvaluator();
		printMem("StandardEvaluator");

		for(int i=0;i<N;++i)
			e2[i] = new FastEvaluator();
		printMem("FastEvaluator");

		for(int i=0;i<N;++i)
			pv[i] = new PrintVisitor();
		printMem("PrintVisitor");

		System.out.println();
		for(int i=0;i<N;++i) {
			cs.setNumberFactory(nuf[i]);
			cs.setVariableFactory(vf[i]);
			cs.setNodeFactory(nf[i]);
			cs.setVariableTable(vt[i]);
			cs.setFunctionTable(ft[i]);
			cs.setOperatorTable(ot[i]);
			cs.setEvaluator(e[i]);
			cs.setParser(p[i]);
			cs.setPrintVisitor(pv[i]);
			j[i] = new Jep(cs);
		}
		printMem("Jep with existing comps");

		for(int i=0;i<N;++i) {
			j3[i] = new Jep();
		}
		printMem("Jep");

		for(int i=0;i<N;++i) {
			//j4[i] = new Jep(new MinimalComponentSet());
			j4[i] = new Jep(new FunctionTable(),new StandardConfigurableParser());
		}
		printMem("Jep minimal");

		for(int i=0;i<N;++i) {
			j5[i] = new Jep(new FunctionTable(),new NullParser());
		}
		printMem("Jep minimal, null parser");

		for(int i=0;i<N;++i) {
			LightWeightComponentSet lwcs = new LightWeightComponentSet(j[i]);
			j2[i] = new Jep(lwcs);
		}
		printMem("LightWeightJep");
		System.out.println();
		
		String expr = getExpression(50);
		Node[] nodes = new Node[N];        
		try {
			for(int i=0;i<N;++i) {
				nodes[i] = j[i].parse(expr);
			}
		} catch (ParseException e1) {
			e1.printStackTrace();
		}
		printMem("Parse expression");
		int numnodes=1;
		try {
			TreeAnalyzer ta = new TreeAnalyzer(nodes[0]);
			System.out.println(ta.summary());
			numnodes = ta.getNumNodes();
		} catch (JepException e1) {
			e1.printStackTrace();
		}
		System.out.printf("Avg per node %3.1f%n",(this.curMem/(N*50.0d*numnodes)));
		readMem();
		
		SerializableExpression[] se = new SerializableExpression[N];
		try {
			for(int i=0;i<N;++i) {
				se[i] = new SerializableExpression(nodes[i]);
			}
		} catch (JepException e1) {
			e1.printStackTrace();
		}
		printMem("SerializableExpression ");
		System.out.printf("Avg per node %3.1f%n",(this.curMem/(N*50.0d*numnodes)));
			

	}

	public String getExpression(int nDeriv) {
		StringBuilder sb = new StringBuilder();
		for(int i=1;i<=nDeriv;++i)
		{
			if(i%2==0)
				sb.append("-");
			else
				sb.append("+");
			sb.append("x^"+i+"/"+i);
		}
		return sb.toString();
	}

	long curMem=0;

	long readMem() {
		curMem = getMem();
		return curMem;
	}
	long getMem() {
		Runtime rt = Runtime.getRuntime();
		rt.gc();
		long newMem = rt.totalMemory()-rt.freeMemory();
		return newMem;
	}

	long printMem(String s) {
		long newMem = getMem();
		//		System.out.println(
		//			"Total "+rt.totalMemory()+" free "+rt.freeMemory()+" max "+rt.maxMemory()+" diff "+newMem);
		System.out.printf("%-25s%8d avg%8d%n",s, (newMem-curMem), (newMem-curMem)/N);
		//    	System.out.println(s+"\t"+ (newMem-curMem));
		curMem = newMem;
		return newMem;
	}


	/**
	 * @param args
	 */
	public static void main(String[] args) {
		ExamineSizes ex = new ExamineSizes();
		ex.run();
	}

}
