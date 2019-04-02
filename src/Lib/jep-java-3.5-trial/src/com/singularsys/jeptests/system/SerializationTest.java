/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.system;

import static org.junit.Assert.assertFalse;
import static org.junit.Assert.assertTrue;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.FileOutputStream;
import java.io.InputStream;
import java.io.ObjectInputStream;
import java.io.ObjectOutputStream;
import java.util.Set;

import org.junit.Assert;
import org.junit.Test;

import com.singularsys.jep.ComponentSet;
import com.singularsys.jep.Jep;
import com.singularsys.jep.Variable;
import com.singularsys.jep.VariableTable;
import com.singularsys.jep.configurableparser.StandardConfigurableParser;
import com.singularsys.jep.misc.LightWeightComponentSet;
import com.singularsys.jep.parser.ASTVarNode;
import com.singularsys.jep.parser.Node;
import com.singularsys.jep.standard.StandardVariableTable;
import com.singularsys.jep.walkers.SerializableExpression;
import com.singularsys.jep.walkers.TreeAnalyzer;

public class SerializationTest {
    static final String FILE_NAME = "expr.ser"; 
    /**
     * Test simple serialization
     * @throws Exception
     */
    @Test
    public void testExpr() throws Exception
    {
        Jep j = new Jep();
        Node n = j.parse("1+cos(2 th)");
        SerializableExpression se = new SerializableExpression(n);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(se);
        oos.close();
        byte bytes[] = baos.toByteArray();
        System.out.println("Serialize '1+cos(2 th)': "+bytes.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);
        Node n2 = se.toNode(j);
        ois.close();
        String s = j.toString(n2);
        Assert.assertEquals("1.0+cos(2.0*th)",s);
    }

    @Test
    public void testStringRep() throws Exception
    {
        Jep j = new Jep();
        Node n = j.parse("1+cos(2 th)");
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(j.toString(n));
        oos.close();
        byte bytes[] = baos.toByteArray();
        System.out.println("Serialize '1+cos(2 th)' as a string: "+bytes.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);
        String si = (String) ois.readObject();
        Node n2 = j.parse(si);
        ois.close();
        String s = j.toString(n2);
        Assert.assertEquals("1.0+cos(2.0*th)",s);
    }


    /**
     * Test passing serialized objects between two jep instances
     * @throws Exception
     */
    @Test
    public void testSerializableExpression() throws Exception
    {
        Jep j = new Jep();
        j.addVariable("a",3.0);
        Node n = j.parse("a+cos(2 th)");
        SerializableExpression se = new SerializableExpression(n);
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(se);
        oos.close();
        byte bytes[] = baos.toByteArray();
        System.out.println("SerializeExpression '1+cos(2 th)': "+bytes.length);
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);

