package lexical_analysis;

import java.util.Arrays;

import exceptions.SyntaxException;

public class LexScanner {
	private MyScanner s;
//	private List<Lexeme> l;
//	private int lineNum;

	public LexScanner(String filename){
		s = new MyScanner(filename);
	}
	
	public int getLineNum(){
		return s.getLineNum();
	}
	
//	public List<Lexeme> getLexemes(){
//		return l;
//	}
	
	public Lexeme lex(){
		skipWhiteSpace();
		char c = s.readChar();
		switch (c){
		case MyScanner.EOF: //
			return new Lexeme(Lexeme.Type.EOF,"EOF",getLineNum());
		case ';':
			return new Lexeme(Lexeme.Type.SEMI, ";",getLineNum());
		case ':':
			return new Lexeme(Lexeme.Type.COLON, ":",getLineNum());
		case '(':
			return new Lexeme(Lexeme.Type.OPAREN, "(",getLineNum());
		case ')':
			return new Lexeme(Lexeme.Type.CPAREN, ")",getLineNum());
		case '[':
			return new Lexeme(Lexeme.Type.OBRACKET, "[",getLineNum());
		case ']':
			return new Lexeme(Lexeme.Type.CBRACKET, "]",getLineNum());
		case '{':
			return new Lexeme(Lexeme.Type.OBRACE, "{",getLineNum());
		case '}':
			return new Lexeme(Lexeme.Type.CBRACE, "}",getLineNum());
		case '.':
			return new Lexeme(Lexeme.Type.DOT, ".",getLineNum());
		case ',':
			return new Lexeme(Lexeme.Type.COMMA, ",",getLineNum());
		case '+':
			return new Lexeme(Lexeme.Type.PLUS, "+",getLineNum());
		case '-':
			return new Lexeme(Lexeme.Type.MINUS, "-",getLineNum());
		case '*':
			return new Lexeme(Lexeme.Type.MULT, "*",getLineNum());
		case '/':
			return new Lexeme(Lexeme.Type.DIV, "/",getLineNum());
		case '^':
			return new Lexeme(Lexeme.Type.EXP, "^",getLineNum());
		case '>':
			return new Lexeme(Lexeme.Type.GT, ">",getLineNum());
		case '<':
			return new Lexeme(Lexeme.Type.LT, "<",getLineNum());
		case '=':
			return new Lexeme(Lexeme.Type.EQUALS, "=",getLineNum());
		case '\"':
			return stringIdentifier();
		default:
			Lexeme t = null;
			if (Character.isDigit(c)){
				t = numIdentifier(c);
			}
			else{
				try{
					t = idIdentifier(c);
				}
				catch (SyntaxException e){
//					e.printStackTrace();
					System.exit(1);
				}
			}	
			return t;
		}
	}
	
	private Lexeme stringIdentifier(){
		char c = s.readChar();
		StringBuilder sb = new StringBuilder();
		while (c != '\"'){
			sb.append(c);
			c = s.readChar();
		}
		return new Lexeme(Lexeme.Type.STRING, sb.toString(),getLineNum());
	}
	
	private Lexeme numIdentifier(char c){
		StringBuilder sb = new StringBuilder(Character.toString(c));
		c = s.readChar();
		while (Character.isDigit(c)){
			sb.append(c);
			c = s.readChar();
		}
		s.putBackChar(c);
		return new Lexeme(Lexeme.Type.INTEGER, Integer.parseInt(sb.toString()),getLineNum());
	}
	
	private Lexeme idIdentifier(char c) throws SyntaxException{
		if (c != '_' && !Character.isLetter(c)){
			throw new SyntaxException(getLineNum(),"Invalid ID Name");
		}
		StringBuilder sb = new StringBuilder(Character.toString(c));
//		System.out.println(sb.toString());
		c = s.readChar();
		while (Character.isLetterOrDigit(c) || c == '_'){
			sb.append(c);
			c = s.readChar();
		}
		s.putBackChar(c);
		return new Lexeme(getType(sb.toString()), sb.toString(),getLineNum());
	}
	
	private void skipComments(char c){
		if (s.peekChar() == '/'){ //Check for block comment
			s.readChar(); 			//Throw away '/'
			c = s.readChar(); 		//Read next comment char
			while (c != '/' || s.peekChar() != '#'){ //While not the end block comment identifier...
				if (c=='\n'){
//					lineNum++;
				}
				c = s.readChar();   //Read char
			}
			s.readChar(); //Throw away # sign
		}
		else{ //Else is inline comment
			while (c != '\n'){//while we're not at end of line...
				c = s.readChar(); //throw away chars
			}
//			lineNum++;
		}
	}
	
	private void skipWhiteSpace(){
		char c = s.readChar();
		while (c ==' ' || c =='\n' || c == '\t' || c =='#'){ //Throw away spaces and tabs and newlines
			if (c == '\n'){
//				lineNum++;
			}
			if (c == '#'){ //If char is # or comment identifier
				skipComments(c);
			}
			c = s.readChar();	
		}
		s.putBackChar(c);
	}
	
	private Lexeme.Type getType(String str){
		if (Arrays.asList(Lexeme.keywords).contains(str)){
			switch (str){
			case "return":
				return Lexeme.Type.RETURN;
			case "Main":
				return Lexeme.Type.MAIN;
			case "loop":
				return Lexeme.Type.LOOP;
			case "run":
				return Lexeme.Type.RUN;
			case "TRUE":
				return Lexeme.Type.TRUE;
			case "FALSE":
				return Lexeme.Type.FALSE;
			case "func":
				return Lexeme.Type.FUNC;
			case "AND":
				return Lexeme.Type.AND;
			case "var":
				return Lexeme.Type.VAR;
			case "kbl":
				return Lexeme.Type.KBL;
			case "ml":
				return Lexeme.Type.ML;
			case "os":
				return Lexeme.Type.OS;
			case "if":
				return Lexeme.Type.IF;
			case "else":
				return Lexeme.Type.ELSE;
			case "NOT":
				return Lexeme.Type.NOT;
			case "GET":
				return Lexeme.Type.GET;
			case "SET":
				return Lexeme.Type.SET;
			case "GETNDX":
				return Lexeme.Type.GETNDX;
			case "SETNDX":
				return Lexeme.Type.SETNDX;
			case "KEYPRESS":
				return Lexeme.Type.KEYPRESS;
			case "KEYDOWN":
				return Lexeme.Type.KEYDOWN;
			case "KEYUP":
				return Lexeme.Type.KEYUP;
			case "ONCHANGE":
				return Lexeme.Type.ONCHANGE;
			case "ONMOVE":
				return Lexeme.Type.ONMOVE;
			case "ONCLICK":
				return Lexeme.Type.ONCLICK;
			case "while":
				return Lexeme.Type.WHILE;
			case "NULL":
				return Lexeme.Type.NULL;
			default:
				return Lexeme.Type.OR;
			}
		}
		else{
			return Lexeme.Type.ID;
		}
	}
	
	public static void main(String[] args){
		if (args.length < 1){
			System.out.println("No File Supplied: Exiting...");
			System.exit(1);
		}
		LexScanner s = new LexScanner(args[0]);
		Lexeme l = s.lex();
		while (l.type != Lexeme.Type.EOF){
			System.out.println(l);
			l = s.lex();
		}
	}
	

}
