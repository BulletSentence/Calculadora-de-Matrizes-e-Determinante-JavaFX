/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 23 May 2008 - Richard Morris
 */
package com.singularsys.jepexamples.diagnostics;

import java.util.List;
import java.util.Stack;
import java.util.Vector;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Evaluator;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.Operator;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.functions.CallbackEvaluationI;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.Complex;

/**
 * Class to examine the results types of the various operators
 * and function.
 * Can be called with 1 argument to just test a function or operator with that name
 * <code>java ExamineResultTypes sin</code>
 * <code>java ExamineResultTypes +</code>
 * @author Richard Morris
 *
 */
public class ExamineResultTypes {
    Jep jep;
    Stack<Object> stack = new Stack<>();
    TypeCreator[] allTypes=null;
    
    abstract static class TypeCreator
    {
        abstract public Object getSampleType();
        public String getTypeDescriptor() { return getSampleType().getClass().getSimpleName(); }
    }
    static class IntegerTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Integer.valueOf(123); }
    }

    static class LongTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Long.valueOf(123); }
    }

    public static class ShortTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Short.valueOf((short)123); }
    }

    static class DoubleTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Double.valueOf(1.23); }
    }

    static class ComplexTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return new Complex(1.23,4.56); }
    }


    static class FloatTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Float.valueOf(1.23f); }
    }

    static class StringTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return String.valueOf("abc"); }
    }
    static class BooleanTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Boolean.TRUE; }
    }
    static class VectorTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {
            Vector<Object> res = new Vector<>();
            res.add(new Double(1));
            res.add(new Double(2));
            res.add(new Double(3));
            return res;
        }
    }

    static class NegativeDoubleTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Double.valueOf(-1.23); }
        @Override
        public String getTypeDescriptor() {
            return "Neg-Double";
        }
    }

    static class NaNDoubleTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Double.NaN; }
        @Override
        public String getTypeDescriptor() {
            return "NaN";
        }
    }

    static class InfityDoubleTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Double.MAX_VALUE; }
        @Override
        public String getTypeDescriptor() {
            return "+Inf";
        }
    }

    static class ZeroDoubleTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return Double.valueOf(0.0); }
        @Override
        public String getTypeDescriptor() {
            return "0.0D";
        }
    }

    static class NullTypeCreator extends TypeCreator {
        @Override
        public Object getSampleType() {  return null; }
        @Override
        public String getTypeDescriptor() {
            return "null";
        }
    }

    /**
     * 
     * @param jep
     * @param types
     */
    public ExamineResultTypes(Jep jep,TypeCreator[] types) {
        this.jep = jep;
        this.allTypes = types;
    }


    /**
     * Tests return types of all operators and functions.
     */
    private void examineOperatorsAndFunctions() {
        OperatorTableI ot = jep.getOperatorTable();
        for(Operator op : ot.getOperators())
        {
            examineTypes(op.getName(),op.getPFMC());
            System.out.println();
        }
//        FunctionTable ft = jep.getFunctionTable();
//        List<String> list = new ArrayList<String>(ft.keySet());
//        Collections.sort(list);
//        for(String key:list)
//        {
//            examineTypes(key,ft.getFunction(key));
//            System.out.println();
//        }
    }

    void examineOperatorOrFunction(String name) {
        OperatorTableI ot = jep.getOperatorTable();

        List<Operator> ops = ot.getOperatorsBySymbol(name);
        if(ops.size()>0) {
            for(Operator op2:ops) {
                examineTypes(op2.getName(),op2.getPFMC());
                System.out.println();
            }
        }
        PostfixMathCommandI pfmc = jep.getFunctionTable().getFunction(name);
        if(pfmc!=null) {
            examineTypes(name,pfmc);
            System.out.println();
            
        }
    }
    /**
     * Tests return types for a specific PostfixMathCommandI
     * will test for 0,1,2 and 3 parameters
     */
    public void examineTypes(String name, PostfixMathCommandI pfmc) {
        if(pfmc==null) {
            System.out.println(name+"\t"+"NO PFMC DEFINED");
            return;
        }
        int nc = pfmc.getNumberOfParameters();
        if(nc==-1) {
            for(int i=0;i<3;++i){
                if(pfmc.checkNumberOfParameters(i)) {
                    examineTypes(name,pfmc,i);
                }
            }
        }
        else
            examineTypes(name,pfmc,nc);
    }


    /**
     * Tests return types for a specific PostfixMathCommandI with a specific number of parameters
     */
    public void examineTypes(String name,PostfixMathCommandI pfmc, int n) {
        TypeCreator[] types = new TypeCreator[n];
        examineTypes(name,pfmc,n,n,types);
    }

    private void examineTypes(String name,PostfixMathCommandI pfmc, int i, int n,TypeCreator[] types) {
        if(i==0)
            runPfmc(name,pfmc,n,types);
        else
        {
            for(TypeCreator ot:allTypes) {
                types[i-1] = ot;
                examineTypes(name,pfmc,i-1,n,types);
            }
        }

    }

    private void runPfmc(String name,PostfixMathCommandI pfmc,int n,TypeCreator[] types) {
        System.out.print(name+"("+n+")\t");
        stack.clear();
        for(int i=0;i<n;++i)
        {
            Object value = types[i].getSampleType();
            System.out.print(types[i].getTypeDescriptor());
            System.out.print('\t');
            stack.push(value);
        }
        System.out.print("->\t");
        try {
            pfmc.setCurNumberOfParameters(n);
            Object res=null;
            if(pfmc instanceof CallbackEvaluationI) {
                res = runCallBack(name,(CallbackEvaluationI) pfmc,n);
            }
            else
            {
                pfmc.run(stack);
                res = stack.pop();
                if(!stack.isEmpty())
                    throw new EvaluationException("Stack not empty");
            }
            if(res!=null)
            {
                System.out.print(res.getClass().getSimpleName());
                System.out.println("\t"+res.toString());
            }
            else
                System.out.println("null\tnull");
        } catch (EvaluationException e) {
            System.out.println("EvaluationException: "+e.getMessage());
        } catch (Exception e) {
            System.out.println(e.toString());
        }

    }

    Evaluator ev = new Evaluator() {
        private static final long serialVersionUID = 1L;

        @Override
		public Object eval(Node node) throws EvaluationException {
            return node.getValue();
        }

        @Override
		public Object evaluate(Node node) throws EvaluationException {
            return node.getValue();
        }

        @Override
		public JepComponent getLightWeightInstance() {
            return null;
        }

        @SuppressWarnings("hiding")
		@Override
		public void init(Jep jep) { /* empty */	}
    };

    private Object runCallBack(String name,CallbackEvaluationI pfmc, int n) throws ParseException, EvaluationException {
        Object[] vals = new Object[n];
        Node[] nodes = new Node[n];
        for(int i=n-1;i>=0;--i)
        {
            Object val = stack.pop();
            vals[i] = val;
            nodes[i] = jep.getNodeFactory().buildConstantNode(val);
        }
        Node node = jep.getNodeFactory().buildFunctionNode(name, (PostfixMathCommandI) pfmc, nodes);
        Object res = pfmc.evaluate(node, ev);
        return res;
    }

    /**
     * @param args
     */
    public static void main(String[] args) {
        Jep jep=new Jep();
        TypeCreator[] types = new TypeCreator[]{
                new IntegerTypeCreator(),
                new DoubleTypeCreator(),
                new VectorTypeCreator(),
                new NaNDoubleTypeCreator(),
                new InfityDoubleTypeCreator(),
                new NullTypeCreator(),
                new NegativeDoubleTypeCreator(),
                new ComplexTypeCreator(),
        };

        ExamineResultTypes ert = new ExamineResultTypes(jep,types);
        
        if(args.length>0)
            ert.examineOperatorOrFunction(args[0]);
        else
            ert.examineOperatorsAndFunctions();
    }

}
