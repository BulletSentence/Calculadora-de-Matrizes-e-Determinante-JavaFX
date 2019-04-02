/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system.perf;

import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.Vector;

/**
 * Stores a single performance test result and contains logic to write this to
 * a DB.
 */
public class PerfResult {

    PerfTest test;
    double setupTime, runTime;

    public PerfResult(PerfTest test, double setupTime, double runTime)
    {
        this.test = test;
        this.setupTime = setupTime;
        this.runTime = runTime;
    }


    public void writeToDB(String password) throws Exception
    {
        String connString = "jdbc:mysql://www.singularsys.com/singularsys";
        String loginName = "singularsys";

        try {
            // The newInstance() call is a work around for some
            // broken Java implementations

            Class.forName("com.mysql.jdbc.Driver").newInstance();
        } catch (Exception ex) {
            // handle the error
            System.out.println("Failed to load MySQL driver");
            throw new Exception(ex);
        }
        Connection conn;
        try {
            conn = DriverManager.getConnection(connString, loginName, password);
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            throw new Exception(ex);
        }
        Statement stmt = null;
        ResultSet rs = null;

        // build the SQL query
        Vector<String> strArray = buildSettingsStrings();
        StringBuffer query = new StringBuffer(200);
        query.append("INSERT INTO Performance SET ");
        for (int i = 0; i<strArray.size(); i++)
        {
            if (i > 0) query.append(", ");
            query.append(strArray.elementAt(i));
        }

        // try issuing the query 
        try {
            stmt = conn.createStatement();
            stmt.executeUpdate(query.toString());
        } catch (SQLException ex) {
            // handle any errors
            System.out.println("SQLException: " + ex.getMessage());
            System.out.println("SQLState: " + ex.getSQLState());
            System.out.println("VendorError: " + ex.getErrorCode());
            throw new Exception(ex);
        } finally {
            // it is a good idea to release resources in a finally{} block
            // in reverse-order of their creation if they are no-longer needed
            try {
                if (rs != null) {
                    rs.close();
                }

                if (stmt != null) {
                    stmt.close();
                }
            } catch (SQLException sqlEx) { } // ignore
        }
    }

    /**
     * Builds an array of strings for later use in the SQL query for inserting
     * an entry in the DB
     * @return
     */
    private Vector<String> buildSettingsStrings() {
        Vector<String> output = new Vector<>();
        output.add("JepVersion = '3.2'");
        output.add("TestName = '" + test.getName() + "'");
        output.add("Expression = '" + test.getExpression() + "'");
        output.add("Iterations = '" + test.getIterations() + "'");
        output.add("SetupTime = '" + setupTime +"'");
        output.add("RunTime = '" + runTime +"'");
        /*		// if it's a parse test, specify the parser used
		if (test instanceof ParsePerfTest) {
			ParsePerfTest pt = (ParsePerfTest)test;
			output.add("Parser = " +
					(pt.useConfigParser ? "'ConfigurableParser'" : "'StandardParser'"));
		}*/
        return output;
    }
}
