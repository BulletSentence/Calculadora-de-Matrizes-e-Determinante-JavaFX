/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
/*
HTML code for applet:
<applet code="org.nfunk.jepexamples.FunctionPlotter" width=300 height=320>
<param name=initialExpression value="100 sin(x/3) cos(x/70)">
</applet>
 */

package com.singularsys.jepexamples.applets;

import java.awt.BorderLayout;
import java.awt.Color;
import java.awt.Dimension;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.net.URL;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JList;
import javax.swing.JScrollPane;
import javax.swing.JTextField;
import javax.swing.ListSelectionModel;
import javax.swing.event.ListSelectionEvent;
import javax.swing.event.ListSelectionListener;

import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.Variable;
import com.singularsys.jep.parser.Node;

/**
 * This applet is a demonstration of the possible applications of the Jep
 * mathematical expression parser.
 * <p>
 * The FunctionPlotter class arranges the text field and FunctionCanvas classes
 * and requests a repainting of the graph when the expression in the text field
 * changes. All plotting (and interaction with the Jep API) is performed in
 * FunctionCanvas class.
 */
public class FunctionPlotter extends JApplet implements ListSelectionListener {
    private static final long serialVersionUID = 330L;

    String[][] equations = new String[][] { { "Straight Line", "3 x-1" }, { "x", "x" }, { "x^2", "x^2" },
	    { "x^3", "x^3" }, { "x^4", "x^4" }, { "sqrt(x)", "sqrt(x)" }, { "pow(x,1/3)", "pow(x,1/3)" },
	    { "1/x", "1/x" }, { "1/x^2", "1/x^2" }, { "sin(x)", "sin(x)" }, { "cos(x)", "cos(x)" },
	    { "tan(x)", "tan(x)" }, { "sec(x)", "sec(x)" }, { "cosec(x)", "cosec(x)" }, { "cot(x)", "cot(x)" },
	    { "asin(x)", "asin(x)" }, { "acos(x)", "acos(x)" }, { "atan(x)", "atan(x)" },

	    { "sinh(x)", "sinh(x)" }, { "cosh(x)", "cosh(x)" }, { "tanh(x)", "tanh(x)" }, { "asinh(x)", "asinh(x)" },
	    { "acosh(x)", "acosh(x)" }, { "atanh(x)", "atanh(x)" },

	    { "log(x)", "log(x)" }, { "ln(x)", "ln(x)" }, { "lg(x)", "lg(x)" }, { "exp(x)", "exp(x)" },
	    { "exp(-x)", "exp(-x)" }, { "exp(-x^2)", "exp(-x^2)" },

	    { "sin(1/x)", "sin(1/x)" }, { "x sin(1/x)", "x sin(1/x)" }, { "x^2 sin(1/x)", "x^2 sin(1/x)" },

	    { "abs(x)", "abs(x)" }, { "round(x)", "round(x)" }, { "rint(x)", "rint(x)" }, { "floor(x)", "floor(x)" },
	    { "ceil(x)", "ceil(x)" }, { "signum(x)", "signum(x)" }, { "mod(x,1)", "mod(x,1)" },

	    { "Sawtooth", "x % 1 + if(x<0,1,0)" }, { "Square wave", "if(x % 2 + if(x<0,2,0)>1,1,0)" },
	    { "Square wave approx",
		    "4 / pi ( sin(2 pi x) + 1/3 sin(6 pi x) + 1/5 sin(10 pi x) + 1/7 sin(14 pi x) + 1/9 sin(18 pi x) )" },
	    { "Triangle wave", "4*abs((x+0.5)/2-rint((x+0.5)/2))-1" }, { "Triangle wave approx",
		    "8 / pi^2 (sin(pi x)-1/9 sin(3 pi x)+1/25 sin(5 pi x) - 1/49 sin(7 pi x) + 1/81 sin(9 pi x) )" }, };

    /** The expression field */
    JTextField exprField;

    /** List of equations */
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> list = new JList<>(listModel);

    Map<String, String> map = new HashMap<>();

    /** The canvas for plotting the graph */
    private FunctionCanvas graphCanvas;

    protected Jep jep;

    /**
     * Initializes the applet FunctionPlotter
     */
    @Override
    public void init() {
	try {
	    jep = initJep();
	    initComponents(jep);

	} catch (JepException e) {
	    e.printStackTrace();
	}
    }

