/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 /*
Created 2 Nov 2006 - Richard Morris
 */
package com.singularsys.jepexamples.consoles;

import com.singularsys.jep.JepException;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.walkers.PrefixTreeDumper;
import com.singularsys.jep.walkers.TreeAnalyzer;
import com.singularsys.jepexamples.EgMessages;

/**
 * A console application which dumps the tree representing an equation and statistics about the tree. 
 * @see TreeAnalyzer
 * @see PrefixTreeDumper
 */
public class PrefixDumperConsole extends Console {
    private static final long serialVersionUID = 1L;
    PrefixTreeDumper dumper;
    TreeAnalyzer totals = new TreeAnalyzer();
    boolean setDump = true;
    boolean setStats = true;
    
    public static void main(String[] args)
    {
        Console c = new PrefixDumperConsole();
        c.run(args);
    }

    @Override
    public void initialise() {
        super.initialise();
        dumper = new PrefixTreeDumper();
    }

    @Override
    public Object processEquation(Node node) throws JepException {
        Object value = this.jep.evaluate(node);
        println(EgMessages.getString("consoles.PrefixDumperConsole.Result")+value); //$NON-NLS-1$
        TreeAnalyzer tra = new TreeAnalyzer(node);
        totals.merge(tra);
        if(this.setStats) {
            println(tra.toString());
            println(""); //$NON-NLS-1$
        }
        if(this.setDump)
            dumper.dump(node);
        return value;
    }

    @Override
    public void printHelp() {
        super.printHelp();
        this.println(EgMessages.getString("consoles.PrefixDumperConsole.SetDumpHelpMessage")); //$NON-NLS-1$
        this.println(EgMessages.getString("consoles.PrefixDumperConsole.SetStatsHelpMessage")); //$NON-NLS-1$
        this.println(EgMessages.getString("consoles.PrefixDumperConsole.TotalsHelpMessage")); //$NON-NLS-1$
    }

    @Override
    public SPEC_ACTION testSpecialCommands(String command) {
        if(command.startsWith(EgMessages.getString("consoles.PrefixDumperConsole.SetDumpCommand"))) { //$NON-NLS-1$
            setDump = (EgMessages.getString("consoles.PrefixDumperConsole.YesOption").equals(this.split(command)[1])); //$NON-NLS-1$
            return SPEC_ACTION.BREAK;
        }
        if(command.startsWith(EgMessages.getString("consoles.PrefixDumperConsole.SetStatsCommand"))) { //$NON-NLS-1$
            setStats = (EgMessages.getString("consoles.PrefixDumperConsole.YesOption").equals(this.split(command)[1])); //$NON-NLS-1$
            return SPEC_ACTION.BREAK;
        }
        if(command.startsWith(EgMessages.getString("consoles.PrefixDumperConsole.TotalsCommand"))) { //$NON-NLS-1$
            this.println(totals.toString());
            return SPEC_ACTION.BREAK;
        }
        return super.testSpecialCommands(command);
    }


}
