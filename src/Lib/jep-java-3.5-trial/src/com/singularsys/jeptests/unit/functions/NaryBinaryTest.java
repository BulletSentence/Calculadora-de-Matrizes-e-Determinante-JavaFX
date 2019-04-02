/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import static org.junit.Assert.assertEquals;
import static org.junit.Assert.fail;

import org.junit.Test;
import com.singularsys.jep.EvaluationException;
import com.singularsys.jep.functions.*;


public class NaryBinaryTest {
    
    @Test
    public void testNaryBinary() throws EvaluationException {
        Add pfmc = new Add();

        Double[] vals = new Double[]{4.0,3.0,2.0,1.0};
    	Object res = pfmc.eval(vals);
    	assertEquals(10.0,res);

    	vals = new Double[]{4.0,3.0,2.0};
    	res = pfmc.eval(vals);
    	assertEquals(9.0,res);

    	vals = new Double[]{4.0,3.0};
    	res = pfmc.eval(vals);
    	assertEquals(7.0,res);

    	vals = new Double[]{4.0};
    	res = pfmc.eval(vals);
    	assertEquals(4.0,res);

    	vals = new Double[]{};
    	try {
    		res = pfmc.eval(vals);
    		fail();
    	} catch (EvaluationException e) {
    		System.out.println("Expected exception \""+e.toString()+"\" caught.");
    	}
    	assertEquals(4.0,res);

    }

}
