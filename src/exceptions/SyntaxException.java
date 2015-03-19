package exceptions;


public class SyntaxException extends Throwable {
	
	/**
	 * Get rid of warnings.
	 */
	private static final long serialVersionUID = 1L;

	public SyntaxException(int lineNum, String expected, String found, boolean debug){	
		System.out.println("Syntax Error on line " + lineNum + ", Expected: " + expected + " but found: " + found);
	}
	
	public SyntaxException(Integer lineNumber, String msg){
		System.out.println("Syntax Error on Line " + lineNumber + ": " + msg);
	}
}
