/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jepexamples.consoles;
import java.applet.Applet;
import java.awt.BorderLayout;
import java.awt.TextArea;
import java.awt.event.KeyEvent;
import java.awt.event.KeyListener;
import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.io.OutputStream;
import java.io.PrintStream;
import java.util.ArrayList;
import java.util.List;
import java.util.TreeSet;
import java.util.regex.Pattern;

import com.singularsys.jep.FunctionTable;
import com.singularsys.jep.Jep;
import com.singularsys.jep.JepException;
import com.singularsys.jep.OperatorTableI;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.parser.Node;
import com.singularsys.jepexamples.EgMessages;

/**
 * This class implements a simple command line utility for evaluating
 * mathematical expressions.
 * <pre>
 *   Usage: java com.singularsys.jepexamples.consoles.Console [expression]
 * </pre>
 * If an argument is passed, it is interpreted as an expression
 * and evaluated. Otherwise, a prompt is printed, and the user can enter
 * expressions to be evaluated. 
 * 
 * <p>
 * This class and its subclasses can also be run as a java applet 
 * which displays a textarea for interactive input.
 * <p>
 * This class has been designed to be sub classed to allow different
 * consol applications.
 * The methods
 * <pre>
 * public void initialise()
 * public Object processEquation(Node node) throws Exception
 * public boolean testSpecialCommands(String command)
 * public void printPrompt()
 * public void printIntroText()
 * public void printHelp()
 * </pre>
 * can all be overwritten.
 * <p>
 * Furthermore main should be overwritten. For example
 * <pre> 
 * 	public static void main(String args[]) {
 *		Console c = new DJepConsole();
 *		c.run(args);
 *	}
 *</pre>
 *<p>
 *The main input loop is approximately
 *<pre>
 * initialise();
 * printIntroText();
 * print(getPrompt());
 * String command;
 * while((command = getCommand()) != null) 
 * {
 *	if(command.equals("quit") || command.equals("exit"))
 *		break;
 *	if(!testSpecialCommands(command)) continue;
 *   try {
 *	  Node n = j.parse(command);
 *	  processEquation(n);
 *   } catch(Exception e) {}
 *	print(getPrompt());
 * }
 *</pre>
 */

public class Console extends Applet implements KeyListener {

    private static final long serialVersionUID = 350;

    /** Main Jep object */
    protected Jep jep;	

    /** The input reader */
    private BufferedReader br;

    /** Text area for user input in applets. */
    protected TextArea ta = null;

    /** Format for double output */
    protected String doubleFormat=null;
    
    /** History */
    protected List<String> history = new ArrayList<>();
    
    protected boolean showHistory = false;
    
    /** Constructor */
    public Console() {
        br = new BufferedReader(new InputStreamReader(System.in));
    }
    
    class JepOutputStream extends OutputStream {

	@Override
	public void write(int b) throws IOException {
	    Console.this.print(b);
	}
	
    }

    protected class JepPrintStream extends PrintStream {

	@Override
	public void print(Object obj) {
	    Console.this.print(obj);
	}

	@Override
	public void println(Object x) {
	    Console.this.println(x);
	}

	@Override
	public void print(String s) {
	    Console.this.print(s);
	}

	@Override
	public void println(String x) {
	    Console.this.println(x);
	}

	public JepPrintStream() {
	    super(new JepOutputStream());
	}
    }
    /** Applet initialisation */

    @Override
    public void init() 
    {
        initialise();
        this.setLayout(new BorderLayout(1,1));
        ta = new TextArea("",10,80,TextArea.SCROLLBARS_BOTH); //$NON-NLS-1$
        ta.setEditable(true);
        ta.addKeyListener(this);
        add("Center",ta); //$NON-NLS-1$
        printIntroText();
        print(getPrompt());
    }

    /** sets up all the needed objects. */
    public void initialise()
    {
        jep = new Jep();
    }

    /** Creates a new Console object and calls run() */
    public static void main(String args[]) {
        Console c = new Console();
        c.run(args);
    }

