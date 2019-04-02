/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit.bigdecimal.functions;

import java.math.RoundingMode;

import org.junit.Test;

import com.singularsys.jep.bigdecimal.functions.BigDecRound;
import com.singularsys.jep.functions.*;
import com.singularsys.jeptests.unit.functions.Utilities;

public class BigDecRoundTest {
    
    @Test
    public void testRound() {
        Round roundPFMC = new BigDecRound();
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "3");
        Utilities.testBDUnary(roundPFMC, 1.5, "2");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-2");
        Utilities.testBDUnary(roundPFMC, -1.5, "-1");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }
    
    @Test
    public void testRoundUP() {
        Round roundPFMC = new BigDecRound(RoundingMode.UP);
        Utilities.testBDUnary(roundPFMC, 3.14, "4");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-4");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "3");
        Utilities.testBDUnary(roundPFMC, 1.5, "2");

        Utilities.testBDUnary(roundPFMC, 0.1, "1");
        Utilities.testBDUnary(roundPFMC, -2.5, "-3");
        Utilities.testBDUnary(roundPFMC, -1.5, "-2");
        Utilities.testBDUnary(roundPFMC, -0.1, "-1");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }

    @Test
    public void testRoundDown() {
        Round roundPFMC = new BigDecRound(RoundingMode.DOWN);
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "2");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-2");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "2");
        Utilities.testBDUnary(roundPFMC, 1.5, "1");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-2");
        Utilities.testBDUnary(roundPFMC, -1.5, "-1");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.141");
    }

    @Test
    public void testRoundCeil() {
        Round roundPFMC = new BigDecRound(RoundingMode.CEILING);
        Utilities.testBDUnary(roundPFMC, 3.14, "4");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-2");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "3");
        Utilities.testBDUnary(roundPFMC, 1.5, "2");

        Utilities.testBDUnary(roundPFMC, 0.1, "1");
        Utilities.testBDUnary(roundPFMC, -2.5, "-2");
        Utilities.testBDUnary(roundPFMC, -1.5, "-1");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }

    @Test
    public void testRoundFloor() {
        Round roundPFMC = new BigDecRound(RoundingMode.FLOOR);
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "2");
        Utilities.testBDUnary(roundPFMC, -3.14, "-4");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "2");
        Utilities.testBDUnary(roundPFMC, 1.5, "1");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-3");
        Utilities.testBDUnary(roundPFMC, -1.5, "-2");
        Utilities.testBDUnary(roundPFMC, -0.1, "-1");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.141");
    }

    @Test
    public void testRoundHalfUp() {
        Round roundPFMC = new BigDecRound(RoundingMode.HALF_UP);
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "3");
        Utilities.testBDUnary(roundPFMC, 1.5, "2");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-3");
        Utilities.testBDUnary(roundPFMC, -1.5, "-2");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }

    @Test
    public void testRoundHalfDown() {
        Round roundPFMC = new BigDecRound(RoundingMode.HALF_DOWN);
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "2");
        Utilities.testBDUnary(roundPFMC, 1.5, "1");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-2");
        Utilities.testBDUnary(roundPFMC, -1.5, "-1");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }

    @Test
    public void testRoundHalfEven() {
        Round roundPFMC = new BigDecRound(RoundingMode.HALF_EVEN);
        Utilities.testBDUnary(roundPFMC, 3.14, "3");
        Utilities.testBDUnary(roundPFMC, 2.71, "3");
        Utilities.testBDUnary(roundPFMC, -3.14, "-3");
        Utilities.testBDUnary(roundPFMC, -2.71, "-3");

        
        Utilities.testBDUnary(roundPFMC, 2.5, "2");
        Utilities.testBDUnary(roundPFMC, 1.5, "2");

        Utilities.testBDUnary(roundPFMC, 0.1, "0");
        Utilities.testBDUnary(roundPFMC, -2.5, "-2");
        Utilities.testBDUnary(roundPFMC, -1.5, "-2");
        Utilities.testBDUnary(roundPFMC, -0.1, "0");
        Utilities.testBDBinary(roundPFMC, 3.1415926, 3, "3.142");
    }

}
