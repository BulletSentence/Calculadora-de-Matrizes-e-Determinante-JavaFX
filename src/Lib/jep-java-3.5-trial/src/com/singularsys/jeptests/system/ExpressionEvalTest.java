/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 
package com.singularsys.jeptests.system;

import java.io.*;

import org.junit.Assert;
import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.ParseException;
import com.singularsys.jep.standard.Complex;
import com.singularsys.jep.standard.FastEvaluator;
import com.singularsys.jep.walkers.PostfixEvaluator;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.reals.RealEvaluator;

/**
 * This class is designed for testing the validity of Jep evaluations.
 * Expressions from a text file are evaluated with Jep in pairs of two, and
 * the results are compared. If they do not match, the two expressions are 
 * printed to standard output.<p>
 * Take for example an input text file containing the two lines
 * <pre>1+2
 *3.</pre>
 * The expressions '1+2' and '3' are evaluated with Jep and the results compared.
 * 
 * @author Nathan Funk
 */
public class ExpressionEvalTest {

    /** Switch for printing expressions while running tests */
    static final boolean printExpressions = true;

    /** Current line position */
    protected int lineCount;

    /**
     * Creates a new ExpressionEvalTest instance
     */
    public ExpressionEvalTest() { // does nothing

    }

    /**
     * The main method checks the arguments and creates an instance
     * and runs a test
     */
    public static void main(String args[]) {
        String fileName;

        // get filename from argument, or use default
        if (args!=null && args.length>0) {
            fileName = args[0];
        } else {
            fileName = "JEPTestExpressions.txt";
            println("Using default input file: " + fileName);
        }

        // Create an instance of this class and analyse the file
        ExpressionEvalTest jt = new ExpressionEvalTest();
        try {
            jt.testStandard();
        } catch (Exception e) {
            e.printStackTrace();
        }
    }

    @Test
    public void testStandard() throws Exception {
        Jep jep;
        System.out.println("--------------------------------------------------------------");
        jep = new Jep();
        jep.setImplicitMul(true);
        jep.getVariableTable().remove("true");
        jep.getVariableTable().remove("false");
        jep.addVariable("true", Boolean.TRUE);
        jep.addVariable("false", Boolean.FALSE);
        lineCount = 0;

        testJepInstance(jep, "JEPTestExpressions.txt");
    }

    @Test
    public void testFast() throws Exception {
        Jep jep;
        System.out.println("--------------------------------------------------------------");
        jep = new Jep(new FastEvaluator());
        jep.setImplicitMul(true);
        jep.getVariableTable().remove("true");
        jep.getVariableTable().remove("false");
        jep.addVariable("true", Boolean.TRUE);
        jep.addVariable("false", Boolean.FALSE);
        lineCount = 0;

        testJepInstance(jep, "JEPTestExpressions.txt");
    }

    @Test
    public void testConfParser() throws Exception  {
        Jep jep;
        System.out.println("--------------------------------------------------------------");
        jep = new Jep(new StandardConfigurableParser());
        jep.setImplicitMul(true);
        jep.getVariableTable().remove("true");
        jep.getVariableTable().remove("false");
        jep.addVariable("true", Boolean.TRUE);
        jep.addVariable("false", Boolean.FALSE);
        lineCount = 0;

        testJepInstance(jep, "JEPTestExpressions.txt");
    }

    @Test
    public void testPostfixEvaluator() throws Exception  {
        Jep jep;
        System.out.println("--------------------------------------------------------------");
        jep = new Jep(new PostfixEvaluator());
        jep.setImplicitMul(true);
        jep.getVariableTable().remove("true");
        jep.getVariableTable().remove("false");
        jep.addVariable("true", Boolean.TRUE);
        jep.addVariable("false", Boolean.FALSE);
        lineCount = 0;

        testJepInstance(jep, "JEPTestExpressions.txt");
    }

    @Test
    public void testReals() throws Exception  {
        Jep jep;

        System.out.println("--------------------------------------------------------------");
        jep = new Jep(new RealEvaluator());
        jep.setImplicitMul(true);
        jep.getVariableTable().remove("true");
        jep.getVariableTable().remove("false");
        jep.addVariable("true", Boolean.TRUE);
        jep.addVariable("false", Boolean.FALSE);
        lineCount = 0;

        testJepInstance(jep, "JEPTestExpressionsReals.txt");
    }

