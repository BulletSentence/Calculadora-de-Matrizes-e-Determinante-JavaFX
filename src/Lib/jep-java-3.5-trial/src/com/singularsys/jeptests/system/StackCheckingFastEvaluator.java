/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 

package com.singularsys.jeptests.system;

import java.io.IOException;
import java.io.ObjectInputStream;
import java.text.MessageFormat;
import java.util.Stack;

import com.singularsys.jep.Evaluator;
import com.singularsys.jep.Jep;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.JepComponent;
import com.singularsys.jep.JepException;
import com.singularsys.jep.JepMessages;
import com.singularsys.jep.ParserVisitor;
import com.singularsys.jep.PostfixMathCommandI;
import com.singularsys.jep.Variable;
import com.singularsys.jep.functions.BinaryFunction;
import com.singularsys.jep.functions.CallbackEvaluationI;
import com.singularsys.jep.functions.NaryBinaryFunction;
import com.singularsys.jep.functions.NaryFunction;
import com.singularsys.jep.functions.NullaryFunction;
import com.singularsys.jep.functions.UnaryFunction;
import com.singularsys.jep.parser.ASTConstant;
import com.singularsys.jep.parser.ASTFunNode;
import com.singularsys.jep.parser.ASTOpNode;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.parser.JccParserTreeConstants;

public class StackCheckingFastEvaluator implements Evaluator, ParserVisitor {
    private transient Stack<Object> stack = new Stack<>();

    /** Whether null values for variables are trapped 
     * @serial
     **/ 
    protected boolean trapUnsetValues=true;
    protected boolean trapNullValues=true;
    protected boolean trapNaN=false;
    protected boolean trapInfinity=false;


    @Override
	public void init(Jep jep) { /* do nothing */ }

    @Override
	public Object eval(Node node) throws EvaluationException {
        Object res=null;
        //res = node.jjtAccept(this, null);
        res = nodeAccept(node);
        return res;
    }

    @Override
	public Object evaluate(Node node) throws EvaluationException {
        stack.clear();
        Object res=null;
        // attempt to evaluate the expression
        //res = node.jjtAccept(this, null);
        res = nodeAccept(node);
        // Stack should only have a single item on it
        if (stack.size() != 0) 
            throw new EvaluationException(JepMessages.getString("standard.FastEvaluator.StackCorrupted")); //$NON-NLS-1$
        // Stack only has a single item, so pop it and return it
        return res;
    }

    protected Object nodeAccept(Node node) throws EvaluationException {
        switch(node.getId())
        {
        case JccParserTreeConstants.JJTOPNODE: 
            return visitFun(node);
        case JccParserTreeConstants.JJTVARNODE: 
            return visitVar(node);
        case JccParserTreeConstants.JJTFUNNODE: 
            return visitFun(node);
        case JccParserTreeConstants.JJTCONSTANT: 
            return visitConstant(node);
        }
        try {
            return node.jjtAccept(this, null);
        } catch(EvaluationException e) { 
            throw e; 
        } catch (JepException e) {
            throw new EvaluationException(e);
        }
    }
    /*	    if(node instanceof ASTConstant) 
		visit((ASTConstant)node,null);
	    else if(node instanceof ASTVarNode)
		visit((ASTVarNode)node,null);
	    else visitFun(node);
	}
/**/
    @Override
	public Object visit(ASTConstant node, Object data) throws EvaluationException {
        return visitConstant(node);
    }
    public Object visitConstant(Node node) throws EvaluationException {
        Object o = node.getValue();
        if(this.trapNullValues && o == null) {
            throw new EvaluationException(JepMessages.getString("standard.FastEvaluator.NullConstantValue")); //$NON-NLS-1$
        }
        if(this.trapNaN) {
            if (  (o instanceof Double && ((Double) o).isNaN())
                    ||(o instanceof Float && ((Float) o).isNaN()) )
                throw new EvaluationException(JepMessages.getString("standard.FastEvaluator.NaNConstantValue")); //$NON-NLS-1$
        }
        if(this.trapInfinity) {
            if (  (o instanceof Double && ((Double) o).isInfinite())
                    ||(o instanceof Float && ((Float) o).isInfinite())
            )
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.InfiniteConstantValue"),o.toString())); //$NON-NLS-1$
        }
        return o;
    }

    @Override
	public Object visit(ASTVarNode node, Object data) throws EvaluationException {
        return visitVar(node);
    }

