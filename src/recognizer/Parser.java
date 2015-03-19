package recognizer;
import lexical_analysis.Lexeme;
import environment.Environment;

public class Parser {
	Lexeme tree;
	Environment e;
	
	
	public Parser (Lexeme tree,Environment E){
		this.tree = tree;
		e = E;
		getObjDecl(e,tree.getLeft().getLeft());
		getMainDef(e,tree.getRight());

	}
	
	public Environment getEnvironment(){
		return e;
	}
	
	public void getObjDecl(Environment e,Lexeme l){
		if (l != null){
			objDecl(e,l.getLeft());
			getObjDecl(e,l.getRight());
		}
	}
	
	public void objDecl(Environment e, Lexeme l){
		if (l == null){
			return;
		}
		Lexeme id = l.getRight();
		Lexeme arg = id.getRight().getRight().getLeft();
		Lexeme objDecl = id.getRight().getRight().getRight().getLeft();
		Lexeme val = expr(e,arg);
//		System.out.println("SETTING ENV FOR VAR " + id);
		id.setObjEnv(initObjEnv(e,objDecl));
		e.insert(id, val);
//		System.out.println(e);
	}
	
	public void getMainDef(Environment e,Lexeme l){
		//Skip main stuff
		l = l.getRight().getRight().getRight().getRight().getRight().getRight().getRight().getLeft();  
//		System.out.println(l);
		codeBlock(e,l);
	}
	
	public void statement(Environment e, Lexeme l){ 
		if (l.type == Lexeme.Type.GLUE){
			if (l.getLeft().type == Lexeme.Type.VAR || l.getLeft().type == Lexeme.Type.OS){ //CHANGE THIS LINE TO ACCEPT MORE TYPES
				objDecl(e, l.getLeft());
			}
			else if (l.getLeft().type == Lexeme.Type.FUNC){
				function(e,l);
			}
			else{expr(e,l.getLeft());}
		}
		else{
			if (l.type == Lexeme.Type.WHILE){
				whileLoop(e,l);
			}
			else{
				conditional(e, l);
			}
		}
	}
	