    /**
     * Loads the file specified in fileName. Evaluates the expressions listed
     * in it and compares the expressions with the results.
     */
    public void testJepInstance(Jep jep, String fileName) {
        BufferedReader reader;

        InputStream is = ExpressionEvalTest.class.getResourceAsStream(fileName);
        
        // Load the input file
        try {
            if(is!=null)
                reader = new BufferedReader(new InputStreamReader(is));
            else
                reader = new BufferedReader(new FileReader(fileName));
            println("Reading from "+fileName);
        } catch (Exception e) {
            println("File \""+fileName+"\" not found");

            Assert.fail("File \""+fileName+"\" not found");
            return;
        }

        testJepInstance(jep, reader);
    }

    public void testJepInstance(Jep jep, BufferedReader reader) {
	Object v1, v2;
        String expression1, expression2;
        boolean hasError = false;
        
        // reset the line count
        lineCount = 0;

        // cycle through the expressions in pairs of two
        println("Evaluating and comparing expressions...");
        while (true) {
            v1 = null;
            v2 = null;

            // get values of a pair of two lines
            try {
                expression1 = getNextLine(reader);
                expression2 = getNextLine(reader);
                if (expression1 != null && expression2 != null) {
                    if (printExpressions) 
                        System.out.println("Checking \"" + expression1 + "\" == \"" + expression2 + "\"?");
                    v1 = parseNextLine(jep, expression1);
                    v2 = parseNextLine(jep, expression2);
                }
            } catch (Exception e) {
                println("Exception occured: "+e.getMessage());
                e.printStackTrace();
                hasError = true;
                //break;
                continue;
            }

            // expression1 or expression2 is null when end of file is reached
            if (expression1 == null || expression2 == null) {
                println("Reached end of file.");
                break;
            }

            // compare the results
            if (!equal(v1, v2)) {
                hasError = true;
                print("Line: " + lineCount + ": ");
                println("\"" + expression1 + "\" (" + v1 + ") != \"" 
                        + expression2 + "\" (" + v2 +")");
            }
        }

        // Closing remarks
        print("\n" + lineCount + " lines processed. ");
        if (hasError) {
            print("Errors were found.\n\n");
        } else {
            print("No errors were found.\n\n");
        }

        // Fail if errors are found
        Assert.assertTrue("Errors were found.", !hasError);
    }

    /**
     * Reads the next line from the Reader into a  String.
     * @throws Exception when IOException occurs, parsing fails, or when
     *         evaluation fails
     */
    private String getNextLine(BufferedReader reader) throws Exception {
        String line;

        // cycle till a valid line is found
        do {
            line = reader.readLine(); // returns null on end of file
            if (line == null) return null;
            lineCount++;
        } while (line.length() == 0 || line.trim().charAt(0) == '#');

        return line;
    }

    /**
     * Parses a single line from the reader, and returns the
     * evaluation of that line.
     * @return the value of the evaluated line. Returns null when the end of the file
     *         is reached.
     * @throws Exception when parsing fails, or when
     *         evaluation fails
     */
    private Object parseNextLine(Jep jep, String line) throws Exception {
        Object value;
        String errorStr;
        // parse the expression
        try {
            jep.parse(line);
        } catch (ParseException e) {
            // an error occur while parsing
            errorStr = e.getMessage();
            throw new Exception("Error while parsing line " + lineCount + ": " + errorStr);
        }

        // evaluate the expression
        try {
            value = jep.evaluate();
            if (value == null) throw new EvaluationException("Evaluated as null");
        } catch (EvaluationException e) {
            errorStr = e.getMessage();
            throw new Exception("Error while evaluating line " + lineCount + ": " + errorStr);
        }

        return value;
    }

    /**
     * Compares o1 and o2. Copied from Comparative.java.
     * @return true if o1 and o2 are equal. false otherwise.
     */
    private boolean equal(Object param1, Object param2)
    {
        double tolerance = 1e-15;
        if ((param1 instanceof Complex) && (param2 instanceof Complex)) {
            return ((Complex)param1).equals((Complex)param2, tolerance);
        }
        if ((param1 instanceof Complex) && (param2 instanceof Number)) {
            return ((Complex)param1).equals(new Complex((Number) param2), tolerance);
        }
        if ((param2 instanceof Complex) && (param1 instanceof Number)) {
            return ((Complex)param2).equals(new Complex((Number) param1), tolerance);
        }
        if ((param1 instanceof Number) && (param2 instanceof Number)) {
            return Math.abs(((Number)param1).doubleValue()-((Number)param2).doubleValue())
            < tolerance;
        }
        // test any other types here
        return param1.equals(param2);
    }



    /**
     * Helper function for printing.
     */
    private static void print(String str) {
        System.out.print(str);
    }

    /**
     * Helper function for printing lines.
     */
    protected static void println(String str) {
        System.out.println(str);
    }
}
