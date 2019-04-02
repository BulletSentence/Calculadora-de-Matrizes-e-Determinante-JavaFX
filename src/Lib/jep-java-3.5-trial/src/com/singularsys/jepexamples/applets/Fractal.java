/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
/*
<applet code="org.nfunk.jepexamples.Fractal" width=300 height=320>
<param name=initialExpression value="z*z+c">
</applet>
 */
package com.singularsys.jepexamples.applets;

import java.awt.Color;
import java.awt.Font;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.*;

import javax.swing.JApplet;
import javax.swing.JButton;
import javax.swing.JFrame;
import javax.swing.JTextField;

public class Fractal extends JApplet implements ActionListener {
    private static final long serialVersionUID = -1825231934586941116L;
    private JTextField exprField, itField;
    private JButton button;
    private FractalCanvas complexCanvas;


    /** Initializes the applet Fractal */
    @Override
    public void init () {
        initComponents();
    }


    private void initComponents () {
        GridBagLayout gridbag = new GridBagLayout();
        GridBagConstraints c = new GridBagConstraints();

        setLayout(gridbag);
        c.fill = GridBagConstraints.BOTH;

        // Expression field
        String expr=null;
	try {
	    expr = getParameter("initialExpression");
	} catch (Exception e) {
	}
        if (expr==null) expr = "z*z+c";
        exprField = new JTextField(expr);

        exprField.setBackground (java.awt.Color.white);
        exprField.setName ("exprField");
        exprField.setFont (new Font ("Dialog", 0, 11));
        exprField.setForeground (Color.black);
        exprField.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent evt) {
                exprFieldTextValueChanged (evt);
            }

        }
        );

        c.gridx = 0;
        c.gridy = 0;
        c.weightx = 1;
        gridbag.setConstraints(exprField, c);
        add(exprField);

        // RENDER BUTTON
        c.gridx = 1;
        c.gridy = 0;
        c.weightx = 0.2;
        button = new JButton("Render");
        gridbag.setConstraints(button, c);
        add(button);
        button.addActionListener(this);

        // Iterations field
        itField = new JTextField("20");
        itField.addActionListener (new ActionListener () {
			@Override
			public void actionPerformed(ActionEvent evt) {
                itFieldTextValueChanged(evt);
            }
        }
        );

        c.gridx = 0;
        c.gridy = 1;
        c.weightx = 1;
        gridbag.setConstraints(itField, c);
        add(itField);


        // CANVAS
        c.gridx = 0;
        c.gridy = 2;
        c.gridwidth = 2;
        c.weighty = 1;
        //		button2 = new Button("test");

        complexCanvas = new FractalCanvas(expr, exprField);
        gridbag.setConstraints(complexCanvas, c);
        add(complexCanvas);
    }



    /**
	 * @param evt  
	 */
    void exprFieldTextValueChanged (ActionEvent evt) {
        String newExpressionString = exprField.getText();
        complexCanvas.setExpressionString(newExpressionString);
        //complexCanvas.repaint();
    }

    /**
	 * @param evt  
	 */
    void itFieldTextValueChanged (ActionEvent evt) {
        Integer newIterationsValue = new Integer(itField.getText());
        complexCanvas.setIterations(newIterationsValue.intValue());
        //complexCanvas.repaint();
    }

    @Override
	public void actionPerformed(ActionEvent ae) {
        String str = ae.getActionCommand();
        if (str.equals("Render")) {
            String newExpressionString = exprField.getText();
            complexCanvas.setExpressionString(newExpressionString);
            complexCanvas.repaint();
        }
    }
    
    public static void main(String argv[]) {
    	JFrame f = new JFrame();
    	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
    	f.setSize(600, 400);
    	Fractal pp = new Fractal();
    	f.add(pp);
    	pp.init();
//        f.pack();
    	f.setVisible(true);
    }

}
