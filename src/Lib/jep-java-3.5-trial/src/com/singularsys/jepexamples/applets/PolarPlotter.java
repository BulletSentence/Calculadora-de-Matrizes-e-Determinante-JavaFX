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
import java.awt.Component;
import java.awt.Dimension;
import java.awt.GridBagConstraints;
import java.awt.GridBagLayout;
import java.awt.event.ActionEvent;
import java.awt.event.ActionListener;
import java.util.HashMap;
import java.util.Map;

import javax.swing.DefaultListModel;
import javax.swing.JApplet;
import javax.swing.JFrame;
import javax.swing.JLabel;
import javax.swing.JList;
import javax.swing.JPanel;
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
 * The FunctionPlotter class arranges the text field and GraphCanvas classes and
 * requests a repainting of the graph when the expression in the text field
 * changes. All plotting (and interaction with the Jep API) is performed in
 * GraphCanvas class.
 */
public class PolarPlotter extends JApplet implements ListSelectionListener {
    private static final long serialVersionUID = 330L;

    /** The expression field */
    JTextField rexprField;

    JTextField minField;
    JTextField maxField;
    JTextField stepsField;

    /** The canvas for plotting the graph */
    private PolarCanvas graphCanvas;

    protected Jep jep;

    double tMin = 0;
    double tMax = 2 * Math.PI;
    int tSteps = 2000;

    /** List of equations */
    /** List of equations */
    DefaultListModel<String> listModel = new DefaultListModel<>();
    JList<String> list = new JList<>(listModel);

