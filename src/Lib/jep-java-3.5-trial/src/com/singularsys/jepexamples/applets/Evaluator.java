/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
/*
HTML code for running the applet:
<applet code="org/nfunk/jepexamples/Evaluator.class" width=400 height=200>
</applet>
 */
package com.singularsys.jepexamples.applets;

import java.applet.Applet;
import java.awt.*;
import java.awt.event.*;

import com.singularsys.jep.*;
import com.singularsys.jep.standard.StandardVariableTable;

/**
 * This applet is an simple example for how Jep can be used to evaluate
 * expressions. It also displays the different options, and the effects of their
 * settings.
 */
public class Evaluator extends Applet {

    private static final long serialVersionUID = 4592714713689369505L;

    /** Initial expression */
    private String initialExpression = "x";

    /** Parser */
    private Jep jep;

    /** Current xValue */
    private double xValue;

    /* GUI components */
    private TextField exprField, xField;
    private TextArea errorTextArea;
    private Label resultLabel;
    private Checkbox implicitCheckbox;

    /**
     * This method is called if the applet is run as an standalone program. It
     * creates a frame for the applet and adds the applet to that frame.
     */
    public static void main(String args[]) {
	Evaluator a = new Evaluator();
	a.init();
	a.start();

	Frame f = new Frame("Evaluator");
	f.add("Center", a);
	f.setSize(400, 200);
	f.addWindowListener(new WindowAdapter() {
	    @Override
	    public void windowClosing(WindowEvent e) {
		System.exit(0);
	    }
	});

	// f.show();
	f.setVisible(true);
    }

    /**
     * The initialization function of the applet. It adds all the components
     * such as text fields and also creates the Jep object
     */
    @Override
    public void init() {
	// initialize value for x
	xValue = 10;

	// add the interface components
	addGUIComponents();

	// Set up the parser (more initialization in parseExpression())
	jep = new Jep();

	// simulate changed options to initialize output
	optionsChanged();
    }

    /**
     * Creates and adds the necessary GUI components.
     */
    private void addGUIComponents() {
	setBackground(Color.white);

	GridBagLayout gridbag = new GridBagLayout();
	GridBagConstraints c = new GridBagConstraints();
	setLayout(gridbag);

	// Expression
	c.fill = GridBagConstraints.HORIZONTAL;
	c.weightx = 0.0;
	Label exprFieldp = new Label("Expression: ", Label.RIGHT);
	gridbag.setConstraints(exprFieldp, c);
	add(exprFieldp);

	c.weightx = 0.8;
	exprField = new TextField(initialExpression, 27);
	gridbag.setConstraints(exprField, c);
	add(exprField);

	// x
	c.weightx = 0.0;
	Label xFieldp = new Label("x: ", Label.RIGHT);
	gridbag.setConstraints(xFieldp, c);
	add(xFieldp);

	c.weightx = 0.2;
	c.gridwidth = GridBagConstraints.REMAINDER;
	xField = new TextField("" + xValue, 4);
	gridbag.setConstraints(xField, c);
	add(xField);

	// Result
	c.weightx = 0.0;
	c.gridwidth = 1;
	Label resultLabelText = new Label("Result: ", Label.RIGHT);
	gridbag.setConstraints(resultLabelText, c);
	add(resultLabelText);

	c.weightx = 1.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	resultLabel = new Label("", Label.LEFT);
	gridbag.setConstraints(resultLabel, c);
	add(resultLabel);

	// Options
	c.weightx = 0.0;
	c.gridwidth = 1;
	Label optionsLabelText = new Label("Options: ", Label.RIGHT);
	gridbag.setConstraints(optionsLabelText, c);
	add(optionsLabelText);

	c.weightx = 1.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	implicitCheckbox = new Checkbox("Implicit multiplication", true);
	gridbag.setConstraints(implicitCheckbox, c);
	add(implicitCheckbox);

	// Errors
	c.weightx = 0.0;
	c.gridwidth = 1;
	c.anchor = GridBagConstraints.NORTH;
	Label errorLabel = new Label("Errors: ", Label.RIGHT);
	gridbag.setConstraints(errorLabel, c);
	add(errorLabel);

	c.fill = GridBagConstraints.BOTH;
	c.weightx = 1.0;
	c.weighty = 1.0;
	c.gridwidth = GridBagConstraints.REMAINDER;
	errorTextArea = new TextArea("");
	errorTextArea.setEditable(false);
	errorTextArea.setBackground(Color.white);
	gridbag.setConstraints(errorTextArea, c);
	add(errorTextArea);

	// Set up listeners
	exprField.addTextListener(new TextListener() {
	    @Override
	    public void textValueChanged(TextEvent evt) {
		exprFieldTextValueChanged();
	    }
	});

	xField.addTextListener(new TextListener() {
	    @Override
	    public void textValueChanged(TextEvent evt) {
		xFieldTextValueChanged();
	    }
	});

	implicitCheckbox.addItemListener(new ItemListener() {
	    @Override
	    public void itemStateChanged(ItemEvent evt) {
		optionsChanged();
	    }
	});

    }

