import com.singularsys.jep.*;

/**
 * An example class to test custom functions with Jep.
 */
class CustFunc {
	
	/**
	 * Constructor.
	 */
	public CustFunc() {

	}

	/**
	 * Main method. Create a new Jep object and parse an example expression
	 * that uses the SquareRoot function.
	 */
	public static void main(String args[]) {
		
		Jep parser = new Jep();        // Create a new parser
		String expr = "1 + half(2)";
		Object value;
		
		System.out.println("Starting CustFunc...");
		// Add the custom function
		parser.getFunctionTable().addFunction("half", new Half());
		
		try {
			parser.parse(expr);                 // Parse the expression
		} catch (ParseException e) {
			System.out.println("Error while parsing");
			System.out.println(e.getMessage());
			return;
		}
		
		try {
			value = parser.evaluate();                    // Get the value
		} catch (EvaluationException e) {
			System.out.println("Error during evaluation");
			System.out.println(e.getMessage());
			return;
		}
		
		System.out.println(expr + " = " + value); // Print value
	}
}