    public Object visitVar(Node node) throws EvaluationException {

        Variable var = node.getVar();
        assert var!=null;

        if(this.trapUnsetValues && !var.hasValidValue()) {
            String message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.CouldNotEvaluateVariableNoValueSet"),var.getName()); //$NON-NLS-1$
            throw new EvaluationException(message);
        }

        // get the variable value
        Object temp = var.getValue();

        if (trapNullValues && temp == null) {
            String message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.CouldNotEvaluateVariableNullValue"),var.getName()); //$NON-NLS-1$
            throw new EvaluationException(message);
        }
        if(this.trapNaN) {
            if (  (temp instanceof Double && ((Double) temp).isNaN())
                    ||(temp instanceof Float && ((Float) temp).isNaN()) )
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.NaNValueForVariable"),var.getName())); //$NON-NLS-1$
        }
        if(this.trapInfinity) {
            if (  (temp instanceof Double && ((Double) temp).isInfinite())
                    ||(temp instanceof Float && ((Float) temp).isInfinite())
            )
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.InfiniteValueForVariable"),temp.toString(),var.getName())); //$NON-NLS-1$
        }
        // all is fine
        return temp;
    }

    @Override
	public Object visit(ASTFunNode node, Object data) throws EvaluationException {
        return visitFun(node);
    }

    @Override
	public Object visit(ASTOpNode node, Object data) throws EvaluationException {
        return visitFun(node);
    }

    /**
     * Visits a function/operator node. This is the most visited method for most
     * expression evaluations. Keeping it fast is important.
     * @param node
     * @throws EvaluationException
     */
    protected Object visitFun(Node node) throws EvaluationException {
	int cur_stack_size = stack.size();
        PostfixMathCommandI pfmc = node.getPFMC();
        int nchild = node.jjtGetNumChildren();
        Object res;

        if (pfmc == null)
            throw new EvaluationException(
                    MessageFormat.format(JepMessages.getString("standard.FastEvaluator.NoFunctionClass"),node.getName())); //$NON-NLS-1$

        if (pfmc instanceof CallbackEvaluationI) {
            res = ((CallbackEvaluationI) pfmc).evaluate(node, this);
        }
        else if(pfmc instanceof UnaryFunction) {
            if(nchild != 1) throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsExpected"),node.getName(),nchild,1)); //$NON-NLS-1$
            Object cval = nodeAccept(node.jjtGetChild(0));
            res = ((UnaryFunction) pfmc).eval(cval);
        }
        else if(pfmc instanceof BinaryFunction) {
            if(nchild != 2) throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsExpected"),node.getName(),nchild,2)); //$NON-NLS-1$
            Object lval = nodeAccept(node.jjtGetChild(0));
            Object rval = nodeAccept(node.jjtGetChild(1));
            res = ((BinaryFunction) pfmc).eval(lval,rval);
        }
        else if(pfmc instanceof NaryBinaryFunction) {
            switch(nchild) {
            case 0:
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsNonZero"),node.getName(),nchild)); //$NON-NLS-1$
            case 1:
                res = nodeAccept(node.jjtGetChild(0));
                break;
            case 2:
                Object lval = nodeAccept(node.jjtGetChild(0));
                Object rval = nodeAccept(node.jjtGetChild(1));
                res = ((NaryBinaryFunction) pfmc).eval(lval,rval);
                break;
            default:
            	// get the number of children
            	Object[] cvals = new Object[nchild];
            	// loop through each child
            	for (int i=0; i<nchild; ++i) {
            		Node child = node.jjtGetChild(i);
            		cvals[i] = nodeAccept(child);
            	}
            	pfmc.setCurNumberOfParameters(nchild);
            	res = ((NaryBinaryFunction) pfmc).eval(cvals);
            }
        }
        else if(pfmc instanceof NaryFunction) {
            if (!pfmc.checkNumberOfParameters(nchild)) {
                String message; 
                if(pfmc.getNumberOfParameters() >-1 ) 
                        message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsExpected"),node.getName(),nchild,pfmc.getNumberOfParameters()); //$NON-NLS-1$
                else
                    message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArguments"),node.getName(),nchild); //$NON-NLS-1$
                throw new EvaluationException(message);
            }
            Object[] args = new Object[nchild];
            for (int i=0; i<nchild; ++i) {
                Node child = node.jjtGetChild(i);

                //nodeAccept(child);
                args[i] = nodeAccept(child);
            }
            pfmc.setCurNumberOfParameters(nchild);
            res = ((NaryFunction) pfmc).eval(args);
        }
        else if(pfmc instanceof NullaryFunction) {
            if(nchild != 0)
            	throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsExpected"),node.getName(),nchild,0)); //$NON-NLS-1$
        	res = ((NullaryFunction) pfmc).eval();
        }
        else {
            // check whether the number of parameters is correct
            if (!pfmc.checkNumberOfParameters(nchild)) {
                String message; 
                if(pfmc.getNumberOfParameters() >-1 ) 
                        message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArgumentsExpected"),node.getName(),nchild,pfmc.getNumberOfParameters()); //$NON-NLS-1$
                else
                    message = MessageFormat.format(JepMessages.getString("standard.FastEvaluator.IncorrectNumberOfArguments"),node.getName(),nchild); //$NON-NLS-1$
                throw new EvaluationException(message);
            }

            // evaluate all the children
            Object cval=null;
            for (int i=0; i<nchild; ++i) {
                Node child = node.jjtGetChild(i);

                cval = nodeAccept(child);
                stack.push(cval);
            }
            // set the number of parameters for this node
            pfmc.setCurNumberOfParameters(nchild);
            // run the function on the stack
            pfmc.run(stack);
            res = stack.pop();
        }

        if (trapNullValues && res == null) {
            throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.NullValueForFunction"),node.getName())); //$NON-NLS-1$
        }
        if(this.trapNaN) {
            if (  (res instanceof Double && ((Double) res).isNaN())
                    ||(res instanceof Float && ((Float) res).isNaN()) )
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.NaNValueForFunction"),node.getName())); //$NON-NLS-1$
        }
        if(this.trapInfinity) {
            if (  (res instanceof Double && ((Double) res).isInfinite())
                    ||(res instanceof Float && ((Float) res).isInfinite())
            )
                throw new EvaluationException(MessageFormat.format(JepMessages.getString("standard.FastEvaluator.InfiniteValueForFunction"),node.getName(),res.toString())); //$NON-NLS-1$
        }
        
        if(stack.size() != cur_stack_size)
            throw new EvaluationException("Stack error in evaluating function "+node.getName());
        return res;
        
    }

