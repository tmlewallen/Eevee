EEVEE - A programming language by Thomas Lewallen
(Developed using Java 7 SE)

Eevee is pretty similar to javascript. The "var" keyword is used to declare new variables and the "func" keyword to declare/define new functions. A program consists of 2 parts:

1. Variable Declarations/initialization

2. Main function.


VARIABLE DECLARATIONS:

One notable aspect of Eevee is that variables are like mini-objects. Each variable’s setter and getter can be overridden by a user defined function. Getters and setters are automatically called using the specific operators: if a variable is on the left of an = sign, the setter is called whereas whenever the value of the variable is needed, the getter is called.

. . . x = 5 #Setter is called
. . . 4 + x #Getter is called

A variable definition is similar to javascript’s JSON, where you have a key-value pair for every override. They "key" is a specific keyword used to denote which override and the "value" is an anonymous function defined by the user. Example:

var x = 5 {
	GET:func(value){return value + 1};
	SET:func(value){return value + 2};
};

So. . . 

x = 2;      #2 is passed to the function assigned to SET —> setting x to 2 + 2 -> 4;
print(x);   #the value of x is needed, the current value of x is passed to "GET" function, returning 4 + 1 -> 5. So this will print 5;

Additionally, a variable can have additional variables define as fields. . .

var y = 5 {
	var field = 0;
	SET:func(val){
		field = field + val;
		return field;
	};
	
};

The state of the variable’s fields are saved between references. So. . .

y = 3; #Setter returns value of "field" + "val" (value on right of = sign, 3). So y is set to 0 + 3 = 3;
y = 2; #Again, field is added to val (2) and returned, setting y to 3+2=5;
print(y); #prints 5

Using this functionality, you can create simple objects by adding fields to a variable and having "SET" act as a dispatch function

var z = 0{
	var value1 = 0;
	var value2 = 5;
	var value3 = 20;
	SET: func(val){
		var returnVal = 0;
		if (val == "value1"){ #Note string comparison
			returnVal = value1;
		}
		if (val == "value2"){
			returnVal = value2;
		}
		if (val == "value3"){
			returnVal = value3;
		}
		return returnVal;
	};
};

So. . .
z = "value1"; #sets z to the value of the field "value1" which is 5.
print(z); #prints 0
z = "value2"; #sets z to the value of the field "value2" which is 5;
print(z); #prints 5
#etc. . .

You can also add functions to serve as "methods" to variable declarations:

var a = 0{
	var count = 0;
	SET:func(val){
		var returnVal = 0;
		if (val == "count"){
			incCount();
			returnVal = getCount();
		}
		else{
			returnVal = val;
		}
		return returnVal;
	}; #Overrides delimited with SEMI COLONS
	func getCount(){
		return count;
	}, #helper functions/methods are delimited with COMMAS
	func incCount(){
		count = count + 1;
	}
};

So . . .
a = "count";
print(a); #prints 1
a = "count";
print(a); #prints 2
	
Additionally, the "=" sign is simply an operator. Therefore, it can be chained with other operators to form expressions.

var x = 0{
	SET:func(val){return val + 1;};
};

var y = 0{
	SET:func(val){return val + 2;};
};

var z = y = x = 1; #sets z = 1 + 1(x.SET) + 2(y.SET) = 4;

In this way, variables can act as "black boxes" that simply take, transform, then return values. Overrides are not required. Without a user-defined override, the value that would be passed as an argument to the override function is simply returned, i.e. func(val){return val;};.

var x = 5;

NOTE: Only DECLARATIONS and INITIALIZATIONS of variables are allowed above the Main function, no further assignment or function definitions/definitions. 


MAIN:

Every Eevee program has a main function (more of a code block). This function is defined using the following syntax:

Main.run(){
	#code
}

The "code" portion can contain function definitions, variable definitions, and function calls. 

Main.run(){
	var x = 2;
	func addTwo(i){
		return i + 2;
	}; #again, note semicolon after function definitions
	print(addTwo(x)); #print 4;
}#no semicolon here

Eevee Supports:
	Conditionals
	Arrays
	Recursion 
	Functions as first class objects (can be passed)
	Iteration (while)
	Dynamic Typing
	Integers
	Strings
	Comments (# for line comments, #/ /# for block comments)

NOTABLE LIMITATIONS:
	-You cannot have expressions in predicates: i.e. no predicate function calls or expressions
	if (x + 2 > 5){} #NO
	
	x = x + 2;
	if (x > 5){} #YES

	-Variable objects cannot be passed, only values.
	-Functions only support ONE return statement at the LAST LINE OF THE FUNCTION.
	-No "break" keyword to break out of loops
	-no "else if" conditionals, only if-else
	-No escaped characters in strings (\n,\t)
	-"@" denotes end-of-file, so cannot be used in a file
	-No order of operations, therefore no parentheses in expressions. 
	-negative numbers must be denoted using "0 - x" and not simply "-x"
	-No short circuiting
	-Arrays do NOT sit on top of java static arrays. Arrays are implemented as a tree of lexemes. 
	-Main.Loop() is accepted by recognizer, but doesn't loop main code block, only runs once, just like Main.run()
	-No input from stdin/file
	

KEYWORDS:
	var - used to declare a new variable
		var x = 5;
	func - used to declare and define a new function. (note the semi colon after "}")
		func y(arg1, arg2){ *code* }; 
	Main - denotes main code block.
	GET - denotes GET override. Should be set to an anonymous function that takes 1 arg and must return a value
	SET - denote SET override. Should be set to an anonymous function that takes 1 arg and must return a value
	GETNDX - denotes GETNDX override. Should be set to an anonymous function that takes 2 args and must return a value;
	SETNDX - denotes SETNDX override. Should be set to an anonymous function that takes 2 args and must return a value;
	NULL - denotes "null" value;
	
PROVIDED FUNCTIONS:
	print(value); - prints the value passed (printing functions results is "(" and printing arrays results in "[");
	cons(a,b); returns a dispatch function that will return a if "left" is passed and b if "right" passed
	car(list); takes a dispatch function returned from a cons call and returns a
	cdr(list); takes a dispatch function returned from a cons call and returns b
	printNewLine(); - prints a new line
	forLoopPassIndex(start,end,delta,f); - takes a start value, an end value, a delta, and a function. Emulates a for loop. Calls f each time, passing the value of the counter to f.
	forLoop(start,end,delta,f); - takes a start value, an end value, a delta, and a function. Emulates a for loop. Calls f each time with no arguments 	
	length(arr); - takes an array and returns its length
	

OVERRIDES:
	GET: takes one argument which is set to current value of variable. Must return a value.
	SET: takes one argument which is set the the value TO BE set to variable. Return value from function is then set as value to variable. 
	GETNDX: takes TWO arguments, first argument is value at given index, second argument is the index itself, i.e.
	var x = [0,1,2]{
		GETNDX:func(val,ndx){
			print(val); 
			print(nix);
			return val+1;
		};
	};
	. . .
	var y = x[1]; #prints value at x[1] (1), then prints index (1), then returns val+ 1 (2)

	SETNDX: takes two arguments, again, first argument is value at the given index, second is the index itself, i.e.
	var x = [0,1,2]{
		SETNDX:func(val,ndx){
			print(val);
			print(nix);
			return val+1;
		};
	};
	. . .
	x[1] = 2; #prints value (2) and index (1), then sets x[1] to 2 + 1 (3)
	print(x[1]); #prints 3


			
	