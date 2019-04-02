/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.functions;

import org.junit.Test;
import com.singularsys.jep.functions.*;


public class RoundTest {
    
    @Test
    public void testRounding() {
        Round roundPFMC = new Round();
        // Should not depend on round mode
        Utilities.testUnary(roundPFMC, 3.14, 3);
        Utilities.testUnary(roundPFMC, 2.71, 3);
        Utilities.testUnary(roundPFMC, -3.14, -3);
        Utilities.testUnary(roundPFMC, -2.71, -3);

        Utilities.testUnary(roundPFMC, 2.5, 3.0);
        Utilities.testUnary(roundPFMC, 1.5, 2.0);
        Utilities.testUnary(roundPFMC, 0.1, 0.0);
        Utilities.testUnary(roundPFMC, -2.5, -2.0);
        Utilities.testUnary(roundPFMC, -1.5, -1.0);
        Utilities.testUnary(roundPFMC, -0.1, 0.0);
        Utilities.testBinary(roundPFMC, 3.1415926, 2, 3.14);
    }
    

}