    /** The main entry point with command line arguments 
     */
    public void run(String args[]) {
        initialise();
        boolean cont = true;
        if (args.length>0) {
            for (int i=0; i<args.length; i++)
            {
                cont = processCommand(args[i]);
            }
        }
        if(cont)
            inputLoop();
    }

    /**
     * The main input loop for interactive operation.
     * Repeatedly calls getCommand() and processCommand().
     */
    public void inputLoop() {
        String command=""; //$NON-NLS-1$

        printIntroText();
        print(getPrompt());
        while((command = getCommand()) != null) 
        {
            if( !processCommand(command)) break;
            print(getPrompt());
        }
    }

    /** 
     * Process a single command.
     * <ol>
     * <li>Calls 
     * {@link #testSpecialCommands(String)}</li>
     * <li>Tests for exit, break, and altered results.</li>
     * <li>Adds the command to the history.</li>
     * <li>Parses the command.</li>
     * <li>Calls {@link #processEquation(Node)}<li>
     * <li>Checks for errors, calling {@link #handleError(Exception)} in necessary</li>
     * </ol>
     * 
     * @param command The line to be processed
     * @return false if un-recoverable error or 'quit' or 'exit'
     */
    public boolean processCommand(String command) 
    {	
        switch(testSpecialCommands(command)) {
        case EXIT: 
            return false;
        case EMPTY: 
            return true;
        case BREAK: 
            history.add(command);
            return true;
        case ALTERED:
	    processCommand(altered);
	    return true;
        case CONTINUE: 
            break;
        }
        history.add(command);

        try {
            Node n = jep.parse(command);
            Object res=processEquation(n);
            jep.setVariable("__",res);
            jep.setVariable("_"+history.size(), res);
        }
        catch(Exception e) { return handleError(e); }

        return true;
    }



    /** Performs the required operation on a node. 
     * Typically evaluates the node and prints the value.
     * 
     * @param node Node representing expression
     * @return The result of the calculation
     * @throws JepException if a Parse or evaluation error
     */ 
    public Object processEquation(Node node) throws JepException
    {
        Object res = jep.evaluate(node);
        println(res);
        return res;
    }


    /**
     * Get a command from the input.
     * @return null if an IO error or EOF occurs.
     */
    protected String getCommand() {
        String s=null;

        if (br == null)	return null;

        try
        {
            if ( (s = br.readLine()) == null) return null;
        }
        catch(IOException e)
        {
            println(EgMessages.getString("consoles.Console.IOErrorExiting")); return null; //$NON-NLS-1$
        }
        return s;
    }

    /** Prints the prompt string. */
    public String getPrompt() { 
    	int h = history.size()+1;
    	String res = showHistory ? "" + h + " " : "";
    	res += EgMessages.getString("consoles.Console.Prompt"); 
    	return res;
    } //$NON-NLS-1$

    /** Prints a standard help message. 
     * Type 'quit' or 'exit' to quit, 'help' for help.
     **/
    public final void printStdHelp() {
        if(ta == null)
            println(EgMessages.getString("consoles.Console.ApplicationHelpMessage")); //$NON-NLS-1$
        else 
            println(EgMessages.getString("consoles.Console.AppletHelpMessage")); //$NON-NLS-1$
    }		

    /** Print help message. */
    public void printHelp() { 
        printStdHelp();
        println(EgMessages.getString("consoles.Console.FunctionsHelpMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.OperatorsHelpMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.VariablesHelpMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.FormatHelpMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.HistoryHelpMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.HistoryVarMessage"));  //$NON-NLS-1$
        println(EgMessages.getString("consoles.Console.ShowHistoryHelp"));  //$NON-NLS-1$
    }

    /** Prints introductory text. */
    public void printIntroText() {
        println(EgMessages.getString("consoles.Console.IntroText")); //$NON-NLS-1$
        printStdHelp();
    }

    /** Prints a list of defined functions. */
    public void printFuns() {
        FunctionTable ft = jep.getFunctionTable();
        println(EgMessages.getString("consoles.Console.KnownFunctions")); //$NON-NLS-1$
        for(String key:new TreeSet<>(ft.keySet()))
        {
            println("\t"+key); //$NON-NLS-1$
        }
    }

