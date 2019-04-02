/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;


import static org.junit.Assert.*;

import java.util.ArrayList;
import java.util.List;

import org.junit.After;
import org.junit.Before;
import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.misc.VariableTableObserver;
import com.singularsys.jep.parser.Node;

public class ObservingTest {
    String TBL_CLEAR = "table cleared";
    String VAR_ADD = "variable added ";
    String VAR_DEL = "variable deleted ";
    String VAR_CHANGED = "variable changed ";
    class MyVariableObserver extends VariableTableObserver {
        List<String> actions = new ArrayList<>();


        public MyVariableObserver(Jep jep, boolean watchValues) {
			super(jep, watchValues);
		}

		/**
         * @param jep
         */
        public MyVariableObserver(Jep jep) {
            super(jep);
        }

        @Override
        protected void tableCleared(VariableTable table) {
            actions.add(TBL_CLEAR);
            super.tableCleared(table);
        }

        @Override
        protected void variableAdded(Variable var) {
            actions.add(VAR_ADD+var.getName());
            super.variableAdded(var);
        }

        @Override
        protected void variableRemoved(Variable var) {
            actions.add(VAR_DEL+var.getName());
            super.variableRemoved(var);
        }

        @Override
        protected void variableChanged(Variable var) {
            actions.add(VAR_CHANGED+var.getName());
            super.variableChanged(var);
        }

    }
    Jep jep;
    @Before
    public void setUp() throws Exception {
        jep = new Jep();
    }

    @After
    public void tearDown() throws Exception {
    }

    @Test
    public void testObserving() throws Exception {
        MyVariableObserver vo = new MyVariableObserver(jep);
        vo.actions.clear();
        jep.addVariable("x");
        assertEquals(1,vo.actions.size());
        assertEquals(VAR_ADD+"x",vo.actions.get(0));

        vo.actions.clear();
        Node n = jep.parse("y=5");
        assertEquals(1,vo.actions.size());
        assertEquals(VAR_ADD+"y",vo.actions.get(0));

        vo.actions.clear();
        jep.evaluate(n);
        assertEquals(1,vo.actions.size());
        assertEquals(VAR_CHANGED+"y",vo.actions.get(0));

    }

    @Test
    public void testObservingNoValues() throws Exception {
        MyVariableObserver vo = new MyVariableObserver(jep,false);
        vo.actions.clear();
        jep.addVariable("x");
        assertEquals(1,vo.actions.size());
        assertEquals(VAR_ADD+"x",vo.actions.get(0));

        vo.actions.clear();
        Node n = jep.parse("y=5");
        assertEquals(1,vo.actions.size());
        assertEquals(VAR_ADD+"y",vo.actions.get(0));

        vo.actions.clear();
        jep.evaluate(n);
        assertEquals(0,vo.actions.size());
        //assertEquals(VAR_CHANGED+"y",vo.actions.get(0));

    }

}
