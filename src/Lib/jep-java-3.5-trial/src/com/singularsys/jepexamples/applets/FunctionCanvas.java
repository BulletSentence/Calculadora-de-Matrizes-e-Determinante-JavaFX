/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jepexamples.applets;


import java.awt.Color;
import java.awt.Graphics;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.parser.Node;


/**
 * This class plots a graph using the Jep API.
 */
public class FunctionCanvas extends AbstractCanvas {
    private static final long serialVersionUID = 330L;



    /** Math parser */
    protected Jep jep;

    /** Expression to evaluate */
    protected Node expression=null;

    /** "x" variable */
    protected Variable x;

    /**
     * Constructor
     */
    public FunctionCanvas(Jep jep,Variable x) {
        super(50,50,0,0);
        this.jep = jep; 
        this.x = x;
    }


    public Node getExpression() {
        return expression;
    }

    public void setExpression(Node expression) {
        if(expression!=null) {
            this.expression = expression;
        }
    }

    /**
     * @return The value of the function at an x value of the parameter. NaN on errors
     */
    private double getYValue(double xValue) {
        try {
            x.setValue(xValue);
            Object result = jep.evaluate(expression);
            if (result instanceof Double) {
                return ((Double) result).doubleValue();
            }
			return Double.NaN;
        } catch (JepException e) {
            return Double.NaN;
        }
    }


    /**
     * Paints the graph of the function.
     * A null argument cause the values to be dumped to stdout.
     */
    @Override
    protected void paintCurve(Graphics g) {
        if(g==null)
            System.out.println("\n"+"x\ty\txScreen\tyScreen");
        if(expression==null) return;

        boolean firstpoint=true;
        int lastX=0, lastY=0;

        if(g!=null)
            g.setColor(Color.black);

        for (int xAbsolute = 0; xAbsolute <= (dimensions.width-1); xAbsolute++)
        {
            double xRelative = xRelative(xAbsolute);
            double yRelative = getYValue(xRelative);

            if(Double.isNaN(yRelative)) {
                if(g==null)
                    System.out.println(""+xRelative+"\t"+yRelative+"\t"+xAbsolute+"\tNaN");
                firstpoint = true;
                continue;
            }

            int yAbsolute = yAbsolute(yRelative);

            if(g==null)
                System.out.println(""+xRelative+"\t"+yRelative+"\t"+xAbsolute+"\t"+yAbsolute);
            yAbsolute = clipY(yAbsolute);

            if (firstpoint != true) {
                if(lastY <0 && yAbsolute >= dimensions.height) {/* don't draw singularities */}
                else if(lastY >= dimensions.height && yAbsolute < 0) {/* don't draw singularities */}
                else if(g!=null)
                    g.drawLine(lastX, lastY, xAbsolute, yAbsolute);
            }
            else
                firstpoint = false;

            lastX = xAbsolute;
            lastY = yAbsolute;
        }
    }

    @Override
    public void rescaled() {
        this.repaint();
    }


    /** Dump the coordinates on the standard output */
    @Override
    public void dump() {
        paintCurve(null);
    }


	@Override
	protected long getNumPts() {
		return (long) dimensions.getWidth();
	}


}
