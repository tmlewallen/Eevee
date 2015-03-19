package recognizer;
import environment.Environment;


public class Main {
	public static void main(String[] args){
		Recognizer r = new Recognizer("test.ev");
		Parser p = new Parser(r.getTree(),new Environment());
	}
}
