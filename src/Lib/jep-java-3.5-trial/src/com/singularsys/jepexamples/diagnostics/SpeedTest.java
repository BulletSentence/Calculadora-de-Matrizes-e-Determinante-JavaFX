/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /* @author rich
 * Created on 26-Feb-2004
 */

package com.singularsys.jepexamples.diagnostics;

import static java.lang.System.out;

import java.math.BigDecimal;
import java.math.MathContext;
import java.util.ArrayList;
import java.util.List;
import java.util.Random;

import com.singularsys.jep.Jep;
import com.singularsys.jep.Variable;
import com.singularsys.jep.bigdecimal.BigDecComponents;
import com.singularsys.jep.misc.threadsafeeval.ThreadSafeEvaluator;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.reals.RealEvaluator;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.standard.StandardEvaluator;
import com.singularsys.jep.walkers.TreeAnalyzer;

/**
 * Compares the speed of evaluation between different evaluation schemes.
 * The standard class compares BigDecimal, Jep (with default Fast evaluator), 
 * the old StandardEvaluator, and RealEvaluator.
 * <p>
 * If you have some nice complicated examples, I'd love to
 * hear about them to see if we can tune things up. - rich
 */
public class SpeedTest {
    public int num_itts = 100000; // for normal use
    //	static int num_itts = 100;	  // for use with profiler
    public int num_vals = 1000; // number of random numbers selected
    public int nDeriv = 20;
    static MathContext MC = MathContext.DECIMAL64;

    long seed; // seed for random number generator
    Random generator;


    protected final List<EvaluationConfig> configs = new ArrayList<>();
    protected Outputter outputter;
    protected long[] totalTimes;

    /**
     * @param outputter
     */
    public SpeedTest(Outputter outputter) {
        this.outputter = outputter;
        seed = System.currentTimeMillis();
    }

    public void init() {
        generator = new Random(seed);
        totalTimes = new long[configs.size()];
        outputter.printHeader(this);
    }

    public void fini() {
        outputter.printFooter(this, totalTimes);
    }

    /*** Different output methods ****************/

    public static class Outputter {
        Jep globalJep = new Jep();

        public void printHeader(SpeedTest st) {
            out.println("Performing "+st.num_itts+" iterations.");
            for(EvaluationConfig c :st.configs) {
                out.println(c.name + "\t" + c.description());
            }
        }
        
        /**
		 * @param st  
         * @param varNames 
		 */
        public void printOutputHeader(SpeedTest st,String eqn, String varNames[]) {
            out.println("\nTesting speed for \"" + eqn + "\"");
            try {
                Node node = globalJep.parse(eqn);
                TreeAnalyzer ta = new TreeAnalyzer(node);
                out.println(ta.summary());
            } catch (Exception e) {
                out.println(e.getMessage());
            }

        }

        /**
		 * @param eqn 
         * @param varNames  
		 */
        public void printOutputTimes(SpeedTest st,String eqn, String varNames[],long[] times) {
            for(int i=0;i<st.configs.size();++i) {
                out.println(st.configs.get(i).name+"\t"+times[i]);
            }
        }
        @SuppressWarnings("unused")
		public void printOutputHeader(SpeedTest st,String eqns[], String varNames[]) {
            out.print("\nTesting speed for ");
            for(int i=0;i<eqns.length;++i) {
                if(i>0) System.out.print(", ");
                out.print("\""+eqns[i] + "\"");
            }
            out.println(".");
            try {
                TreeAnalyzer ta = new TreeAnalyzer();
                for(int i=0;i<eqns.length;++i) {
                    Node node = globalJep.parse(eqns[i]);
                    ta.analyze(node);
                }
                out.println(ta.summary());
            } catch (Exception e) {
                out.println(e.getMessage());
            }
        }

        /**
		 * @param eqns  
         * @param varNames 
		 */
        public void printOutputTimes(SpeedTest st,String eqns[], String varNames[],long[] times) {

        	for(int i=0;i<st.configs.size();++i) {
                out.println(st.configs.get(i).name+"\t"+times[i]);
            }
        }

