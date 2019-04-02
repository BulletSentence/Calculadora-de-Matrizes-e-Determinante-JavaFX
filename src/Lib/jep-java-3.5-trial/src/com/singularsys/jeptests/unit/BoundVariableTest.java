/*****************************************************************************

 Jep 3.5
   2017
   (c) Copyright 2017, Singular Systems
   See LICENSE-*.txt for license information.

 *****************************************************************************/

 package com.singularsys.jeptests.unit;

import static org.junit.Assert.assertEquals;

import org.junit.Test;

import com.singularsys.jep.Jep;
import com.singularsys.jep.Variable;
import com.singularsys.jep.misc.boundvariable.BoundVariableFactory;
import com.singularsys.jep.misc.boundvariable.ChainedObjectVariableBindingMapper;
import com.singularsys.jep.misc.boundvariable.FieldVariableBinding;
import com.singularsys.jep.misc.boundvariable.MutableDouble;
import com.singularsys.jep.parser.Node;

public class BoundVariableTest {

	public class MyObj {
		public Double a;
		public double b;
		public MyObj c;
	}
	
	@Test
	public void testObjectFieldAccessor() throws Exception {
		Jep jep = new Jep(new BoundVariableFactory());
//		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		MyObj obj = new MyObj();
		obj.a = new Double(5);
		obj.b = 6;
		FieldVariableBinding accA = new FieldVariableBinding(obj,"a");
		FieldVariableBinding accB = new FieldVariableBinding(obj,"b");
		
		Variable v1 = jep.addVariable("a", accA);
		Object val1=v1.getValue();
		assertEquals(new Double(5),val1);
		
		Variable v2 = jep.addVariable("b", accB);
		assertEquals(new Double(6),v2.getValue());
		
		Variable v3 = jep.addVariable("x", 3.0);
		assertEquals(new Double(3),v3.getValue());
		
		Node n = jep.parse("x+a+b");
		Object val = jep.evaluate(n);
		assertEquals(Double.valueOf(14.0),val);

		Node n2 = jep.parse("a=x");
		Object val2 = jep.evaluate(n2);
		assertEquals(Double.valueOf(3.0),val2);
		assertEquals(Double.valueOf(3.0),obj.a);

		Node n3 = jep.parse("b=x*4");
		Object val3 = jep.evaluate(n3);
		assertEquals(Double.valueOf(12.0),val3);
		assertEquals(12.0,obj.b,0.01);

	}

	@Test
	public void testMutableDouble() throws Exception {
		Jep jep = new Jep(new BoundVariableFactory());
//		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);
		MutableDouble a = new MutableDouble(5.0);
		MutableDouble b = new MutableDouble(6.0);
		MutableDouble x = new MutableDouble(3);
		FieldVariableBinding accA = new FieldVariableBinding(a,"val");
		FieldVariableBinding accB = new FieldVariableBinding(b,"val");
		FieldVariableBinding accX = new FieldVariableBinding(x,"val");
		
		Variable v1 = jep.addVariable("a", accA);
		Object val1=v1.getValue();
		assertEquals(new Double(5),val1);
		
		Variable v2 = jep.addVariable("b", accB);
		assertEquals(new Double(6),v2.getValue());
		
		Variable v3 = jep.addVariable("x", accX);
		assertEquals(new Double(3),v3.getValue());
		
		Node n = jep.parse("x+a+b");
		Object val = jep.evaluate(n);
		assertEquals(Double.valueOf(14.0),val);

		Node n2 = jep.parse("a=x");
		Object val2 = jep.evaluate(n2);
		assertEquals(Double.valueOf(3.0),val2);
		assertEquals(Double.valueOf(3.0),a.doubleValue(),1e-6);

		Node n3 = jep.parse("b=x*4");
		Object val3 = jep.evaluate(n3);
		assertEquals(Double.valueOf(12.0),val3);
		assertEquals(12.0,b.doubleValue(),1e-6);
		assertEquals(Double.valueOf(12.0),jep.getVariableValue("b"));
		
		a.setValue(7);
		Node n4 = jep.parse("a*4");
		Object val4 = jep.evaluate(n4);
		assertEquals(Double.valueOf(28.0),val4);
		
		jep.setVariable("a", 13.0);
		assertEquals(Double.valueOf(13.0),a.doubleValue(),1e-6);
		
		

	}

	@Test
	public void testObjectVariableFactory() throws Exception {
		ChainedObjectVariableBindingMapper uvm = new ChainedObjectVariableBindingMapper("_");
		BoundVariableFactory bvf = new BoundVariableFactory(uvm);
		Jep jep = new Jep(bvf);
//		((FastEvaluator)jep.getEvaluator()).setTrapUnsetValues(false);

		Variable v3 = jep.addVariable("x", 3.0);
		assertEquals(new Double(3),v3.getValue());
		
		MyObj obj = new MyObj();
		obj.a = new Double(5);
		obj.b = 7;

		uvm.put("Z", obj);
		Node n = jep.parse("x+Z_a");
		Object val = jep.evaluate(n);
		assertEquals(Double.valueOf(8.0),val);
		
		MyObj obj2 = new MyObj();
		obj2.a = new Double(11);
		obj2.b = 13.0;
		obj.c = obj2;
		
		Node n2 = jep.parse("x+Z_c_b");
		Object val2 = jep.evaluate(n2);
		assertEquals(Double.valueOf(16.0),val2);
		
	}
	
	@Test
	public void testDoc() throws Exception {
		// Create Jep with a variable factory which allows bound variables
		Jep jep = new Jep(new BoundVariableFactory());
		// Create an object, with a Double field a and a double field b
		MyObj obj = new MyObj();
		obj.a = new Double(3.0);
		obj.b = 5.0;
		// Create binding objects for the two fields
		FieldVariableBinding bindA = new FieldVariableBinding(obj,"a");
		FieldVariableBinding bindB = new FieldVariableBinding(obj,"b");
		// Create variables bound to these fields
		jep.addVariable("a", bindA);
		jep.addVariable("b", bindB);
//		</pre>
//		<p>
//		These variables can then be used as normal in Jep, and changes will affect
//		the field values.</p>
//		<pre class="codebox">
		// Parse an equation using these values
		Node n = jep.parse("b=4*a");
		Object res = jep.evaluate(n);
		System.out.println("Result is "+res+" value of field b is "+obj.b);
		obj.a = 7.0;
		res = jep.evaluate(n);
		System.out.println("Result is "+res+" value of field b is "+obj.b);

	}
}
