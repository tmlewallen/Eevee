package lexical_analysis;
import environment.Environment;

public class Lexeme {
	public Type type;
	public String sval;
	public Integer ival;
	private Integer lineNumber;
	private Type ID_type; //Hack to save types of vars
	private Environment objEnv = null;
	private  Lexeme left,right;
	
	
	public static enum Type {SEMI, COLON, OPAREN, CPAREN, OBRACKET, CBRACKET, OBRACE, CBRACE, 
											DOT, INTEGER, STRING, COMMA, 
											PLUS, MINUS, MULT, DIV, EXP, EQUALS, GT, LT, ID, EOF,
											MAIN, LOOP, RUN, TRUE, FALSE, AND, OR, FUNC, VAR, KBL, ML, OS, IF, ELSE, NOT,
											ONCHANGE, GET, SET, GETNDX, SETNDX, KEYPRESS, KEYDOWN, KEYUP, ONMOVE, ONCLICK, RETURN, GLUE, NULL,WHILE};
											
	public static final String[] keywords = {"Main", "loop", "run", "TRUE", "FALSE", "AND", "OR", "func", "var", "kbl", "ml", "os", "if", "else", "NOT", "var[]","GET", "SET","return","while","GETNDX","SETNDX","NULL" };
	
	public Lexeme(Type type){ //FOR GLUE/NULL
		this.type = type;
		left = null;
		right = null;
	}
			
	public Lexeme(Type type, String sval, Integer lineNum){
		this.type = type;
		this.sval = sval;
		this.lineNumber = lineNum;
		left = null;
		right = null;
	}
	
	public Lexeme(Type  type, Integer ival, Integer lineNum){
		this.type = type;
		this.ival = ival;
		this.lineNumber = lineNum;
		right = null;
		left = null;
	}
	
	private Lexeme(Type type,Type ID_type, String sval, Integer ival, Integer lineNum, Environment objEnv){
		this.type = type;
		this.ID_type = ID_type;
		this.sval = sval;
		this.ival = ival;
		this.lineNumber = lineNum;
		this.objEnv = objEnv;
	}
	
	public void setObjEnv(Environment e){
		objEnv = e;
	}
	
	public Environment getObjEnv(){
		return objEnv;
	}
	
	public Type getID_type(){
		return ID_type;
	}
	
	public void setID_type(Type t){
		ID_type = t;
	}
	
	public Integer getLineNumber(){
		return lineNumber;
	}
	
	public boolean isNull(){
		return type==Type.NULL;
	}
	
	public Lexeme getLeft(){
		return left;
	}
	
	public Lexeme getRight(){
		return right;
	}
	
	public void setLeft(Lexeme l){
		this.left = l;
	}
	
	public void setRight(Lexeme r){
		this.right = r;
	}
	
	public Lexeme setLeftReturnSelf(Lexeme l){
		this.left = l;
		return this;
	}
	
	public Lexeme setAndAdvanceLeft(Lexeme l){
		this.left = l;
		return l;
	}
	
	public Lexeme setAndAdvanceRight(Lexeme r){
		this.right = r;
		return r;
	}
	
	public String toString(){
		if (type == Type.INTEGER){
			return Integer.toString(ival);
		}
		else if (type == Type.STRING){
//			return "\"" + sval + "\"";
			return sval;
		}
		else if (type == Type.NULL){
			return "NULL";
		}
		else if (type == Type.GLUE){
			return "GLUE: " + java.lang.System.identityHashCode(this);
		}
		else if (type == Type.ID){
			return  sval;
		}
		else{
			return typeToString(type);
		}
	}
	
	public Lexeme clone(){
		return new Lexeme(type,ID_type,sval,ival,lineNumber,objEnv);
	}
	
	public Lexeme cloneTree(){ //Clones a Lexeme tree for use with function calls
		Lexeme c = this.clone();
		if (this.right != null){
			c.setRight(right.cloneTree());
		}
		if (left != null){
			c.setLeft(left.cloneTree());
		}
		return c;
	}
	
	
	public static String typeToString(Lexeme.Type t){
		switch (t){
		case RETURN:
			return "return";
		case EOF:
			return "@";
		case LT:
			return "<";
		case GT:
			return ">";
		case EQUALS:
			return "=";
		case EXP:
			return "^";
		case DIV:
			return "/";
		case MINUS:
			return "-";
		case PLUS:
			return "+";
		case COMMA:
			return ",";
		case STRING:
			return "STRING";
		case SEMI:
			return ";";
		case COLON:
			return ":";
		case OPAREN:
			return "(";
		case CPAREN:
			return ")";
		case OBRACKET:
			return "[";
		case CBRACKET:
			return "]";
		case OBRACE:
			return "{";
		case CBRACE:
			return "}";
		case DOT:
			return ".";
		case INTEGER:
			return "INT";
		case MAIN:
			return "Main";
		case LOOP:
			return "loop";
		case RUN:
			return "run";
		case TRUE:
			return "TRUE";
		case FALSE:
			return "FALSE";
		case FUNC:
			return "func";
		case AND:
			return "AND";
		case VAR:
			return "var";
		case KBL:
			return "kbl";
		case ML:
			return "ml";
		case OS:
			return "os";
		case IF:
			return "if";
		case ELSE:
			return "else";
		case NOT:
			return "NOT";
		case GET:
			return "GET";
		case SET:
			return "SET";
		case GETNDX:
			return "GETNDX";
		case SETNDX:
			return "SETNDX";
		case KEYPRESS:
			return "KEYPRESS";
		case KEYDOWN:
			return "KEYDOWN";
		case KEYUP:
			return "KEYUP";
		case ONCHANGE:
			return "ONCHANGE";
		case ONMOVE:
			return "ONMOVE";
		case ONCLICK:
			return "ONCLICK";
		case ID:
			return "ID";
		case WHILE:
			return "while";
		default:
			return "OR";
		}
	}
	
}	