        public void printFooter(SpeedTest st,long[] totalTimes) {
            out.println();
            out.println("======= Totals =======");
            for(int i=0;i<st.configs.size();++i) {
                EvaluationConfig c = st.configs.get(i);
				out.println(c.name+"\t"+totalTimes[i]+"\t"+c.description());
            }
            this.printRatios(st, totalTimes);
        }
        
        public void printRatios(SpeedTest st, long[] time) {
            out.println();
            out.println("======= Ratios =======");
            out.print("\t");
            for(int i=0;i<time.length;++i) 
                out.print(st.configs.get(i).name+"\t");
            out.println();

            for(int i=0;i<time.length;++i) {
                out.print(st.configs.get(i).name+"\t");
                for(int j=0;j<time.length;++j) {
                    long t1 = time[i];
                    long t2 = time[j];
                    if(t2 != 0) {
                        Double ratio = ((double) t1) / t2;
                        out.printf("%.1f\t", new Object[] { ratio });
                    } else
                        out.print("" + t1 + "/0\t");
                }
                out.println();
            }
        }

    }
    /**
     * Print detailed ratios for each run.
     */
    public static class RatioOutputter extends Outputter {
        @Override
        public void printOutputTimes(SpeedTest st,String eqn, String varNames[],long[] times) {
            super.printOutputTimes(st,eqn,varNames,times);
            printRatios(st,times);
        }
        @Override
        public void printOutputTimes(SpeedTest st,String eqns[], String varNames[],long[] times) {
            super.printOutputTimes(st, eqns, varNames, times);
            printRatios(st,times);
        }
        @Override
        public void printFooter(SpeedTest st, long[] totalTimes) {
            super.printFooter(st, totalTimes);
        }
    }
    /**
     * Print output tab separated.
     * One line of headers
     * One line per test
     * One line of totals
     */
    public static class TabOutputter extends Outputter {
        @Override
        public void printOutputTimes(SpeedTest st,String eqn, String varNames[],long[] times) {
            out.print(eqn);

            for(int i=0;i<st.configs.size();++i) {
                out.print("\t");
                out.print(times[i]);
            }
            out.println();
        }
        @Override
        public void printOutputTimes(SpeedTest st,String eqns[], String varNames[],long[] times) {
            for(int i=0;i<eqns.length;++i) {
                out.print(eqns[i] + ";");
            }

            for(int i=0;i<st.configs.size();++i) {
                out.print("\t");
                out.print(times[i]);
            }
            out.println();
        }
        @Override
        public void printFooter(SpeedTest st, long[] totalTimes) {
            out.print("Total");
            for(int i=0;i<st.configs.size();++i) {
                out.print("\t");
                out.print(totalTimes[i]);
            }
            out.println();
        }
        @Override
        public void printHeader(SpeedTest st) {
            for(int i=0;i<st.configs.size();++i) {
                out.print("\t");
                out.print(st.configs.get(i).name);
            }
            out.println();
        }


    }

    /**
     * Run speed comparison for a single equation.
     * 
     * @param eqn
     *            The equation to test
     * @param varNames
     *            an array of variable names which will be set to random values.
     */
    public void doAll(String eqn, String varNames[]) {

        outputter.printOutputHeader(this,eqn, varNames);

        double varVals[][] = new double[varNames.length][num_vals];

        for (int i = 0; i < varNames.length; ++i) {
            for (int j = 0; j < num_vals; ++j)
                varVals[i][j] = generator.nextDouble();
        }

        long times[] = new long[configs.size()];
        for (int i=0; i < configs.size(); ++i) {
            EvaluationConfig c = configs.get(i);
            times[i] = c.doEval(eqn, varNames, varVals);
            totalTimes[i] += times[i];
        }
        outputter.printOutputTimes(this,eqn, varNames, times);
        //		Runtime rt = Runtime.getRuntime();
        //		out.format("free %d max %d total %d%n",rt.freeMemory(),rt.maxMemory(),rt.totalMemory());
    }