        Jep j2 = new Jep();
        Assert.assertNull(j2.getVariable("th"));
        j2.addVariable("a",2.0);
        SerializableExpression se2 = (SerializableExpression) ois.readObject();
        Node n2 = se2.toNode(j2);
        ois.close();
        String s = j2.toString(n2);
        Assert.assertEquals("a+cos(2.0*th)",s);
        Assert.assertNotNull(j2.getVariable("th"));
        Assert.assertEquals(2.0,j2.getVariable("a").getValue());
        Assert.assertNull(j2.getVariable("th").getValue());
        Assert.assertSame(j2.getVariable("a"),((ASTVarNode)n2.jjtGetChild(0)).getVar());
        Assert.assertNotSame(j.getVariable("a"),((ASTVarNode)n2.jjtGetChild(0)).getVar());
    }

    /**
     * Test passing serialized objects between two jep instances
     * @throws Exception
     */
    @Test
    public void testImport() throws Exception
    {
        Jep j = new Jep();
        j.addVariable("a",3.0);
        Node n = j.parse("a+cos(2 th)");

        Jep j2 = new Jep();
        Assert.assertNull(j2.getVariable("th"));
        j2.addVariable("a",2.0);
        SerializableExpression se = new SerializableExpression(n);
        Node n2 = se.toNode(j2);
        String s = j2.toString(n2);
        Assert.assertEquals("a+cos(2.0*th)",s);
        Assert.assertNotNull(j2.getVariable("th"));
        Assert.assertEquals(2.0,j2.getVariable("a").getValue());
        Assert.assertNull(j2.getVariable("th").getValue());
        Assert.assertSame(j2.getVariable("a"),((ASTVarNode)n2.jjtGetChild(0)).getVar());
        Assert.assertNotSame(j.getVariable("a"),((ASTVarNode)n2.jjtGetChild(0)).getVar());
    }

    @Test
    public void testVariableTable() throws Exception
    {
        Jep j = new Jep();
        Node n1 = j.parse("x=2");
        Node n2 = j.parse("y=x+3");
        j.evaluate(n1);
        j.evaluate(n2);
        VariableTable vt1 = j.getVariableTable();
        Set<String> keys1 = vt1.keySet();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(j.getVariableTable());
        System.out.println("Serialize Variable table: "+baos.size());

        oos.writeObject(new SerializableExpression(n1));
        oos.writeObject(new SerializableExpression(n2));
        oos.close();
        byte bytes[] = baos.toByteArray();

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        Assert.assertTrue(o instanceof StandardVariableTable);
        VariableTable vt2 = (VariableTable) o;
        Set<String> keys2 = vt2.keySet();
        Assert.assertEquals(keys1,keys2);
        for(String s:keys1) {
            Variable v1 = vt1.getVariable(s);
            System.out.println(v1);
            Variable v2 = vt2.getVariable(s);
            Assert.assertNotSame(v1,v2);
            Assert.assertEquals(v1.getValue(),v2.getValue());
        }

        Jep j2 = new Jep();
        j2.setComponent(vt2);

        SerializableExpression se3 = (SerializableExpression) ois.readObject();
        Node n3 = se3.toNode(j2);
        SerializableExpression se4 = (SerializableExpression) ois.readObject();
        Node n4 = se4.toNode(j2);
        ois.close();
        Assert.assertEquals(j.toString(n1),j2.toString(n3));
        Assert.assertEquals(j.toString(n2),j2.toString(n4));
        Assert.assertSame(vt2.getVariable("x"),((ASTVarNode) n3.jjtGetChild(0)).getVar());
    }

    @Test
    public void testJepStdParser() throws Exception
    {
        Jep j = new Jep();
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(j);
        oos.close();
        byte bytes[] = baos.toByteArray();
        System.out.println("Serialize Standard Jep: "+baos.size());

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);

        Object o = ois.readObject();
        Assert.assertTrue(o instanceof Jep);
        Jep j2 = (Jep) o;
        Node n = j2.parse("1.0+cos(2.0*th)");
        j2.addVariable("th", 0.0);
        @SuppressWarnings("unused")
        double val = ((Double) j2.evaluate(n)).doubleValue();
        j2.println(n);
    }

    @Test
    public void testJepConfigParser() throws Exception
    {
        Jep j = new Jep();
        j.setComponent(new StandardConfigurableParser());
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);
        oos.writeObject(j);
        byte bytes[] = baos.toByteArray();
        System.out.println("Serialize Configurable Jep: "+baos.size());

        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);
        Object o = ois.readObject();
        Assert.assertTrue(o instanceof Jep);
        Jep j2 = (Jep) o;
        Node n = j2.parse("1.0+cos(2.0*th)");
        j2.addVariable("th", 0.0);
        @SuppressWarnings("unused")
        double val = ((Double) j2.evaluate(n)).doubleValue();
        j2.println(n);
    }

    @Test
    public void testSizes() throws Exception
    {
        Jep j = new Jep();

        String expr[] = new String[] {
                "1","x","xx","xxx","-x","rand()","1+1","1+x","x+y","x+x","cos(x)",
                "1+2+3+4+5+6+7+8+9","a+b+c+d+e+f+g+h+ii"
        };
        for(String s:expr) {
            Node n = j.parse(s);
            SerializableExpression se = new SerializableExpression(n);
            ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
            ObjectOutputStream oos = new ObjectOutputStream(baos);
            oos.writeObject(se);
            oos.close();
            TreeAnalyzer ta = new TreeAnalyzer(n);
            System.out.println(s+":"+baos.size()+"\t"+ta.toString());
            //	    Iterator<SerializableExpression.Element> eles = se.iterator();
            //	    while(eles.hasNext())
            //		System.out.println(eles.next());
        }
    }

    @Test
    public void testWrite() throws Exception
    {
        String cwd = System.getProperty("user.dir");
        System.out.println("cwd: "+cwd);
        File f1 = new File(cwd,FILE_NAME);
        System.out.println(f1);
        FileOutputStream fos = new FileOutputStream(f1);
        ObjectOutputStream oos = new ObjectOutputStream(fos);

        Jep j = new Jep();
        Node n = j.parse("1.0+cos(2.0*th)");
        SerializableExpression se = new SerializableExpression(n);
        oos.writeObject(j);
        oos.writeObject(se);
        oos.close();
    }

    /**
     * reads from a stream which contains a serialised version of jep and and expression
     * @throws Exception
     */
    @Test
    public void testRead() throws Exception
    {
	InputStream stream = SerializationTest.class.getResourceAsStream(FILE_NAME);
        ObjectInputStream ois = new ObjectInputStream(stream);

        Jep j = (Jep) ois.readObject();
        SerializableExpression se = 
            (SerializableExpression) ois.readObject();
        ois.close();
        Node n = se.toNode(j);
        j.println(n);
    }

    @Test
    public void testConstantsLW() throws Exception
    {
        Jep j1 = new Jep();
        j1.addVariable("x",5.0);
        j1.addConstant("y",6.0);
        ComponentSet cs = new LightWeightComponentSet(j1);
        Jep lwj = new Jep(cs); 
        Variable v1 = lwj.getVariable("x");
        Variable v2 = lwj.getVariable("y");
        assertFalse("x should not be a constant",v1.isConstant());
        assertTrue("y should be a constant",v2.isConstant());
    }

    @Test
    public void testConstantsSerialised() throws Exception
    {

        Jep j = new Jep();
        j.addVariable("x",5.0);
        j.addConstant("y",6.0);

        // Setup the ObjectOutputStream
        ByteArrayOutputStream baos = new ByteArrayOutputStream(); 
        ObjectOutputStream oos = new ObjectOutputStream(baos);

        // Serialize the VariableTable
        oos.writeObject(j.getVariableTable());

        oos.close();
        byte bytes[] = baos.toByteArray();

        //  Set-up jep        
        Jep j2 = new Jep();

        // Create an ObjectInputStream 
        ByteArrayInputStream bais = new ByteArrayInputStream(bytes); 
        ObjectInputStream ois = new ObjectInputStream(bais);

        // Deserialize the SymbolTable
        VariableTable vt2 = (VariableTable) ois.readObject();

        // Set the SymbolTable as that used by the jep instance
        j2.setComponent(vt2);

        ois.close();


        Variable v1 = j2.getVariable("x");
        Variable v2 = j2.getVariable("y");
        assertFalse("x should not be a constant",v1.isConstant());
        assertTrue("y should be a constant",v2.isConstant());
    }

}
