/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system.perf;

import java.io.FileInputStream;
import java.io.IOException;
import java.util.Properties;
import java.util.Vector;

/**
 * Runs a set of performance tests and stores the results in a database.
 * @author nathan
 *
 */
public class PerfRunner {
    Vector<PerfTest> tests; 

    String propertiesFile = "perf.properties";
    String databasePWProp = "databasepw";

    /**
     * 
     */
    public PerfRunner()
    {
        tests = new Vector<>();
    }
    public void addTest(PerfTest test)
    {
        tests.add(test);
    }
    /**
     * Runs the set of tests defined
     */
    public void run()
    {
        // Read properties file.
        String password;
        Properties properties = new Properties();
        try {
            properties.load(new FileInputStream(propertiesFile));
            password = (String)properties.get(databasePWProp);
        } catch (IOException e) {
            password = "";
        }

        //String password = getPassword("Enter the DB password (or press Enter to not store to DB): ");
        if (password.length() < 1) {
            System.out.println("No password provided in perf.properties file. Results will not be stored.");
        }

        // loop through each test in the test set
        for (PerfTest test:tests)
        {
            System.out.println("Running \""+test.getName()+"\" with "
                    + test.getIterations()+" iterations...");
            try {
                long t1 = System.currentTimeMillis(); 
                test.setup();
                // start timer
                long t2 = System.currentTimeMillis(); 
                test.run();
                long t3 = System.currentTimeMillis(); 
                // stop timer
                // create test result
                // save test result
                System.out.println("\tSetup:\t"+(t2-t1)+",\tRun:\t"+(t3-t2));

                // try to write it to the database
                if (password.length() > 0)
                {
                    // create a perf result entry
                    PerfResult pr = new PerfResult(test, t2-t1, t3-t2);
                    System.out.print("Trying to store to DB... ");
                    try {
                        pr.writeToDB(password);
                        System.out.println("Successfully stored record.");
                    } catch (Exception e) {

                    }
                }
            }
            catch (Exception e)
            {
                System.out.println(test.getName() + " test failed: " +e.getMessage());
                e.printStackTrace();
            }
        }
        System.out.println("Complete!");
    }



}