    /** Prints a list of defined operators. */
    public void printOps() {
        OperatorTableI opset = jep.getOperatorTable();
        println(opset);
    }

    /** Prints a list of variable. */
    public void printVars() {
        VariableTable st = jep.getVariableTable();
        println(EgMessages.getString("consoles.Console.Variables")); //$NON-NLS-1$
        for(Variable var:st.getVariables())
        {
            println(var);
        }
    }
    private String altered;
    /**
     * Values returned by @link{testSpecialCommands(String command)}.
     */
    public enum SPEC_ACTION {
	/**
	 * Continue processing. Add command to history.
	 */
        CONTINUE,
        /** Stop processing. Add command to history. */ 
        BREAK,
        /**
         * Quit the program.
         */
        EXIT,
        /**
         * Stop processing. Does not add the command to the history
         */
        EMPTY,
        /**
         * The command has been altered. The altered command is set using @link{setAlteredCommand(String)}
         **/
        ALTERED }
    
    /**
     * Checks for special commands. For example a subclass may have a verbose
     * mode switched on of off using the command
     * 
     * <pre>
     * verbose on
     * </pre>
     * 
     * This method can be used detected this input, perform required actions and
     * skip normal processing by returning true.
     * 
     * In general sub classes should call the superclass methods to test for
     * special commands that class implements
     * 
     * @param command
     * @return SPEC_ACTION.CONTINUE - continue processing this equation,
     *         SPEC_ACTION.BREAK - stop processing this equation and get the
     *         next line of input, SPEC_ACTION.ALTERED - the input text has been
     *         altered, SPEC_ACTION.EXIT stop the program
     * @see #split(String)
     */
    public SPEC_ACTION testSpecialCommands(String command) {
	if (Pattern.matches("^\\s*$", command)) //$NON-NLS-1$
	    return SPEC_ACTION.EMPTY;
	if (command.equals(EgMessages.getString("consoles.Console.QuitCommand")) //$NON-NLS-1$
		|| command.equals(EgMessages.getString("consoles.Console.ExitCommand"))) //$NON-NLS-1$
	    return SPEC_ACTION.EXIT;

	if (command.equals(EgMessages.getString("consoles.Console.HelpCommand"))) { //$NON-NLS-1$
	    printHelp();
	    return SPEC_ACTION.BREAK;
	}

	if (command.equals(EgMessages.getString("consoles.Console.FunctionsCommand"))) { //$NON-NLS-1$
	    printFuns();
	    return SPEC_ACTION.BREAK;
	}

	if (command.equals(EgMessages.getString("consoles.Console.OperatorsCommand"))) { //$NON-NLS-1$
	    printOps();
	    return SPEC_ACTION.BREAK;
	}

	if (command.equals(EgMessages.getString("consoles.Console.VariablesCommand"))) { //$NON-NLS-1$
	    printVars();
	    return SPEC_ACTION.BREAK;
	}
	if (command.startsWith(EgMessages.getString("consoles.Console.FormatCommand"))) { //$NON-NLS-1$
	    String[] args = split(command);
	    String format = null;
	    if (args.length >= 2) {
		if (args[1].charAt(0) == '%')
		    format = command.substring(EgMessages.getString("consoles.Console.FormatCommand").length() + 1);
		else
		    format = "%." + args[1] + "f";
	    }
	    setFormat(format);
	    return SPEC_ACTION.BREAK;
	}
	if (command.equals(EgMessages.getString("consoles.Console.HistoryCommand"))) { //$NON-NLS-1$
            history.add(command);
	    printHistory();
	    return SPEC_ACTION.EMPTY;
	}
	if (command.startsWith(EgMessages.getString("consoles.Console.ShowHistory"))) { //$NON-NLS-1$
	    String[] args = split(command);
	    showHistory = true;
	    if (args.length >= 2) {
		if (args[1].startsWith(EgMessages.getString("consoles.Console.ShowHistoryOff"))) {
		    showHistory = false;
		}
	    }

	    return SPEC_ACTION.BREAK;
	}
	if (command.startsWith("!")) { //$NON-NLS-1$
	    String alt;
	    if (command.equals("!!")) //$NON-NLS-1$
		alt = history.get(history.size() - 1);
	    else {
		if (command.length() <= 1)
		    return SPEC_ACTION.BREAK;

		String s = command.substring(1);
		try {
		    int index = Integer.parseInt(s);
		    if (index > 0)
			alt = history.get(index - 1);
		    else
			alt = history.get(history.size() + index);
		} catch (NumberFormatException e) {
		    handleError(e);
		    return SPEC_ACTION.BREAK;
		}

	    }
	    println(alt);
	    setAlteredCommand(alt);
	    return SPEC_ACTION.ALTERED;
	}
	return SPEC_ACTION.CONTINUE;
    }		

