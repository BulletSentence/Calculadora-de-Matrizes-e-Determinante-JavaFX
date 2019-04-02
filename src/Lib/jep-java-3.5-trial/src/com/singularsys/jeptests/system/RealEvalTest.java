/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.reals.RealEvaluator;

public class RealEvalTest extends CPTest {

    @Override
    @Before
    public void setUp() {
	super.setUp();
	jep.setComponent(new RealEvaluator());
	this.myFalse = new Double(0.0);
	this.myTrue = new Double(1.0);
    }

    @Override
    protected void valueTest(String expr, Object expected) throws Exception {
	if (expected instanceof Integer)
	    super.valueTest(expr, ((Integer) expected).doubleValue());
	else
	    super.valueTest(expr, expected);
    }

    @Override
    public void testEvaluateComplex() throws Exception {
	// tests not run
    }

    @Override
    public void testEvaluateString() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testComplex() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testChangeVariableComplex() {
	// tests not run
    }

    @Override
    @Test
    public void testListAccess() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testListFunctions() throws Exception {
	// tests not run
    }

    @Override
    public void testListExtra() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testMultiDimArray() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testMultiDimArrayAccess() throws Exception {
	// tests not run
    }

    @Override
    public void testDepth3ArrayAccess() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testStrings() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testStringsFun() throws Exception {
	// tests not run
    }

    @Override
    @Test
    public void testCPStrings() throws Exception {
	// tests not run
    }

    @Override
    public void testCaseString() throws Exception {
	// tests not run
    }

    @Override
    public void testCaseNull() throws Exception { // does nothing
    }

    @Override
    public void testSpecialFunctions() throws Exception {
	testSpecialFunctions(true, true);
    }

}
