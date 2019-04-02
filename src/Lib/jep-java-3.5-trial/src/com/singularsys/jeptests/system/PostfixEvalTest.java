/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.fail;

import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.Jep;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.misc.functions.Case;
import com.singularsys.jep.misc.functions.Switch;
import com.singularsys.jep.walkers.PostfixEvaluator;

public class PostfixEvalTest extends JepTest {


    @Override
    @Before
    public void setUp() {
        this.jep = new Jep();
        jep.setComponent(new StandardConfigurableParser());
        jep.setComponent(new PostfixEvaluator());
        jep.setAllowAssignment(true);
        jep.setAllowUndeclared(true);
        jep.setImplicitMul(true);
    }

    @Override
    @Test
    public void testSetAllowUndeclared() throws Exception {
    }

    
    @Test
    public void testPostfixBug() throws Exception {
        jep.addFunction("switch",new Switch());
        jep.addFunction("case",new Case());
        try {
            valueTest("switch(5,5,6,7,8)",8.0);
            fail("Exception should hav been thrown");
        } catch(EvaluationException e) {
            System.out.println("Expected exception thrown");
        }                                                                     
        jep.addFunction("case",new Case());
        valueTest("case(1.0,1.0,5,2.0,6,3.0,7,8)",5.0);
    }

    @Override
    public void testNull() throws Exception { // does nothing
    }

	@Override
	public void testCaseNull() throws Exception { // does nothing
	}


}
