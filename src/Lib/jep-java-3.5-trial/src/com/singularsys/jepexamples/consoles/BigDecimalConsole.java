/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 18 Dec 2006 - Richard Morris
 */
package com.singularsys.jepexamples.consoles;

import java.math.MathContext;

import com.singularsys.jep.Jep;
import com.singularsys.jep.bigdecimal.BigDecComponents;
import com.singularsys.jepexamples.EgMessages;

public class BigDecimalConsole extends Console {
    private static final long serialVersionUID = 1L;

    BigDecComponents bdc;
    /**
     * @param args
     */
    public static void main(String[] args) {
        BigDecimalConsole bdc = new BigDecimalConsole();
        bdc.run(args);
    }

    @Override
    public void initialise() {
        bdc = new BigDecComponents(MathContext.UNLIMITED);
        jep = new Jep(bdc);
    }

    @Override
    public String getPrompt() {
        return EgMessages.getString("consoles.BigDecimalConsole.Prompt"); //$NON-NLS-1$
    }

    @Override
    public SPEC_ACTION testSpecialCommands(String command) {
        if(command.toLowerCase().startsWith(EgMessages.getString("consoles.BigDecimalConsole.SetPrecedenceCommand").toLowerCase())) { //$NON-NLS-1$
            String[] args = split(command);
            String prec = args[1];
            String rm;
            if(args.length>=3)
                rm = args[2];
            else
                rm = "HALF_UP"; //$NON-NLS-1$
            bdc.setMathContext(new MathContext("precision="+prec+" roundingMode="+rm)); //$NON-NLS-1$ //$NON-NLS-2$
            return SPEC_ACTION.BREAK;
        }
        return super.testSpecialCommands(command);
    }

    @Override
    public void printHelp() {
        super.printHelp();
        println(EgMessages.getString("consoles.BigDecimalConsole.SetPrecedenceHelpMessage")); //$NON-NLS-1$
        println(EgMessages.getString("consoles.BigDecimalConsole.PrecisionHelpDetails")); //$NON-NLS-1$
        println(EgMessages.getString("consoles.BigDecimalConsole.RoundingModeHelpDetails")); //$NON-NLS-1$
    }

}
