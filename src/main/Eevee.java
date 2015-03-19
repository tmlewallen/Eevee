package main;
import recognizer.*;
import environment.Environment;

public class Eevee {
	
	
	public static void main(String[] args){
		if (args.length < 1){
			System.out.println("No File Supplied: Exiting...");
			System.exit(1);
		}
		Recognizer r = new Recognizer("main.lib"); //Parse main.lib for build in functions and such. 
		Parser p = new Parser(r.getTree(),new Environment());
		Environment e = p.getEnvironment();
		r = new Recognizer(args[0]);
		p = new Parser(r.getTree(),e);
	}
}
