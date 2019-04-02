/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jepexamples.applets;

//import java.awt.CheckboxMenuItem;
import java.awt.Color;
import java.awt.Graphics;
import java.awt.event.ItemEvent;
import java.awt.event.ItemListener;

import javax.swing.JCheckBoxMenuItem;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.parser.Node;


/**
 * This class plots parametrised curves specified by two Jep expressions.
 */
public class ParametrisedCanvas extends AbstractCanvas {
    private static final long serialVersionUID = 330L;



    /** Math parser */
    protected Jep jep;

    /** Expression to evaluate */
    protected Node xExpression=null;
    protected Node yExpression=null;

    /** "x" variable */
    protected Variable t;

    double tMin;
    double tMax;
    int tSteps;

    boolean showPoints = false;
    /**
     * @param jep The Jep instance
     * @param t A Variable object holding the parameter 
     * @param min minimum value for parameter
     * @param max maximum value for parameter
     * @param steps number of line segments to draw
     */
    public ParametrisedCanvas(Jep jep, Variable t, double min, double max,
            int steps) {
        super(50,50,0,0);
        this.jep = jep;
        this.t = t;
        tMin = min;
        tMax = max;
        tSteps = steps;
        JCheckBoxMenuItem points = new JCheckBoxMenuItem("Show points",showPoints);
        popup.add(points);
        points.addItemListener(new ItemListener(){
            @Override
			public void itemStateChanged(ItemEvent e) {
                int state = e.getStateChange();
                if(state == ItemEvent.SELECTED)
                    showPoints = true;
                if(state == ItemEvent.DESELECTED)
                    showPoints = false;
            }});
    }


    public Variable getT() {
        return t;
    }


    public void setT(Variable t) {
        this.t = t;
    }


    public Node getXExpression() {
        return xExpression;
    }
    public Node getYExpression() {
        return yExpression;
    }

    public void setXExpression(Node xExpr) {
        if(xExpr!=null) {
            this.xExpression = xExpr;
        }
    }

    public void setYExpression(Node yExpr) {
        if(yExpr != null) {
            this.yExpression = yExpr;
        }
    }

    public double getTMin() {
        return tMin;
    }

    public double getTMax() {
        return tMax;
    }

    public int getTSteps() {
        return tSteps;
    }

    public void setRange(double min, double max,int steps) {
        tMin = min;
        tMax = max;
        tSteps = steps;
        repaint();
    }


    /**
     * @return The value of the function at an x value of the parameter. NaN on errors
     */
    protected double getYValue(double tValue) {
        try {
            t.setValue(tValue);
            Object result = jep.evaluate(yExpression);
            if (result instanceof Double) {
                return ((Double) result).doubleValue();
            }
			return Double.NaN;
        } catch (JepException e) {
            return Double.NaN;
        }
    }

    protected double getXValue(double tValue) {
        try {
            t.setValue(tValue);
            Object result = jep.evaluate(xExpression);
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
            System.out.println("\n"+"t\tx\ty\tscreen x\tscreen y");
        if(xExpression==null || yExpression == null) return;

        boolean firstpoint=true;
        int lastX=0, lastY=0;

        if(g!=null)
            g.setColor(Color.black);

        for (int i = 0; i <= tSteps; ++i)
        {
            double tVal = tMin + ((tMax-tMin)*i)/tSteps;

            double xRelative = getXValue(tVal);
            double yRelative = getYValue(tVal);

            if(Double.isNaN(xRelative) || Double.isNaN(yRelative)) {
                if(g==null)
                    System.out.println(""+tVal+"\t"+xRelative+"\t"+yRelative+"\tNaN\tNaN");
                firstpoint = true;
                continue;
            }

            int xAbsolute = xAbsolute(xRelative);
            int yAbsolute = yAbsolute(yRelative);
            if(g==null)
                System.out.println(""+tVal+"\t"+xRelative+"\t"+yRelative+"\t"+xAbsolute+"\t"+yAbsolute);
            //xAbsolute = clipX(xAbsolute);
            //yAbsolute = clipY(yAbsolute);

            if(showPoints && g!=null)
                g.fillOval(xAbsolute-1, yAbsolute-1, 3, 3);
            if (firstpoint != true) {
                if(lastX <0 && xAbsolute >= dimensions.width) {/* don't draw singularities */}
                else if(lastX >= dimensions.width && xAbsolute < 0) {/* don't draw singularities */}
                else if(lastY <0 && yAbsolute >= dimensions.height) {/* don't draw singularities */}
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
		return tSteps;
	}


}