    /**
     * Initialize the jep instance
     * 
     * @return the new instance
     * @throws JepException
     */
    protected Jep initJep() throws JepException {
	Jep j = new Jep();
	// Allow implicit multiplication
	j.setImplicitMul(true);

	return j;
    }

    /**
     * Sets the layout of the applet window to BorderLayout, creates all the
     * components and associates them with event listeners if necessary.
     * 
     * @param j
     * @throws JepException
     */
    private void initComponents(Jep j) throws JepException {
	setLayout(new BorderLayout());
	setBackground(java.awt.Color.white);

	// get the initial expression from the parameters
	String expr = null;
	try {
	    expr = getParameter("initialExpression");
	    // Try to see if equation was specified in URL
	    URL docBase = this.getDocumentBase();
	    String query = docBase.getQuery();
	    if (query != null) {
		String[] parts = query.split("/\\&/");
		for (String part : parts) {
		    if (part.startsWith("EQN="))
			expr = part.substring(4);
		}
	    }
	} catch (NullPointerException e) {
	}

	// set default expression if none specified
	if (expr == null)
	    expr = "x*sin(1/x)";

	exprField = new JTextField(expr);

	// adjust various settings for the expression field
	exprField.setBackground(java.awt.Color.white);
	exprField.setName("exprField");
	exprField.setFont(new java.awt.Font("Dialog", 0, 14));
	exprField.setForeground(java.awt.Color.black);
	exprField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String exp = exprField.getText();
		exprFieldTextValueChanged(exp);
	    }
	});

	add("South", exprField);

	for (String[] eles : equations) {
	    if (map.containsKey(eles[0]))
		System.out.println("Duplicate key: " + eles[0]);
	    else {
		map.put(eles[0], eles[1]);
		listModel.addElement(eles[0]);
	    }
	}

	list.addListSelectionListener(this);
	list.setSelectionMode(ListSelectionModel.SINGLE_SELECTION);
	JScrollPane listScroller = new JScrollPane(list);
	listScroller.setPreferredSize(new Dimension(150, 80));

	add("East", listScroller);

	// create the graph canvas and add it
	graphCanvas = createGraphCanvas(j);
	add("Center", graphCanvas);
	exprFieldTextValueChanged(expr);
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {
	int index = list.getSelectedIndex();
	String key = listModel.elementAt(index);
	String val = map.get(key);
	if (val == null)
	    return;
	exprField.setText(val);
	exprFieldTextValueChanged(val);
    }

    protected FunctionCanvas createGraphCanvas(Jep j) throws JepException {
	Variable x;
	x = j.addVariable("x", 0.0);
	FunctionCanvas gc = new FunctionCanvas(j, x);
	return gc;
    }

    /**
     * Attempts to parse the expression.
     * 
     * @param newString
     * @return the node representing the expression or null on errors
     */
    private Node parseExpression(String newString) {
	// Parse the new expression
	try {
	    Node node = jep.parse(newString);
	    return node;
	} catch (ParseException e) {
	    return null;
	}
    }

    /**
     * Repaints the graphCanvas whenever the text in the expression field
     * changes.
     */
    void exprFieldTextValueChanged(String expr) {
	Node node = parseExpression(expr);
	if (node != null)
	    exprField.setForeground(Color.black);
	else
	    exprField.setForeground(Color.red);
	graphCanvas.setExpression(node);
	graphCanvas.repaint();
    }

    @Override
    public String getAppletInfo() {
	return "Jep Function Plotter\n" + "Author: N. Funk and R. Morris\n" + "Draws functions of a single variable.\n"
		+ "The initial expression can be specified with the initialExpression parameter\n"
		+ "or the URL query string in the EQN field";
    }

    @Override
    public String[][] getParameterInfo() {

	String pinfo[][] = { { "initialExpression", "mathematical expression", "initial expression to use" }, };

	return pinfo;
    }

    public static void main(String argv[]) {
	JFrame f = new JFrame();
	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	f.setSize(600, 400);

	FunctionPlotter fp = new FunctionPlotter();
	f.add(fp);
	fp.init();
	// f.pack();
	f.setVisible(true);
    }

}