    /**
     * Whether variables with unset values are trapped.
     * @return
     * @since 3.5
     */
    public boolean isTrapUnsetValues() {
		return trapUnsetValues;
	}

    /**
     * Sets whether unset variable values are trapped. This flag is set by 
     * when the {@link Variable#setValidValue(boolean)} or cleared
     * using {@link com.singularsys.jep.VariableTable#clearValues()}.
     * @param trapUnsetValues true to trap unset values, false to ignore them
     * @since 3.5
     */
	public void setTrapUnsetValues(boolean trapUnsetValues) {
		this.trapUnsetValues = trapUnsetValues;
	}


    /**
     * Whether null values for variables are trapped.
     * @return the status if the trap null values flag.
     */
    public boolean isTrapNullValues() {
        return trapNullValues;
    }

    /**
     * Sets whether null values for variables are trapped.
     * If set (the default) then an EvaluationException is 
     * thrown for null values of variables.
     * If not set then null values are passed to PostfixMathCommands
     * who will need to test for null values.
     * @param trapNullValues
     */
    public void setTrapNullValues(boolean trapNullValues) {
        this.trapNullValues = trapNullValues;
    }

    public boolean isTrapNaN() {
        return trapNaN;
    }

    public void setTrapNaN(boolean trapNaN) {
        this.trapNaN = trapNaN;
    }

    public boolean isTrapInfinity() {
        return trapInfinity;
    }

    public void setTrapInfinity(boolean trapInfinity) {
        this.trapInfinity = trapInfinity;
    }

    /**
     * @return an new FastEvaluator
     */
    @Override
	public JepComponent getLightWeightInstance() {
        StackCheckingFastEvaluator se = new StackCheckingFastEvaluator();
        se.trapNullValues = this.trapNullValues;
        se.trapNaN = this.trapNaN;
        se.trapInfinity = this.trapInfinity;
        return se;
    }

    private static final long serialVersionUID = 300L;
    private void readObject(ObjectInputStream in) throws IOException, ClassNotFoundException
    {
        in.defaultReadObject();
        stack = new Stack<>();
    }

}