    Map<String, String[]> map = new HashMap<>();

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
	j.setAllowUndeclared(false);
	j.setAllowAssignment(false);
	return j;
    }

    JTextField createTextField(String parameter, String defaultVal) {
	String expr;
	try {
	    expr = getParameter(parameter);
	    if (expr == null)
		expr = defaultVal;
	} catch (NullPointerException e) {
	    expr = defaultVal;
	}
	if (expr == null)
	    expr = defaultVal;
	JTextField field = new JTextField(expr);
	field.setBackground(java.awt.Color.white);
	field.setFont(new java.awt.Font("Dialog", 0, 14));
	field.setForeground(java.awt.Color.black);
	return field;
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

	rexprField = createTextField("rexpr", "th^2");

	rexprField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String expr = rexprField.getText();
		rFieldChanged(expr);
	    }
	});
	minField = createTextField("min", "0");
	minField.setColumns(7);
	maxField = createTextField("max", "2 pi");
	maxField.setColumns(7);
	stepsField = createTextField("steps", String.valueOf(tSteps));
	stepsField.setColumns(5);

	minField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String expr = minField.getText();
		minFieldChanged(expr);
	    }
	});
	maxField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String expr = maxField.getText();
		maxFieldChanged(expr);
	    }
	});
	stepsField.addActionListener(new ActionListener() {
	    @Override
	    public void actionPerformed(ActionEvent arg0) {
		String expr = stepsField.getText();
		stepsFieldChanged(expr);
	    }
	});

	final GridBagLayout gbl = new GridBagLayout();
	final GridBagConstraints gbc = new GridBagConstraints();
	final JPanel pan = new JPanel(gbl);
	gbc.ipadx = 2;
	// pan.setBackground(Color.cyan);
	gbc.fill = GridBagConstraints.HORIZONTAL;
	gbc.weightx = 1;
	class Adder {
	    void addGrid(Component comp, int x, int y) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = 1;
		gbc.weightx = 0;
		gbl.setConstraints(comp, gbc);
		pan.add(comp);
	    }

	    void addGrid(Component comp, int x, int y, int w) {
		gbc.gridx = x;
		gbc.gridy = y;
		gbc.gridwidth = w;
		gbc.weightx = 1;
		gbl.setConstraints(comp, gbc);
		pan.add(comp);
	    }
	}
	Adder adder = new Adder();
	adder.addGrid(new JLabel(" r"), 0, 0);
	adder.addGrid(rexprField, 1, 0, GridBagConstraints.REMAINDER);

	adder.addGrid(new JLabel(" th"), 0, 1);
	adder.addGrid(new JLabel("min"), 1, 1);
	adder.addGrid(minField, 2, 1);
	adder.addGrid(new JLabel("max"), 3, 1);
	adder.addGrid(maxField, 4, 1);
	adder.addGrid(new JLabel("steps"), 5, 1);
	adder.addGrid(stepsField, 6, 1);
	adder.addGrid(new JLabel(), 7, 1);

	add("South", pan);

	for (String[] eles : equations) {
	    if (map.containsKey(eles[0]))
		System.out.println("Duplicate key: " + eles[0]);
	    else {
		String[] vals = new String[eles.length - 1];
		for (int i = 1; i < eles.length; ++i)
		    vals[i - 1] = eles[i];
		map.put(eles[0], vals);
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
	rFieldChanged(rexprField.getText());
    }

    protected PolarCanvas createGraphCanvas(Jep j) throws JepException {
	Variable x;
	x = j.addVariable("th", 0.0);
	PolarCanvas gc = new PolarCanvas(j, x, 0, 2 * Math.PI, tSteps);
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
	    System.out.println(e.getMessage());
	    return null;
	}
    }

    /**
     * Repaints the graphCanvas whenever the text in the expression field
     * changes.
     */
    void rFieldChanged(String expr) {
	Node node = parseExpression(expr);
	if (node != null)
	    rexprField.setForeground(Color.black);
	else
	    rexprField.setForeground(Color.red);
	graphCanvas.setRexpression(node);
	graphCanvas.repaint();
    }

    void minFieldChanged(String s) {
	try {
	    jep.parse(s);
	    tMin = jep.evaluateD();
	    minField.setForeground(Color.black);
	    graphCanvas.setRange(tMin, tMax, tSteps);
	    graphCanvas.repaint();
	} catch (JepException e) {
	    minField.setForeground(Color.red);
	}
    }

    void maxFieldChanged(String s) {
	try {
	    jep.parse(s);
	    tMax = jep.evaluateD();
	    maxField.setForeground(Color.black);
	    graphCanvas.setRange(tMin, tMax, tSteps);
	    graphCanvas.repaint();
	} catch (JepException e) {
	    maxField.setForeground(Color.red);
	}
    }

    void stepsFieldChanged(String s) {
	try {
	    tSteps = Integer.parseInt(s);
	    stepsField.setForeground(Color.black);
	    graphCanvas.setRange(tMin, tMax, tSteps);
	    graphCanvas.repaint();
	} catch (NumberFormatException e) {
	    stepsField.setForeground(Color.red);
	}
    }

    @Override
    public void valueChanged(ListSelectionEvent arg0) {

	int index = list.getSelectedIndex();
	String key = listModel.elementAt(index);
	String[] vals = map.get(key);
	if (vals.length == 0)
	    return;
	rexprField.setText(vals[0]);
	rFieldChanged(vals[0]);
	if (vals.length >= 3) {
	    minField.setText(vals[1]);
	    maxField.setText(vals[2]);
	    minFieldChanged(vals[1]);
	    maxFieldChanged(vals[2]);
	}
	if (vals.length >= 4) {
	    stepsField.setText(vals[3]);
	    stepsFieldChanged(vals[3]);
	}
    }

    String[][] equations = new String[][] { { "Circle", "1" }, { "Circle center (1,0)", "2 cos(th)", "0", "pi" },
	    { "Vertical line", "1/cos(th)", "0", "pi" }, { "Ellipse", "1/(1 + 0.5 cos(th))", "-pi", "pi" },
	    { "Parabola", "1/(1 + cos(th))", "-pi", "pi" }, { "Hyperbola", "1/(1 + 1.5 cos(th))", "-pi", "pi" },
	    { "---- Spirals ----" }, { "Archimedean spiral", "th/pi", "0", "6 pi" },
	    { "Fermat's spiral", "sqrt(th)", "0", "6 pi" }, { "Hyperbolic spiral", "1/th", "0", "20 pi" },
	    { "Lituus", "1/sqrt(th)", "0", "20 pi" }, { "Logarithmic spiral", "1.1^th", "0", "6 pi" },
	    { "Lemniscate of Bernoulli", "sqrt(cos(2 th))", "-pi/4", "5pi/4", "451" }, { "---- Rhodonea curves ----" },
	    { "Rose 2", "cos(2 th)", "-pi", "pi" }, { "Rose 3", "cos(3 th)", "-pi", "pi" },
	    { "Rose 4", "cos(4 th)", "-pi", "pi" }, { "Rose 5", "cos(5 th)", "-pi", "pi" },
	    { "Rose 6", "cos(6 th)", "-pi", "pi" }, { "Rose 1/2", "cos(th/2)", "-2pi", "2pi" },
	    { "Rose 3/2", "cos(3 th/2)", "-2pi", "2pi" }, { "Rose 5/2", "cos(5 th/2)", "-2pi", "2pi" },
	    { "Rose 1/3", "cos(th/3)", "-3pi", "3pi" }, { "Rose 2/3", "cos(2 th/3)", "-3pi", "3pi" },
	    { "Rose 4/3", "cos(4 th/3)", "-3pi", "3pi" }, };

    public static void main(String argv[]) {
	JFrame f = new JFrame();
	f.setDefaultCloseOperation(JFrame.DISPOSE_ON_CLOSE);
	f.setSize(600, 400);
	PolarPlotter pp = new PolarPlotter();
	f.add(pp);
	pp.init();
	// f.pack();
	f.setVisible(true);
    }
}