    /**
     * Run speed comparison for a set of equations.
     * @param eqns
     * @param varNames
     */
    public void doAll(String eqns[], String varNames[]) {

        outputter.printOutputHeader(this,eqns, varNames);
        double varVals[][] = new double[varNames.length][num_vals];

        for (int i = 0; i < varNames.length; ++i) {
            for (int j = 0; j < num_vals; ++j)
                varVals[i][j] = generator.nextDouble();
        }

        long times[] = new long[configs.size()];
        for(int i=0;i<configs.size();++i) {
            EvaluationConfig c = configs.get(i);
            times[i] = c.doEval(eqns, varNames, varVals);
            totalTimes[i] += times[i];
        }
        outputter.printOutputTimes(this,eqns, varNames, times);
        //		Runtime rt = Runtime.getRuntime();
        //		out.format("free %d max %d total %d%n",rt.freeMemory(),rt.maxMemory(),rt.totalMemory());
    }
    /** Basic class to set the evaluation context **/


    public static abstract class EvaluationConfig {
        protected String name;
        protected Jep jep;
        /** Factor to reduce the number of iterations for this evaluator. Useful for very slow evaluators. */ 
        protected int div=1;
        protected SpeedTest st;
        /**
         * @param name
         */
        public EvaluationConfig(String name) {
            this.name = name;
        }

        public Object getValue(double d) {
            return d;
        }

        public abstract String description();

        public long doEval(String eqn, String varNames[], double vals[][])
        {
            return doEval(new String[]{eqn}, varNames, vals);
        }

        public long doEval(String eqns[], String varNames[], double vals[][])
        {
            long tdiff = 0;
            try {
            	// add all variables listed in the varNames array
                Variable vars[] = new Variable[varNames.length];
                for (int i=0; i < varNames.length; ++i)
                    vars[i] = jep.addVariable(varNames[i]);

                // create a 2d array of values to set the variables to
                Object bdvals[][] = new Object[vals.length][st.num_vals];
                for (int i=0; i < vals.length; ++i)
                    for (int j=0; j < st.num_vals; ++j)
                        bdvals[i][j] = getValue(vals[i][j]);

                // parse all equations in the eqns array and store the root nodes in an array
                Node nodes[] = new Node[eqns.length];
                for (int i=0; i < eqns.length; ++i)
                    nodes[i] = jep.parse(eqns[i]);

                // get current time
                long t1 = System.currentTimeMillis();
                // perform iterations
                for (int i = 0; i < st.num_itts/div; ++i) {
                    // set each variable value
                    for (int j = 0; j < vars.length; ++j)
                        vars[j].setValue(bdvals[j][i % st.num_vals]);
                    for (int j=0; j < eqns.length; ++j)
                        jep.evaluate(nodes[j]);
                }
                // get current time
                long t2 = System.currentTimeMillis();
                // calc time elapsed
                tdiff = t2 - t1;
            } catch (Exception e) {
                out.println("Error: " + this.name + "\t"+  e.toString());
                //e.printStackTrace();
                tdiff = -1;
            }
            return tdiff * div;
        }
    }

    /**
     * Standard Jep configuration (with FastEvaluator)
     */
    public static class JepConfig extends EvaluationConfig {

        public JepConfig(String name) {
            super(name);
            jep = new Jep();
        }

        @Override
        public String description() {
            return "Standard Jep config";
        }
    }

    
    /**
     * Configuration using the StandardEvaluator
     */
    public static class OldConfig extends EvaluationConfig {

        public OldConfig(String name) {
            super(name);
            jep = new Jep(new StandardEvaluator());
        }

        @Override
        public String description() {
            return "Old Jep configuration with StandardEvaluator";
        }
    }

    /**
     * Configuration using the RealEvaluator
     */
    public static class RealConfig extends EvaluationConfig {

        public RealConfig(String name) {
            super(name);
            jep = new Jep(new RealEvaluator());
        }

