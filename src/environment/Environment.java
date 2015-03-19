package environment;

import lexical_analysis.Lexeme;

public class Environment {
	private Environment parent;
	private Lexeme tail;
	private Lexeme head;
	
	
	
	public Environment(){
		parent = null;
		tail = null;
		head = null;
	}
	
	public Environment(Environment parent){
		this.parent = parent;
		tail = null;
		head = null;
	}
	
	public boolean exists(Lexeme key){
		Lexeme k = getFromThisEnv(key); //Try to get from this env
		return k != null;
	}
	
	
	public Lexeme insert(Lexeme key, Lexeme val){
		key = key.clone(); //Clones stuff to prevent having to deal with pointers
		val = val.cloneTree();
		if (getFromThisEnv(key) != null){
			FatalError_Environment("Duplicate Declaration of Variable " + key.sval, key.getLineNumber());
		}
		
		if (key.getID_type() == Lexeme.Type.OS){
//			System.out.println("OS");
			System.out.println(val);
		}
		
		if (head == null){
			head = key;
			key.setLeft(val);
			tail = key;
			key.setRight(null);
		}
		else{
			tail.setRight(key);
			tail = tail.getRight();
			tail.setLeft(val);
			key.setRight(null);
		}
//		System.out.println(this);
		return val;
	}
	
	public Lexeme update(Lexeme key, Lexeme val){
		Lexeme k = getKey(key);
		if (k != null){
			if (k.getID_type() == Lexeme.Type.OS){
//				System.out.println("OS");
				System.out.println(val);
			}
			k.setLeft(val);
			}//Errors?
//		System.out.println(this);
		return val;
	}
	
	public Lexeme updateNdx(Lexeme key, Lexeme val, Integer ndx){
		Lexeme element = getNdx(key,ndx);
		//Just gonna copy important from new value to old value... 
		element.type = val.type;
		element.ival = val.ival;
		element.sval = val.sval;
		if (val.getLeft() != null){
			element.setLeft(val.getLeft().cloneTree());
		}
		return element;
	}
	
	public Lexeme getNdx(Lexeme key, Integer ndx){
		Lexeme arr = get(key);
		if (arr.type != Lexeme.Type.OBRACKET){
			FatalError_Environment("Attempted to get index of type " + Lexeme.typeToString(key.type) + ". Dont do that.", key.getLineNumber());
		}
		Lexeme element = arr.getLeft(); //Skip OBRACKET
		if (element == null){
			FatalError_Environment("Attempted to index an empty array",key.getLineNumber());
		}
		for (Integer i = 0; i < ndx; i++){
			if (element != null){
				element = element.getRight();
			}
			else{
				FatalError_Environment("Array indexed out of range", key.getLineNumber());
			}
		}
		if (element== null){
			return new Lexeme(Lexeme.Type.NULL);
		}
		return element.getLeft();
	}
	
	
	
	public Lexeme get(Lexeme key){
		Lexeme k = getFromThisEnv(key); //Try to get from this env
		if (k== null){ //If not in this env...
			if (parent != null){ //Try to get from parent
				k = parent.get(key);
			}
			if (k == null){ //If k is still null after parent check (if no parent, will still be null)
				FatalError_Environment("Unrecognized identifier: "+key.sval,key.getLineNumber());
			}
			return k;
		}
		else{
			return k;
		}
	}
	
	public Environment getEnv(Lexeme key){
		if (getFromThisEnv(key) != null){
			return this;
		}
		else if (parent != null){
			return parent.getEnv(key);
		}
		FatalError_Environment("Unrecognized identifier: "+key.sval,key.getLineNumber());
		return null;
	}
	
	public String thisEnvToString(){
		return toString();
	}
	
	public String allEnvToString(){
		String env = toString();
		if (parent != null){
			env = env + parent.allEnvToString();
		}
		return env;
	}
	
	public String toString(){
		if (head == null){
			return "";
		}
		StringBuilder sb = new StringBuilder("ENVIRONMENT: " + java.lang.System.identityHashCode(this) + "\n\t");
		Lexeme curr = head;
		while (curr != null){
			sb.append(curr.toString());
			sb.append(": ");
			sb.append(curr.getLeft().toString());
			curr = curr.getRight();
			sb.append("\n\t");
		}
		return sb.toString();
	}
	
	private Lexeme getFromThisEnv(Lexeme key){
		Lexeme curr = head;
		while (curr != null){
			if (curr.sval.equals(key.sval)){
				return curr.getLeft();
			}
			curr = curr.getRight();
		}
		return null;
	}
	
	public Lexeme getKey(Lexeme key){
		Lexeme curr = head;
		while (curr != null){
			if (curr.sval.equals(key.sval)){
				return curr;
			}
			curr = curr.getRight();
		}
		if (parent != null){
			curr = parent.getKey(key);
		}
		if (curr == null){
			FatalError_Environment("Unrecognized Identifier: "+ key.sval,key.getLineNumber());
		}
		return curr;
	}
	
	private void FatalError_Environment(String msg,Integer line){
		System.out.println("FATAL ERROR in ENVIRONMENT on line: " + line + " - " + msg );
		System.exit(1);
	}
	
	public static void main(String[] args){
		System.out.println("Creating new environment...");
		Environment e = new Environment();
		System.out.println("Environment is... \n" + e);
		System.out.println("Adding variable x with value 3...");
		e.insert(new Lexeme(Lexeme.Type.ID,"x",0), new Lexeme(Lexeme.Type.INTEGER,3,0));
		System.out.println("Environment is...\n" + e);
		System.out.println("Creating new environment that is linked to previous environment...");
		Environment e1 = new Environment(e);
		System.out.println("Adding variable y with value 54 to new environment...");
		e1.insert(new Lexeme(Lexeme.Type.ID,"y",0),new Lexeme(Lexeme.Type.INTEGER,54,0));
		System.out.println("The local environement is... " + e1);
		System.out.println("The local->global environements are...\n" + e1.allEnvToString());
		System.out.println("Creating ANOTHER new environment that is linked to the previous environment...");
		Environment e2 = new Environment(e1);
		System.out.println("Adding variable z with value \"Thomas\" to local environment");
		e2.insert(new Lexeme(Lexeme.Type.ID,"z",0), new Lexeme(Lexeme.Type.STRING,"Thomas",0));
		System.out.println("The local environement is... " + e2);
		System.out.println("The local->global environements are...\n" + e2.allEnvToString());
		System.out.println("Updating variable x to new value, \"NEW\"");
		e2.update(new Lexeme(Lexeme.Type.ID,"x",0),new Lexeme(Lexeme.Type.STRING,"NEW",0));
		System.out.println("The local environement is... " + e2);
		System.out.println("The local->global environements are...\n" + e2.allEnvToString());
		System.out.println("Exiting...");
	}
	
}
