package recognizer;

import lexical_analysis.*;
import exceptions.*;

 
public class Recognizer {
	Lexeme currentLex;
	LexScanner scanner;
	Lexeme tree;
	
	public Recognizer(String filename){
		scanner = new LexScanner(filename);
		currentLex = scanner.lex();
		tree = program();
	}
	
	public Lexeme getTree(){
		return tree;
	}
	
	public void traverseTree(Lexeme node){
		if (node == null){
			return;
		}
		else{
			System.out.println(node);
			if (node.getLeft() != null){System.out.println("\tLeft: " + node.getLeft());}
			else{System.out.println("\tLeft: NULL");}
			if (node.getRight() != null){System.out.println("\tRight: " + node.getRight());}
			else{System.out.println("\tRight: NULL");}
			traverseTree(node.getLeft());
			traverseTree(node.getRight());
		}
	}
	
	private boolean check(Lexeme.Type type){
		return currentLex.type == type;
	}
	
	private void advance(){
		currentLex = scanner.lex();
//		System.out.println(currentLex);
	}
	
	private Lexeme match(Lexeme.Type type){
		Lexeme t;
		try{
			matchNoAdvance(type);
		}
		catch(SyntaxException e){
//			e.printStackTrace();
			System.out.println("Illegal Expression. Exiting...");
			System.exit(1);
		}
		t = currentLex;
		advance();
		return t;
	}
	
	private void matchNoAdvance(Lexeme.Type type) throws SyntaxException{
		if (!check(type)){
//			System.out.println("LEX: " + currentLex);
			throw new SyntaxException(scanner.getLineNum(), Lexeme.typeToString(type),Lexeme.typeToString(currentLex.type),true);
		}
	}
	
	private Lexeme program(){
		return definition();
	}
	
	private Lexeme definition(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(optionalObjDeclList());
		G.setRight(mainDef());
		return G;
	}
	
	private Lexeme optionalObjDeclList(){
		if (objDeclListPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(objDeclList());
			return G;
		}
		return new Lexeme(Lexeme.Type.NULL);
	}
	private boolean objDeclListPending() {
		return objDeclPending();
	}

	private boolean objDeclPending() {
		return typeIdPending();
	}

	private boolean typeIdPending() {
		return check(Lexeme.Type.VAR) || check(Lexeme.Type.KBL) || check(Lexeme.Type.ML) || check(Lexeme.Type.OS);
	}