	public Lexeme eval(Lexeme a, Lexeme operator, Lexeme b){
		switch(operator.type){
		case PLUS: //CONCAT for strings, ADDITION for ints
			if (a.type == Lexeme.Type.STRING && b.type == Lexeme.Type.STRING){return new Lexeme(Lexeme.Type.STRING, a.sval + b.sval,operator.getLineNumber());} //STRING + STRING
			else if (a.type == Lexeme.Type.STRING && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.STRING, a.sval + Integer.toString(b.ival),operator.getLineNumber());} //STRING + INT = STRING
			else if (a.type == Lexeme.Type.INTEGER && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.INTEGER,a.ival + b.ival,operator.getLineNumber());}
			else if (a.type == Lexeme.Type.OBRACKET){return append(a,operator,b);}
			else if (a.type == Lexeme.Type.STRING && b.type == Lexeme.Type.FALSE){return new Lexeme(Lexeme.Type.STRING, a.sval + "FALSE", operator.getLineNumber());}
			else if (a.type == Lexeme.Type.STRING && b.type == Lexeme.Type.TRUE){return new Lexeme(Lexeme.Type.STRING, a.sval + "TRUE", operator.getLineNumber());}
			else{
				FatalError_Parser("Cannot apply + operator to types " + Lexeme.typeToString(a.type) + " and " + Lexeme.typeToString(b.type), operator.getLineNumber());
			}
		case MINUS:
			if (a.type == Lexeme.Type.INTEGER && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.INTEGER,a.ival - b.ival,operator.getLineNumber());}
			else{
				FatalError_Parser("Cannot apply - operator to types " + Lexeme.typeToString(a.type) + " and " + Lexeme.typeToString(b.type), operator.getLineNumber());
				break;
			}
		case MULT:
			if (a.type == Lexeme.Type.INTEGER && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.INTEGER,a.ival * b.ival,operator.getLineNumber());}
			else{
				FatalError_Parser("Cannot apply * operator to types " + Lexeme.typeToString(a.type) + " and " + Lexeme.typeToString(b.type), operator.getLineNumber());
				break;
			}
		case DIV:
			if (a.type == Lexeme.Type.INTEGER && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.INTEGER,a.ival / b.ival,operator.getLineNumber());}
			else{
				FatalError_Parser("Cannot apply / operator to types " + Lexeme.typeToString(a.type) + " and " + Lexeme.typeToString(b.type), operator.getLineNumber());
				break;
			}
		case EXP:
			if (a.type == Lexeme.Type.INTEGER && b.type == Lexeme.Type.INTEGER){return new Lexeme(Lexeme.Type.INTEGER, (int)Math.pow(a.ival, b.ival),operator.getLineNumber());}
			else{
				FatalError_Parser("Cannot apply + operator to types " + Lexeme.typeToString(a.type) + " and " + Lexeme.typeToString(b.type), operator.getLineNumber());
				break;
			}
		default:
			FatalError_Parser("Invalid Operator " +operator.sval, operator.getLineNumber());
			break;
		}
		return null;
	}
	
	public Lexeme expr(Environment e, Lexeme l){ //Takes glue with expr at left
		if (l == null){
//			System.out.println("expr retuns NULL");
			return new Lexeme(Lexeme.Type.NULL);
		}
		Lexeme operand = l.getLeft();
		Lexeme operator = l.getLeft().getRight();
		Lexeme val;
		if (operand.type == Lexeme.Type.ID){ //If variable, get value;
			Environment objEnv = e.getKey(operand).getObjEnv();
//			System.out.println(objEnv);
			//SET VAL BEGIN
			if (operand.getLeft() != null && operand.getLeft().type == Lexeme.Type.OPAREN){ //If Function call...
//				System.out.println();
				Lexeme argList = argList(e, operand.getLeft());

				val = functionCall(e,operand,argList);
				if (operator != null && operator.type == Lexeme.Type.EQUALS){
					FatalError_Parser("Cannot use assignment on Function Call to " + operand.sval, operand.getLineNumber());
				}
			}
			else if (operand.getLeft() != null && operand.getLeft().type == Lexeme.Type.OBRACKET){ // If array access.....
				Lexeme ndx = expr(e,operand.getLeft().getRight());
				if (ndx.type != Lexeme.Type.INTEGER){
					FatalError_Parser("Array must be integer, not "+ Lexeme.typeToString(ndx.type), operand.getLineNumber());
				}
				Lexeme element = e.getNdx(operand,ndx.ival);
				element.setRight(ndx);
				val = applyOverride(objEnv,"GETNDX",element);
				if (operator != null && operator.type == Lexeme.Type.EQUALS){
					Lexeme result = expr(e,l.getRight());
					result.setRight(ndx);//Set right lexeme to ndx for dual argument calls to overrides
					return applyOverride(objEnv,"GETNDX",e.updateNdx(operand,applyOverride(objEnv,"SETNDX",result),ndx.ival));
				}
			}
			else{
				val = applyOverride(objEnv,"GET",e.get(operand));
			}
			//SET VAL END
			if (operator == null){
				return val;
			}
			else if (operator.type == Lexeme.Type.EQUALS){ //If assignment...	
				Lexeme tempVal = applyOverride(objEnv,"SET",expr(e,l.getRight())); //Need to link new value to obj lexemes to keep obj stuff
//				Lexeme object = e.get(operand).getRight();
//				tempVal.setRight(object);
				return applyOverride(objEnv,"GET",e.update(operand,tempVal)); //Will overwrite function calls... 
			}
			else{
				return eval(val,operator,expr(e,l.getRight()));
			}
		}
		else{ //Is a literal
			val = operand;
			if (operand.type == Lexeme.Type.OBRACKET){
				Lexeme elementPlaceholder = operand.getLeft();
				val = new Lexeme(Lexeme.Type.OBRACKET);
				val.setLeft(element(e,elementPlaceholder.getLeft()));
			}
			if (operator == null){
				return val;
				}
			else{
				return eval(val,operator,expr(e,l.getRight()));
			}
		}
	}
	
	public Lexeme element(Environment e, Lexeme l){
		if (l == null){
			return null;
		}
		l.setLeft(expr(e,l.getLeft())); //Eval this element
		l.setRight(element(e,l.getRight())); //eval next element
		return l;//return it
	}
	
	public Lexeme argList(Environment e, Lexeme l){//Take an arg list, including Parens, and evals each argument
//		System.out.println("argList: " + l);//Debug calls. Should be (
		Lexeme placeholder = l.getRight(); //Glue node
		if (placeholder.type == Lexeme.Type.NULL){return null;} //No arguments to evaluate
		else{
			Lexeme argGlue = placeholder.getLeft();
			Lexeme result = new Lexeme(Lexeme.Type.GLUE); //Lexeme tree we will return;
			Lexeme rest = result;
			Lexeme arg;
			while (argGlue != null){
				arg = argGlue.getLeft();
				if (arg.getLeft().type == Lexeme.Type.FUNC){ //If its a func def, just pass, dont add to env
					rest.setLeft(arg.getLeft().getRight().getRight()); //Copied from val arg in function()
				}
				else{
					rest.setLeft(expr(e,arg));
				}
				argGlue = argGlue.getRight();
				if (argGlue != null){
					rest = rest.setAndAdvanceRight(new Lexeme(Lexeme.Type.GLUE));
				}
			}
			return result;
		}
	}
	
	public Lexeme functionCall(Environment callEnv, Lexeme signature, Lexeme args){//Takes the def env and lexeme that hold args
//		System.out.println("Function Call: \n\tSignature: " + signature + "\n\tArgs: " + args);
//		args = args.cloneTree(); //unnecessary since everything in environment is a copy of the actual lexeme (clone)
		Lexeme definition = callEnv.get(signature);
		Environment defEnv = definition.getObjEnv();
		if (definition.type != Lexeme.Type.OPAREN){
			FatalError_Parser("Attempt to call " + signature.sval + ", a non-function type as a function", signature.getLineNumber());
		}
		Environment local = new Environment(defEnv);
		Lexeme parms = definition.getRight().getLeft(); //Placeholder -> acutual parm list
		if (setParms(local, parms, args) == null){
			FatalError_Parser("Incorrect number of arguments to function " + signature.sval, signature.getLineNumber());
		}
		Lexeme codePlaceholder = definition.getRight().getRight().getRight().getRight();// ( -> parms -> ) -> { -> codePlaceholder
		Lexeme returnStatementPlaceholder = codePlaceholder.getRight(); // codePlaceholder -> returnStatementPlaceholder
		if (codePlaceholder.type != Lexeme.Type.NULL){
			codeBlock(local,codePlaceholder.getLeft());
		}
		if (returnStatementPlaceholder.type != Lexeme.Type.NULL){
			return returnStatement(local,returnStatementPlaceholder.getLeft());
		}
		return null;
	}
	
	public Lexeme returnStatement(Environment e, Lexeme l){
		Lexeme value = l.getRight();
		return expr(e,value);
	}
	
	public Environment setParms(Environment e, Lexeme parms, Lexeme args){//Sets each parm to the provided args in the env provided. Ordered. 
		while (parms!= null && args != null){
			if (parms.type == Lexeme.Type.COMMA){
				parms = parms.getRight();
			}
			else{
				e.insert(parms, args.getLeft());
				parms = parms.getRight();
				args = args.getRight();
			}
		}
		if (args != null || parms != null){ //Make sure args !> parms
			return null;
		}
		return e;
	}
	
	public Lexeme function(Environment e, Lexeme l){ //Takes glue that holds a function def
		Lexeme signature = l.getLeft().getRight();
		Lexeme body = l.getLeft().getRight().getRight();
		body.setObjEnv(e); //Save defining environment for use in calls
		return e.insert(signature,body);
		
	}
	
	public void conditional(Environment e, Lexeme l){
		Environment local = new Environment(e);
		Lexeme predicateGlue = l.getRight().getRight(); //if -> ( -> predicate Glue
		Lexeme codePlaceholder = predicateGlue.getRight().getRight().getRight(); //predicateGlue -> ) -> { -> codePlaceholder
		if (predicate(local,predicateGlue.getLeft()).type == Lexeme.Type.TRUE){
			codeBlock(local, codePlaceholder.getLeft());
		}
		else{
			Lexeme elseClause = codePlaceholder.getRight().getRight();
			if (elseClause.type != Lexeme.Type.NULL){
				codePlaceholder = elseClause.getLeft().getRight().getRight();
				codeBlock(local,codePlaceholder.getLeft());
			}
		}
	}

	public void whileLoop(Environment e, Lexeme l){
		Environment local = new Environment(e);
		Lexeme predicateGlue = l.getRight().getRight(); //while -> ( -> predicate Glue
		Lexeme codePlaceholder = predicateGlue.getRight().getRight().getRight(); //predicateGlue -> ) -> { -> codePlaceholder
		while (predicate(local,predicateGlue.getLeft()).type == Lexeme.Type.TRUE){
			codeBlock(local, codePlaceholder.getLeft());
		}
	}
	
	public Lexeme predicate(Environment e, Lexeme l) {
		Lexeme op = l.getLeft();
		if (op.type == Lexeme.Type.NOT){
			Lexeme result = predicate(e,l.getRight());
			if (result.type == Lexeme.Type.TRUE){return new Lexeme(Lexeme.Type.FALSE);}
			else{return new Lexeme(Lexeme.Type.TRUE);} //NOT BOOLEAN TYPES TRANSLATE TO FALSE
			}
		Lexeme val = op;
		if (op.type == Lexeme.Type.ID){val = e.get(op);}
		if (op.getRight() != null){
			Lexeme operator1 = op.getRight();
			Lexeme operator2 = operator1.getRight();
			return predicateEval(val,operator1,operator2,predicate(e,l.getRight()));
		}
		else{
			return val;
		}
	}
	
	public Lexeme predicateEval(Lexeme a, Lexeme op1, Lexeme op2, Lexeme b){
		switch(op1.type){
		case AND:
			if (a.type == Lexeme.Type.TRUE && b.type == Lexeme.Type.TRUE){return new Lexeme(Lexeme.Type.TRUE);}
			else{return new Lexeme(Lexeme.Type.FALSE);}
		case OR:
			if (a.type == Lexeme.Type.TRUE || b.type == Lexeme.Type.TRUE){return new Lexeme(Lexeme.Type.TRUE);}
			else{return new Lexeme(Lexeme.Type.FALSE);}
		case EQUALS:
			if (a.type != b.type){ return new Lexeme(Lexeme.Type.FALSE);}
			else{
				if (a.type == Lexeme.Type.INTEGER){
					if (a.ival == b.ival){return new Lexeme(Lexeme.Type.TRUE);}
					else{return new Lexeme(Lexeme.Type.FALSE);}
				}
				else if (a.type == Lexeme.Type.NULL){return new Lexeme(Lexeme.Type.TRUE);}
				else{
					if (a.sval.equals(b.sval)){return new Lexeme(Lexeme.Type.TRUE);}
					else {return new Lexeme(Lexeme.Type.FALSE);}
				}
			}
		case GT:
			if (a.type != b.type || a.type != Lexeme.Type.INTEGER){ return new Lexeme(Lexeme.Type.FALSE);}
			else{
				if (op2 != null){
					if (a.ival >= b.ival){return new Lexeme(Lexeme.Type.TRUE);}
					else{return new Lexeme(Lexeme.Type.FALSE);}
				}
				else{
					if (a.ival > b.ival){return new Lexeme(Lexeme.Type.TRUE);}
					else{return new Lexeme(Lexeme.Type.FALSE);}
				}
			}
		case LT:
			if (a.type != b.type || a.type != Lexeme.Type.INTEGER){ return new Lexeme(Lexeme.Type.FALSE);}
			else{
				if (op2 != null){
					if (a.ival <= b.ival){return new Lexeme(Lexeme.Type.TRUE);}
					else{return new Lexeme(Lexeme.Type.FALSE);}
				}
				else{
					if (a.ival < b.ival){return new Lexeme(Lexeme.Type.TRUE);}
					else{return new Lexeme(Lexeme.Type.FALSE);}
				}
			}
		default:
			break;
		}
		return null;
	}
	
	public Environment initObjEnv(Environment e, Lexeme objDef){
		Environment objLocal = new Environment(e); //Gonna return this once we set everything up
		if (objDef == null){
			return objLocal;
		}
		Lexeme objDeclPlaceholder = objDef.getRight();
		if (objDeclPlaceholder.type == Lexeme.Type.GLUE){
			getObjDecl(objLocal,objDeclPlaceholder.getLeft());
		}
		Lexeme overridesPlaceholder = objDeclPlaceholder.getRight();
		if (overridesPlaceholder.type == Lexeme.Type.GLUE){
			setOverrides(objLocal,overridesPlaceholder.getLeft());
		}
		Lexeme helperPlaceholder = overridesPlaceholder.getRight();
		if (helperPlaceholder.type == Lexeme.Type.GLUE){
			setHelpers(objLocal,helperPlaceholder.getLeft());
		}
//		System.out.println(objLocal);
		return objLocal;
	}
	
	public Lexeme applyOverride(Environment e, String overrideType, Lexeme arg){
		Lexeme signature = new Lexeme(Lexeme.Type.ID, overrideType, arg.getLineNumber());
		if (e == null || !e.exists(signature)){return arg;}
		Lexeme args = new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(arg);
		if (overrideType.equals("GETNDX") || overrideType.equals("SETNDX")){ //passing two arguments for GETNDX and SETNDX
			args.setRight(new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(arg.getRight()));
		}
		return functionCall(e,signature,args);
		
	}
	
	public void setOverrides(Environment e, Lexeme overrideList){
		if (overrideList == null){
			return;
		}
		Lexeme override = overrideList.getLeft();
		Lexeme sig = new Lexeme(Lexeme.Type.ID, Lexeme.typeToString(override.type), override.getLineNumber());
		Lexeme function = override.getRight().getRight();
		Lexeme funcBody = function.getLeft().getRight().getRight();
		funcBody.setObjEnv(e); //Set def env for overrides
		e.insert(sig, funcBody);
		setOverrides(e,overrideList.getRight());
	}
	
	public void setHelpers(Environment e, Lexeme helperList){
		if (helperList == null){
			return;
		}
		function(e,helperList.getLeft());
		setHelpers(e, helperList.getRight());
	}
	
	
	public void codeBlock(Environment e, Lexeme l){
		if (l == null){
			return;
		}
		statement(e,l.getLeft());
		if (l.getRight() != null){codeBlock(e,l.getRight());}
	}
	
	private void FatalError_Parser(String msg, Integer line){
		System.out.println("FATAL ERROR in PARSER on line: " + line +" - " + msg);
		System.exit(1);
	}
	public Lexeme append(Lexeme arr,Lexeme operator, Lexeme val){ //Takes operator for line number 
		if (arr.type != Lexeme.Type.OBRACKET){
			FatalError_Parser("Attempted to get index of type " + Lexeme.typeToString(arr.type) + ". Dont do that.", operator.getLineNumber());
		}
		Lexeme element;
		if (arr.getLeft() == null){
			arr.setLeft(new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(val));
			return arr;
		}
		else{
			element = arr.getLeft();
			while (element.getRight() != null){
				element = element.getRight();
			}
			element.setRight(new Lexeme(Lexeme.Type.GLUE).setLeftReturnSelf(val));
			return arr;
		}
	}
}