    /**
     * Set the command used if @link{SPEC_ACTION.ALTERED} returned.
     * @param alt
     */
    public void setAlteredCommand(String alt) {
        altered = alt;
    }

    private void printHistory() {
        int i=1;
        for(String line:history) {
            Variable var = jep.getVariable("_"+i);
            if(var!=null)
                println(""+i+"\t"+line+"\t"+var.getValue());
            else
                println(""+i+"\t"+line);
                
            ++i;
        }
    }

    public void setFormat(String format) {
    	this.doubleFormat=format;
    }

	/**
     * Handle an error in the parse and evaluate routines.
     * Default is to print the error message for JepExceptions and a stack trace for other exceptions
     * @param e
     * @return false if the error cannot be recovered and the program should exit
     */
    public boolean handleError(Exception e)
    {
        if(e instanceof JepException) { 
            Throwable cause = e.getCause();
            if(cause !=null && cause != e && cause instanceof JepException) {
        	e = (Exception) cause;
            }
            println(e.getClass().getSimpleName()+": "+e.getMessage()); 
        }
        else
            e.printStackTrace(new JepPrintStream());

        return true;
    }

    /** Splits a string on spaces.
     * 
     * @param s the input string
     * @return an array of the tokens in the string
     */	
    public String[] split(String s)
    {
	String[] res = s.split(" +");
	return res;
    }

    /** Prints a line of text no newline.
     * Subclasses should call this method rather than 
     * System.out.print to allow for output to different places.
     * 
     */
    public void print(Object o)
    {
	String s = toString(o);
        if(ta != null)
            ta.append(s);
        else
            System.out.print(s);
    }

    /**
     * Return string representation of object.
     * Used the doubleFormat if specified.
     * @param o
     * @return
     */
    public String toString(Object o) {
        String s=null;
        if(o == null) s = "null"; //$NON-NLS-1$
        else if(o instanceof Double && this.doubleFormat!=null)
        	s = String.format(doubleFormat, o);
        else s = o.toString();
        return s;
    }

    /** Prints a line of text followed by a newline.
     * Subclasses should call this method rather than 
     * System.out.print to allow for output to different places.
     */
    public void println(Object o)
    {
	String s = toString(o);

        if(ta != null)
            ta.append(s + "\n"); //$NON-NLS-1$
        else
            System.out.println(s);
    }

    /**
     * Handles keyRelease events
     */
    @Override
	public void keyReleased(KeyEvent event)
    {
        int code = event.getKeyCode();
        if(code == KeyEvent.VK_ENTER)
        {
            int cpos = ta.getCaretPosition();
            String alltext = ta.getText();
            String before = alltext.substring(0,cpos-1);
            int startOfLine = before.lastIndexOf('\n');
            if(startOfLine > 0)
                before = before.substring(startOfLine+1);
            String prompt = getPrompt();
            String line=null;
            if(before.startsWith(prompt))
            {
                line = before.substring(prompt.length());					
                this.processCommand(line);
            }
            //			System.out.println("line ("+line+")");
            //if(!flag) this.exit();
            this.print(getPrompt());
        }
    }

    @Override
	public void keyPressed(KeyEvent arg0)    { /* Not handled */    }

    @Override
	public void keyTyped(KeyEvent arg0)    { /* Not handled */    }

    @Override
    public String getAppletInfo()
    {
        return EgMessages.getString("consoles.Console.AppletInfo"); //$NON-NLS-1$
    }

}