	private Lexeme mainDef(){
		Lexeme L = match(Lexeme.Type.MAIN);
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.DOT));
		rest = rest.setAndAdvanceRight(runOption());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OPAREN));
		rest = rest.setAndAdvanceRight(optionalParamList());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CPAREN));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OBRACE));
		rest = rest.setAndAdvanceRight(optionalCodeBlock());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		return L;
	}
	
	private Lexeme objDeclList(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(objDecl());
		if (objDeclListPending()){ 
			G.setRight(objDeclList());
			}
		return G;
	}
	
	private Lexeme runOption(){
		if (check(Lexeme.Type.LOOP)){return match(Lexeme.Type.LOOP);}
		else{return match(Lexeme.Type.RUN);}
	}
	
	private Lexeme optionalCodeBlock(){
		if (codeBlockPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);	
			G.setLeft(codeBlock());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean codeBlockPending() {
		return statementPending();
	}

	private boolean statementPending() {
		return exprPending() || variablePending() || functionPending() || objDeclPending() || conditionalPending() || whileLoopPending();
	}
	
	private boolean whileLoopPending(){
		return check(Lexeme.Type.WHILE);
	}

	private Lexeme whileLoop(){
		Lexeme L = match(Lexeme.Type.WHILE);
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.OPAREN));
		rest = rest.setAndAdvanceRight(new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(predicate()));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CPAREN));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OBRACE));
		rest = rest.setAndAdvanceRight(optionalCodeBlock());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		return L;
	}
	
	private boolean conditionalPending() {
		return check(Lexeme.Type.IF);
	}

	private Lexeme objDecl(){
		Lexeme L = typeId();
		Lexeme rest = L.setAndAdvanceRight(variable());
		rest.setID_type(L.type);
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.EQUALS));
		rest = rest.setAndAdvanceRight(optionalInitilization());
		rest = rest.setAndAdvanceRight(optionalObjDef());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.SEMI));
		return L;
	}
	
	private Lexeme codeBlock(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(statement());
		if (codeBlockPending()){G.setRight(codeBlock());}
		return G;
	}
	
	private Lexeme typeId(){
		if (check (Lexeme.Type.VAR)){return match(Lexeme.Type.VAR);}
		else if (check(Lexeme.Type.KBL)){return match(Lexeme.Type.KBL);}
		else if (check(Lexeme.Type.ML )){return match(Lexeme.Type.ML);}
		else{return match(Lexeme.Type.OS);}
	}
	private Lexeme variable(){ //UNSURE
		return match(Lexeme.Type.ID);
	}
	
	private Lexeme optionalInitilization(){
		if (exprPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(expr());
			return G;
			}
		else {return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean variablePending() {
		return check(Lexeme.Type.ID);
	}

	private boolean literalPending() {
		return check(Lexeme.Type.INTEGER) || check(Lexeme.Type.STRING) || check(Lexeme.Type.TRUE) || check(Lexeme.Type.FALSE) || check(Lexeme.Type.NULL);
	}

	private Lexeme optionalObjDef(){
		if (objDefPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(objDef());
			return G;
			}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean objDefPending() {
		return check(Lexeme.Type.OBRACE);
	}

	private Lexeme statement(){
		Lexeme L;
		if (exprPending()){
			L = new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(expr());
			L.setRight(match(Lexeme.Type.SEMI));
			return L;
		}
		else if (functionPending()){
			L = function();
			L.setRight(match(Lexeme.Type.SEMI));
			return L;
		}
		else if (objDeclPending()){
			L = new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(objDecl());
			return L;
		}
		else if (whileLoopPending()){
			L = whileLoop();
			return L;
		}
		else{
			L = conditional();
			return L;
		}
	}
	
	private boolean exprPending() {
		return operandPending();
	}

	private Lexeme literal(){
		if (check(Lexeme.Type.INTEGER)){return match(Lexeme.Type.INTEGER);}
		else if (check(Lexeme.Type.STRING)){return match(Lexeme.Type.STRING);}
		else if (check(Lexeme.Type.TRUE)){return match(Lexeme.Type.TRUE);}
		else if (check(Lexeme.Type.NULL)){return match(Lexeme.Type.NULL);}
		else {return match(Lexeme.Type.FALSE);}
	}
	
	private Lexeme objDef(){
		Lexeme L = match(Lexeme.Type.OBRACE);
		Lexeme rest = L.setAndAdvanceRight(optionalObjDeclList());
		rest = rest.setAndAdvanceRight(optionalOverrideList());
		rest = rest.setAndAdvanceRight(optionalHelperList());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		return L;
	}
	
	private Lexeme expr(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		Lexeme L = G.setAndAdvanceLeft(operand());
		if (operatorPending()){
			L.setRight(operator());
			G.setRight(expr());
		}
		else if (check(Lexeme.Type.OPAREN)){
			Lexeme args = L.setAndAdvanceLeft(match(Lexeme.Type.OPAREN));
			args = args.setAndAdvanceRight(optionalArgList());
			args.setRight(match(Lexeme.Type.CPAREN));
			if (operatorPending()){
				L.setRight(operator());
				G.setRight(expr());
			}
		}
		else if (check(Lexeme.Type.OBRACKET)){
			Lexeme index = L.setAndAdvanceLeft(match(Lexeme.Type.OBRACKET));
			index.setAndAdvanceRight(expr());
			match(Lexeme.Type.CBRACKET);
			if (operatorPending()){
				L.setRight(operator());
				G.setRight(expr());
			}
		}
		return G;
	}
	
	private boolean operatorPending() {
		return check(Lexeme.Type.PLUS) || check(Lexeme.Type.MINUS) || check(Lexeme.Type.MULT) || check(Lexeme.Type.DIV) || check(Lexeme.Type.EXP) || check(Lexeme.Type.EQUALS) ;
	}

	private Lexeme conditional(){
		Lexeme L = match(Lexeme.Type.IF);
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.OPAREN));
		rest = rest.setAndAdvanceRight(new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(predicate()));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CPAREN));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OBRACE));
		rest = rest.setAndAdvanceRight(optionalCodeBlock());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		rest = rest.setAndAdvanceRight(optionalElseClause());
		return L;
	}
	
	private Lexeme optionalOverrideList(){
		if (overrideListPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(overrideList());
			return G;
			}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean overrideListPending() {
		return overridePending();
	}

	private boolean overridePending() {
		return signaturePending();
	}

	private boolean signaturePending() {
		return var_sigPending() || kbl_sigPending() || ml_sigPending() || os_sigPending();
	}

	private boolean os_sigPending() {
		return check(Lexeme.Type.ONCHANGE);
	}

	private Lexeme optionalHelperList(){
		if (helperListPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(helperList());
			return G;
			}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean helperListPending() {
		return functionPending();
	}

	private boolean functionPending() {
		return check(Lexeme.Type.FUNC);
	}

	private Lexeme operand(){
		if (variablePending()){return variable();}
		else if (literalPending()){return literal();}
		else if (arrayPending()){return array();}
		else{return functionCall();}
	}
	
	private boolean arrayPending(){
		return check(Lexeme.Type.OBRACKET);
	}
	
	private Lexeme array(){
		Lexeme L = match(Lexeme.Type.OBRACKET);
		Lexeme rest = L.setAndAdvanceLeft(optionalElement());
		rest.setRight(match(Lexeme.Type.CBRACKET));
		return L;
	}
	
	private Lexeme optionalElement(){
		if (elementPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(element());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private Lexeme element(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(expr());
		if (check(Lexeme.Type.COMMA)){
			match(Lexeme.Type.COMMA); //DONT SAVE COMMA, will mess with expr lexeme tree structure
			G.setRight(element());
			}
		return G;
	}
	
	private boolean elementPending(){
		return exprPending();
	}
	
	private Lexeme functionCall() {
		Lexeme L = variable();
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.OPAREN));
		rest = rest.setAndAdvanceRight(optionalArgList());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CPAREN));
		return L;
	}

	private Lexeme optionalArgList() {
		if (argListPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(argList());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}

	private Lexeme argList() {
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
//		Lexeme L;
		if (exprPending()){G.setAndAdvanceLeft(expr());}
		else{G.setAndAdvanceLeft(function());}
		if (check(Lexeme.Type.COMMA)){
			match(Lexeme.Type.COMMA);
			G.setRight(argList());
		}
		return G;
	}

	private boolean argListPending() {
		return exprPending() || functionPending();
	}

	private Lexeme operator(){
		if (check(Lexeme.Type.PLUS)){return match(Lexeme.Type.PLUS);}
		else if (check(Lexeme.Type.MINUS)){return match(Lexeme.Type.MINUS);}
		else if (check(Lexeme.Type.MULT)){return match(Lexeme.Type.MULT);}
		else if (check(Lexeme.Type.EQUALS)){return match(Lexeme.Type.EQUALS);}
		else if (check(Lexeme.Type.EXP)){return match(Lexeme.Type.EXP);}
		else {return match(Lexeme.Type.DIV);}
	}
	
	private Lexeme predicate(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		Lexeme L;
		if (operandPending()){
			L = G.setAndAdvanceLeft(operand());
			if (booleanLogicPending()){
				L.setRight(booleanLogic());
				G.setRight(predicate());
			}
			else if(comparisonLogicPending()){
				L.setRight(comparisonLogic());
				G.setRight(predicate());
			}
		}
		else {
			L = G.setAndAdvanceLeft(match(Lexeme.Type.NOT));
			G.setRight(predicate());
		}
		return G;
	}
	
	private boolean comparisonLogicPending() {
		return check(Lexeme.Type.EQUALS) || check (Lexeme.Type.LT) || check(Lexeme.Type.GT);
	}

//	private boolean predicatePending() {
//		return operandPending() || check(Lexeme.Type.NOT);
//	}

	private boolean booleanLogicPending() {
		return check(Lexeme.Type.AND) || check(Lexeme.Type.OR);
	}

	private boolean operandPending() {
		return variablePending() || literalPending() || arrayPending(); //functionCallPending never reached...
	}

	private Lexeme optionalElseClause(){
		if (elseClausePending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(elseClause());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean elseClausePending() {
		return check(Lexeme.Type.ELSE);
	}

	private Lexeme overrideList(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(override());
		if (overrideListPending()){
			G.setRight(overrideList());
		}
		return G;
	}
	
	private Lexeme helperList(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		G.setLeft(function());
		if (check(Lexeme.Type.COMMA)){
			match(Lexeme.Type.COMMA);
			G.setRight(helperList());
		}
		return G;
	}
	
	private Lexeme booleanLogic(){
		if (check(Lexeme.Type.AND)){return match(Lexeme.Type.AND);}
		else{return match(Lexeme.Type.OR);}
	}
	
	private Lexeme elseClause(){
		Lexeme L = match(Lexeme.Type.ELSE);
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.OBRACE));
		rest = rest.setAndAdvanceRight(optionalCodeBlock());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		return L;
	}
	
	private Lexeme override(){
		Lexeme L = signature();
		Lexeme rest = L.setAndAdvanceRight(match(Lexeme.Type.COLON));
		rest = rest.setAndAdvanceRight(function());
		match(Lexeme.Type.SEMI); //Hack for better dealing with overrides/functions THROW AWAY SEMI
		return L;
	}
	
	private Lexeme function(){
		Lexeme G = new Lexeme(Lexeme.Type.GLUE);
		Lexeme L = G.setAndAdvanceLeft(match(Lexeme.Type.FUNC));
		Lexeme rest;
		if (variablePending()){rest = L.setAndAdvanceRight(variable());}
		else{rest = L.setAndAdvanceRight(new Lexeme(Lexeme.Type.NULL));}
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OPAREN));
		rest = rest.setAndAdvanceRight(optionalParamList());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CPAREN));
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.OBRACE));
		rest = rest.setAndAdvanceRight(optionalCodeBlock());
		rest = rest.setAndAdvanceRight(optionalReturnStatement());
		rest = rest.setAndAdvanceRight(match(Lexeme.Type.CBRACE));
		return G;
	}
	
	private Lexeme optionalReturnStatement() {
		if (returnStatementPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(returnStatement());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}

	private Lexeme returnStatement() {
		Lexeme L = match(Lexeme.Type.RETURN);
		L.setRight(expr());
//		rest = rest.setAndAdvanceRight(match(Lexeme.Type.SEMI)); HACK to make 
		match(Lexeme.Type.SEMI);// Hack to not save SEMI so L.right can be used in expr() in parser... hope that makes sense later
		return L;
		
	}

	private boolean returnStatementPending() {
		return check(Lexeme.Type.RETURN);
	}

	private Lexeme comparisonLogic(){
		Lexeme L;
		if (check(Lexeme.Type.EQUALS)){
			L = match(Lexeme.Type.EQUALS);
			L.setRight(match(Lexeme.Type.EQUALS));
		}
		else if (check(Lexeme.Type.GT)){
			L = match(Lexeme.Type.GT);
			if (check(Lexeme.Type.EQUALS)){
				L.setRight(match(Lexeme.Type.EQUALS));
			}
		}
		else{
			L = match(Lexeme.Type.LT);
			if (check(Lexeme.Type.EQUALS)){
				L.setRight(match(Lexeme.Type.EQUALS));
			}
		}
		return L;
	}
	
	private Lexeme signature(){
		if (var_sigPending()){return var_sig();}
		else if (kbl_sigPending()){return kbl_sig();}
		else if (ml_sigPending()){return ml_sig();}
		else {return os_sig();}
	}
	
	private boolean kbl_sigPending() {
		return check(Lexeme.Type.KEYDOWN) || check(Lexeme.Type.KEYUP) || check(Lexeme.Type.KEYPRESS);
	}

	private boolean ml_sigPending() {
		return check(Lexeme.Type.ONMOVE) || check(Lexeme.Type.ONCLICK);
	}


	private boolean var_sigPending() { //ON CHANGE sitll not implemented... May not be useful
		return /*check(Lexeme.Type.ONCHANGE)||*/ check(Lexeme.Type.GET) || check(Lexeme.Type.SET) || check(Lexeme.Type.GETNDX) || check(Lexeme.Type.SETNDX);
	}

	private Lexeme optionalParamList(){
		if (paramListPending()){
			Lexeme G = new Lexeme(Lexeme.Type.GLUE);
			G.setLeft(paramList());
			return G;
		}
		else{return new Lexeme(Lexeme.Type.NULL);}
	}
	
	private boolean paramListPending() {
		return variablePending();
	}

	private Lexeme var_sig(){
		if (check(Lexeme.Type.ONCHANGE)){return match(Lexeme.Type.ONCHANGE);}
		else if (check(Lexeme.Type.GET)){return match(Lexeme.Type.GET);}
		else if (check(Lexeme.Type.GETNDX)){return match(Lexeme.Type.GETNDX);}
		else if (check(Lexeme.Type.SETNDX)){return match(Lexeme.Type.SETNDX);}
		else {return match(Lexeme.Type.SET);}
	}
	
	private Lexeme kbl_sig(){
		if (check(Lexeme.Type.KEYPRESS)){return match(Lexeme.Type.KEYPRESS);}
		else if (check(Lexeme.Type.KEYDOWN)){return match(Lexeme.Type.KEYPRESS);}
		else {return match(Lexeme.Type.KEYUP);}	
	}
	
	private Lexeme ml_sig(){
		if (check(Lexeme.Type.ONMOVE)){return match(Lexeme.Type.ONMOVE);}
		else{ return match(Lexeme.Type.ONCLICK);}
	}
	
	private Lexeme os_sig(){
		return match(Lexeme.Type.ONCHANGE);
	}
	
	private Lexeme paramList(){
		Lexeme L = variable();
		if (check(Lexeme.Type.COMMA)){
			L.setRight(match(Lexeme.Type.COMMA));
			L.getRight().setRight(paramList());
		}
		return L;
	}
	
	public static void main(String[] args){
		if (args.length < 1){
			System.out.println("No File Supplied: Exiting...");
			System.exit(1);
		}
		Recognizer r = new Recognizer(args[0]);
		System.out.println("All expressions are legal!");
	}
}