        @Override
        public String description() {
            return "Jep with RealEvaluator";
        }
    }

    /**
     * Configuration using the BDConfig
     */
    public static class BDConfig extends EvaluationConfig {
        MathContext mc;

        public BDConfig(String name,MathContext mc) {
            super(name);
            jep = new Jep(new BigDecComponents(mc));
            jep.setComponent(new FastEvaluator());
            div = 10;
            this.mc = mc;
        }

        @Override
        public Object getValue(double d) {
            return new BigDecimal(Double.toString(d),mc);
        }

        @Override
        public String description() {
            return "Jep with BigDecimalComponents and FastEvaluator";
        }

    }

    /**
     * Configuration using the RealEvaluator
     */
    public static class ThreadSafeConfig extends EvaluationConfig {

        public ThreadSafeConfig(String name) {
            super(name);
            jep = new Jep(new ThreadSafeEvaluator());
        }

        @Override
        public String description() {
            return "Jep with ThreadSafeEvaluator";
        }
    }

    /** 
     * Adds a new EvaluationConfig to be be run for comparison.
     * @param config
     */
    public void addConfig(EvaluationConfig config) {
        this.configs.add(config);
        config.st = this;
    }




    static void doCos(SpeedTest st)
    {
        Double[] varVals = new Double[st.num_vals];
        for(int j=0;j<st.num_vals;++j) {
            varVals[j] = new Double(st.generator.nextDouble());
        }

        long t1 = System.currentTimeMillis();
        double x; 
        for(int i=0;i<st.num_itts;++i)
        {
            x = varVals[i%st.num_vals].doubleValue();
            @SuppressWarnings("unused")
            double c = Math.cos(x);
            //double s = Math.sin(x);
            //y = c*c+s*s;
        }
        long t2 = System.currentTimeMillis();
        out.println("Using Java:\t"+(t2-t1));
    }

    static void doHorner(SpeedTest st)
    {
        Double[] varVals = new Double[st.num_vals];
        for(int j=0;j<st.num_vals;++j)
            varVals[j] = new Double(st.generator.nextDouble());

        long t1 = System.currentTimeMillis();
        double x; 
        @SuppressWarnings("unused")
        double y;
        for(int i=0;i<st.num_itts;++i)
        {
            x = varVals[i%st.num_vals].doubleValue();
            y = 1+x*(1+x*(1+x*(1+x*(1+x))));
        }
        long t2 = System.currentTimeMillis();
        out.println("Using Java:\t"+(t2-t1));
    }

    static final double powN(double rIn,short n){
        double r = rIn;
        switch(n){
        case 0: r = 1.0; break;
        case 1: break;
        case 2: r *= r; break;
        case 3: r *= r*r; break;
        case 4: r *= r*r*r; break;
        case 5: r *= r*r*r*r; break;
        case 6: r *= r*r*r*r*r; break;
        case 7: r *= r*r*r*r*r*r; break;
        case 8: r *= r*r*r*r*r*r*r; break;
        default:
        {
            short bitMask = n;
            double evenPower = r;
            double result;
            if ( (bitMask & 1) != 0 )
                result = r;
            else
                result = 1;
            bitMask >>>= 1;
        while ( bitMask != 0 ) {
            evenPower *= evenPower;
            if ( (bitMask & 1) != 0 )
                result *= evenPower;
            bitMask >>>= 1;
        } // end while
        r = result;
        }
        }
        return r;
    } 

    /** Evaluate approximation to log in Java "x^1/1-x^2/2+x^3/3-..." */
    static void doLn(SpeedTest st)
    {
        Double[] varVals = new Double[st.num_vals];
        for(int j=0;j<st.num_vals;++j) {
            varVals[j] = new Double(st.generator.nextDouble());
        }

        long t1 = System.currentTimeMillis();
        double x; 
        for(int i=0;i<st.num_itts;++i)
        {
            x = varVals[i%st.num_vals].doubleValue();
            @SuppressWarnings("unused")
			double res = 0;
            for(int j=1;j<st.nDeriv;++j)
            {
                double val = powN(x,(short) j)/j;
                if(j%2==0) {
                    res -= val;
                } else {
                    res += val;
                }
            }
        }
        long t2 = System.currentTimeMillis();
        out.println("Using Java:\t"+(t2-t1));
    }

