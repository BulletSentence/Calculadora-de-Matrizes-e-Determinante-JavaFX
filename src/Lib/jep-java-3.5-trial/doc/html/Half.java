import java.util.*;
import com.singularsys.jep.*;
import com.singularsys.jep.functions.*;

/**
 * An example custom function class for Jep.
 */
class Half extends PostfixMathCommand {

	/**
	 * Constructor. The half function only accepts 1 parameter so the
	 * numberOfParameters value is set accordingly.
	 */
	public Half() {
		numberOfParameters = 1;
	}
	
	/**
	 * Divides the value of the top on the inStack by two. The parameter is 
	 * popped off the <code>inStack</code>, and divided by two. The final value 
	 * is pushed back to the top of <code>inStack</code>.
	 * <p>
	 * This function only accepts Double types.
	 */
	public void run(Stack<Object> inStack) throws EvaluationException {

		// check the stack
		checkStack(inStack);

		// get the parameter from the stack
		Object param = inStack.pop();

		// check whether the argument is of the right type
		if (param instanceof Double) {
			// calculate the result
			double r = ((Double)param).doubleValue() / 2;
			// push the result on the inStack
			inStack.push(new Double(r));
		} else {
			throw new EvaluationException("Invalid parameter type");
		}
	}
}