    /**
     * Parses the current expression in the exprField. This method also
     * re-initializes the contents of the symbol and function tables. This is
     * necessary because the "allow undeclared variables" option adds variables
     * from expressions to the symbol table.
     */
    private boolean parseExpression() {
	// reload the standard variable table (with pi, e, and i)
	jep.setComponent(new StandardVariableTable(jep.getVariableFactory()));
	// add the x variable
	// try parsing
	try {
	    jep.addVariable("x", xValue);
	    jep.parse(exprField.getText());
	    exprField.setBackground(new Color(255, 255, 255));
	    return true;
	} catch (ParseException e) {
	    exprField.setBackground(new Color(255, 220, 220));
	    resultLabel.setText("");
	    errorTextArea.setText("Error while parsing:\n" + e.getMessage());
	} catch (JepException e) {
	    exprField.setBackground(new Color(255, 220, 220));
	    resultLabel.setText("");
	    errorTextArea.setText("Error while parsing:\n" + e.getMessage());
	}
	return false;
    }

    /**
     * Whenever the expression is changed, this method is called. The expression
     * is parsed, and the updateResult() method invoked.
     */
    void exprFieldTextValueChanged() {
	if (parseExpression())
	    updateResult();
    }

    /**
     * Every time the value in the x field is changed, this method is called. It
     * takes the value from the field as a double, and sets the value of x in
     * the parser.
     */
    void xFieldTextValueChanged() {

	try {
	    xValue = Double.valueOf(xField.getText()).doubleValue();
	    jep.addVariable("x", xValue);
	} catch (NumberFormatException e) {
	    System.out.println("Invalid format in xField " + xField.getText());
	    xValue = 0;
	} catch (JepException e) {
	    e.printStackTrace();
	}

	if (parseExpression())
	    updateResult();
    }

    /**
     * Every time one of the options is changed, this method is called. The
     * parser settings are adjusted to the GUI settings, the expression is
     * parsed again, and the results updated.
     */
    void optionsChanged() {
	jep.setImplicitMul(implicitCheckbox.getState());
	if (parseExpression())
	    updateResult();
    }

    /**
     * This method uses Jep's getValueAsObject() method to obtain the current
     * value of the expression entered.
     */
    private void updateResult() {
	Object result = null;

	// Get the value
	try {
	    result = jep.evaluate();

	    // Is the result ok?
	    if (result != null) {
		resultLabel.setText(result.toString());
	    } else {
		resultLabel.setText("null");
	    }
	    errorTextArea.setText("");
	} catch (EvaluationException e) {
	    // Clear the result and print the error message
	    resultLabel.setText("");
	    errorTextArea.setText("Error while evaluating:\n" + e.getMessage());
	}
    }
}