    /** Evaluate approximation to log using Horner form in Java "x*(1/1-x*(1/2-x*(1/3-x*(1/4 ...))))" */
    static void doLnHorner(SpeedTest st)
    {
        Double[] varVals = new Double[st.num_vals];

        for(int j=0;j<st.num_vals;++j) {
            varVals[j] = new Double(st.generator.nextDouble());
        }

        long t1 = System.currentTimeMillis();
        double x; 
        for(int i=0;i<st.num_itts;++i)
        {
            x = varVals[i%st.num_vals].doubleValue();
            double res = 1.0 / st.nDeriv;
            for(int j=st.nDeriv;j>=1;--j)
            {
                res = 1.0 /j - x * res;
            }
            res *= x;
        }
        long t2 = System.currentTimeMillis();
        out.println("Using Java:\t"+(t2-t1));
    }


    /**
     * Main method, executes all speed tests.
     * @param args
     */
    public static void main(String args[])	{
        SpeedTest st = new SpeedTest(new Outputter());
        if(args.length == 1)
            st.num_itts = Integer.parseInt(args[0]);

        st.addConfig(new JepConfig("Jep"));
        st.addConfig(new OldConfig("OldJep"));
        st.addConfig(new RealConfig("Real"));
        st.addConfig(new ThreadSafeConfig("ThrdS"));
        st.addConfig(new BDConfig("BD", MC));
        st.init();
        runTests(st);
        st.fini();
    }

    /** 
     * A standard set of tests.
     * @param st
     */
    public static void runTests(SpeedTest st) {
        st.doAll(new String[]{}, new String[]{});
        st.doAll(new String[]{}, new String[]{"x"});
        st.doAll(new String[]{}, new String[]{"x","y","z","w"});
        st.doAll("5", new String[]{});
        st.doAll("x", new String[]{"x"});
        st.doAll("1+x", new String[]{"x"});
        st.doAll("5*x", new String[]{"x"});
        st.doAll("5/x", new String[]{"x"});
        st.doAll("x^2", new String[]{"x"});
        st.doAll("x*x", new String[]{"x"});
        st.doAll("1+x+x^2", new String[]{"x"});
        st.doAll("1+x+x^2+x^3", new String[]{"x"});
        st.doAll("1+x+x^2+x^3+x^4", new String[]{"x"});
        st.doAll("1+x+x^2+x^3+x^4+x^5", new String[]{"x"});
        st.doAll("1+x(1+x(1+x(1+x(1+x))))", new String[]{"x"});
        //doHorner();
        st.doAll("1*2*3+4*5*6+7*8*9", new String[]{});
        st.doAll("x1*x2*x3+x4*x5*x6+x7*x8*x9", new String[]{"x1","x2","x3","x4","x5","x6","x7","x8","x9"});
        
        // Big decimal components do not include trig functions
        st.doAll("cos(x)", new String[]{"x"});
        doCos(st);
        st.doAll("cos(x)^2+sin(x)^2", new String[]{"x"});
        st.doAll(new String[]{"c=cos(x)","s=sin(x)","c*c+s*s"}, new String[]{"x"});
        
        st.doAll("if(x>0.5, 1, 0)", new String[]{"x"});
        st.doAll(new String[]{"y=x*x", "z=y*y", "w=z*z"}, new String[]{"x"});

        String expression = Utils.lnExpression("x", st.nDeriv);
        st.doAll(expression, new String[]{"x"});
        //doLn();

        String expression2 = Utils.hornerExpression("x", st.nDeriv);
        st.doAll(expression2, new String[]{"x"});
        //		doLnHorner();
    }
}
