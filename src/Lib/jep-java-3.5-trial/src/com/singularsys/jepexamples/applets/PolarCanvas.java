/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 6 Aug 2008 - Richard Morris
 */
package com.singularsys.jepexamples.applets;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.parser.Node;

class PolarCanvas extends ParametrisedCanvas {
    private static final long serialVersionUID = 330L;
    /** Expression to evaluate */
    protected Node Rexpression=null;

    public PolarCanvas(Jep jep, Variable t, double min, double max,
            int steps) {
        super(jep, t, min, max, steps);
    }


    public Node getRexpression() {
        return Rexpression;
    }


    public void setRexpression(Node rexpression) {
        if(rexpression==null) return;
        Rexpression = rexpression;
        this.xExpression = rexpression;
        this.yExpression = rexpression;
    }

    private double r;
    @Override
    protected double getXValue(double value) {
        try {
            t.setValue(value);
            Object result = jep.evaluate(xExpression);
            if (result instanceof Double) {
                r = ((Double) result).doubleValue();
                return r* Math.cos(value);
            }
			r = Double.NaN;
			return Double.NaN;
        } catch (JepException e) {
            r = Double.NaN;
            return Double.NaN;
        }

    }
    @Override
    protected double getYValue(double value) {
        return r* Math.sin(value);
    }

    @Override
    public Node getXExpression() {	throw new UnsupportedOperationException();	    }
    @Override
    public Node getYExpression() {	throw new UnsupportedOperationException();	    }
    @Override
    public void setXExpression(Node expr) {throw new UnsupportedOperationException();	    }
    @Override
    public void setYExpression(Node expr) {throw new UnsupportedOperationException();   }
}