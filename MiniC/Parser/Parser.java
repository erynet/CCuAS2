package MiniC.Parser;


import MiniC.Scanner.Token;
import MiniC.Scanner.SourcePos;
import MiniC.Parser.SyntaxError;
import MiniC.Scanner.Scanner;
import MiniC.ErrorReporter;

public class Parser {

    private Scanner scanner;
    private ErrorReporter errorReporter;
    private Token currentToken;

    public Parser(Scanner lexer, ErrorReporter reporter) {
    	scanner = lexer;
        errorReporter = reporter;
    }

    // accept() checks whether the current token matches tokenExpected.
    // If so, it fetches the next token.
    // If not, it reports a syntax error.
    void accept (int tokenExpected) throws SyntaxError {
    	if (currentToken.kind == tokenExpected) {
    		currentToken = scanner.scan();
		} else {
		    syntaxError("\"%\" expected here", Token.spell(tokenExpected));
		}
    }

    // acceptIt() unconditionally accepts the current token
    // and fetches the next token from the scanner.
    void acceptIt() {
    	currentToken = scanner.scan();
    }

    void syntaxError(String messageTemplate, String tokenQuoted) throws SyntaxError {
		SourcePos pos = currentToken.GetSourcePos();
		errorReporter.reportError(messageTemplate, tokenQuoted, pos);
		throw(new SyntaxError());
    }

    boolean isTypeSpecifier(int token) {
    	if(token == Token.VOID ||
           token == Token.INT  ||
           token == Token.BOOL ||
           token == Token.FLOAT) {
    		return true;
		} else {
		    return false;
		}
    }
    
    boolean isExpr(int token) {
    	if(token == Token.PLUS ||
			token == Token.MINUS ||
			token == Token.NOT ||
			token == Token.ID ||
			token == Token.LEFTPAREN ||
			token == Token.INTLITERAL ||
			token == Token.BOOLLITERAL ||
			token == Token.FLOATLITERAL ||
			token == Token.STRINGLITERAL) {
			return true;
		} else {
			return false;
		}
	}
    
    boolean isStmt(int token) {
    	if(token == Token.LEFTBRACE ||
    	   token == Token.IF ||
    	   token == Token.WHILE ||
    	   token == Token.FOR ||
    	   token == Token.RETURN ||
    	   token == Token.ID) {
    		return true;
    	} else {
    		return false;
    	}
    }
    
    boolean isUnary(int token) {
		if(token == Token.PLUS ||
			token == Token.MINUS ||
			token == Token.NOT)	{
			return true;
		} else {
			return false;
		}
	}
	
	boolean isOP(int token) {
		if(token == Token.PLUS ||
			token == Token.MINUS ||
			token == Token.OR ||
			token == Token.AND ||
			token == Token.TIMES ||
			token == Token.DIV) {
			return true;
		} else {
			return false;
		}
	}
		
	boolean isEqual(int token) {
		if(token == Token.EQ ||
			token == Token.NOTEQ ||
			token == Token.LESS ||
			token == Token.LESSEQ ||
			token == Token.GREATER ||
			token == Token.GREATEREQ) {
			return true;
		} else {
			return false;
		}
	}

    ///////////////////////////////////////////////////////////////////////////////
    //
    // toplevel parse() routine:
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parse() {

		currentToken = scanner.scan(); // get first token from scanner...
	
		try {
		    parseProgram();
		    if (currentToken.kind != Token.EOF) {
			syntaxError("\"%\" not expected after end of program",
				       currentToken.GetLexeme());
		    }
		}
		catch (SyntaxError s) {return; /* to be refined in Assignment 3...*/ }
		return;
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseProgram():
    //
    // program ::= ( (VOID|INT|BOOL|FLOAT) ID ( FunPart | VarPart ) )*
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseProgram() throws SyntaxError {
		while (isTypeSpecifier(currentToken.kind)) {
	        acceptIt();
		    accept(Token.ID);
		    if (currentToken.kind == Token.LEFTPAREN) {
		    	parseFunPart();
		    } else {
		    	parseVarPart();
		    }
		}
    }


    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseFunPart():
    //
    // FunPart ::= ( "(" ParamsList? ")" CompoundStmt )
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseFunPart() throws SyntaxError {
        // We already know that the current token is "(".
        // Otherwise use accept() !
		// "(" params-list? ")" compound-stmt
        acceptIt();
		// params-list? ")" compound-stmt
        if (isTypeSpecifier(currentToken.kind)) {
		    parseParamsList();
		}
        // ")" compound-stmt
		accept(Token.RIGHTPAREN);
		// compound-stmt
		parseCompoundStmt();
    }


    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseParamsList():
    //
    // ParamsList ::= ParamsDecl ( "," ParamsDecl ) *
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseParamsList() throws SyntaxError {
    	// to be completed by you...
    	// ParamsDecl ( "," ParamsDecl ) *
    	parseParameterDecl();
    	
    	// ( "," ParamsDecl ) *
    	while (currentToken.kind == Token.COMMA) {
    		// "," ParamsDecl
    		acceptIt();
    		// ParamsDecl
    		parseParameterDecl();
    	}
    } 

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseParameterDecl():
    //
    // parameter-decl ::=  typespecifier declarator
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseParameterDecl() throws SyntaxError {
    	// to be completed by you...
    	
    	// typespecifier declarator
    	if (isTypeSpecifier(currentToken.kind)) {
    		// typespecifier declarator
    		acceptIt();
    		// declarator
    		parseDeclarator();
    	} else {
    		throw(new SyntaxError());
    	}
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseDeclarator():
    //
    // declarator ::=  ID 
    //				|  ID “[“ INTLITERAL “]” 
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseDeclarator() throws SyntaxError {
    	// ID | ID "[" INTLITERAL "]"
    	accept(Token.ID);
    	
    	//  | "[" INTLITERAL "]"
    	if (currentToken.kind == Token.LEFTBRACKET) {
    		// "[" INTLITERAL "]"
    		acceptIt();
    		// INTLITERAL "]"
    		accept(Token.INTLITERAL);
    		// "]"
    		accept(Token.RIGHTBRACKET);
    	}
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseCompoundStmt():
    //
    // CompoundStmt ::= "{" VariableDefinition* Stmt* "}"
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseCompoundStmt() throws SyntaxError {
		// to be completed by you...
		// "{" variable-def* stmt* "}"
		accept(Token.LEFTBRACE);
		
		// variable-def* stmt* "}"
		// (Typespecifier ID variable-part)* stmt* "}"
		while (isTypeSpecifier(currentToken.kind)) {
			acceptIt();
			// ID variable-part)* stmt* "}"
			accept(Token.ID);
			// variable-part)* stmt* "}"
			parseVarPart();
			// )* stmt* "}"
		}
		// stmt* "}"
		while (isStmt(currentToken.kind)) {
			// "}"
			parseStmt();
		}
		
		// "}"
		accept(Token.RIGHTBRACE);
    }
    
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseStmt():
    //
    // Stmt ::= compound-stmt | if-stmt | while-stmt | for-stmt | return expr? ";" | ID "=" expr ";" | ID "[" expr "]" "=" expr ";" | ID arglist ";"
    //
    ///////////////////////////////////////////////////////////////////////////////
	
	public void parseStmt() throws SyntaxError {
		switch (currentToken.kind) {
		case Token.LEFTBRACE:
			// “{” variable-def* stmt* “}” 
			parseCompoundStmt();
			break;
		case Token.IF:
			// if “(” expr “)” stmt ( else stmt )?
			parseIf();
			break;
		case Token.WHILE:
			// while “(“ expr “)” stmt
			parseWhile();
			break;
		case Token.FOR:
			// for “(“ asgnexpr? ”;” expr? “;” asgnexpr? “)” stmt
			parseFor();
			break;
		case Token.RETURN:
			// return expr? ";"
			acceptIt();
			// expr? ";"
			
			if (isExpr(currentToken.kind)) {
				// expr? ";"
				parseExpr();
				// ? ";"
			}
			
			// ";"
			accept(Token.SEMICOLON);
			break;
		case Token.ID:
			// ID "=" expr ";" | ID "[" expr "]" "=" expr ";" | ID arglist ";"
			acceptIt();
			// "=" expr ";" | "[" expr "]" "=" expr ";" | arglist ";"
		
			if (currentToken.kind == Token.ASSIGN) {
				// "=" expr ";"
				acceptIt();
				// expr ";"
				parseExpr();
				// ";"
				accept(Token.SEMICOLON);
			} else if (currentToken.kind == Token.LEFTBRACKET) {
				// "[" expr "]" "=" expr ";"
				acceptIt();
				// expr "]" "=" expr ";"
				parseExpr();
				// "]" "=" expr ";"
				accept(Token.RIGHTBRACKET);
				// "=" expr ";"
				accept(Token.ASSIGN);
				// expr ";"
				parseExpr();
				// ";"
				accept(Token.SEMICOLON);
			} else {
				// “(“ args? “)” ; 
				parseArglist();
				// ;
				accept(Token.SEMICOLON);
			}
			break;
		}
	}
	

    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseIf():
    //
    // if-stmt ::= if "(" expr ")" stmt (else stmt) ?
    //
    ///////////////////////////////////////////////////////////////////////////////
	
	public void parseIf() throws SyntaxError {
		// if "(" expr ")" stmt (else stmt) ?
		acceptIt();
		// "(" expr ")" stmt (else stmt) ?
		accept(Token.LEFTPAREN);
		// expr ")" stmt (else stmt) ?
		parseExpr();
		// ")" stmt (else stmt) ?
		accept(Token.RIGHTPAREN);
		// stmt (else stmt) ?
		parseStmt();
		// (else stmt) ?
		while (currentToken.kind == Token.ELSE) {
			// (else stmt) ?
			acceptIt();
			// stmt) ?
			parseStmt();
			// ) ?
		}
	}
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseWhile():
    //
    // while-stmt ::= while "(" expr ")" stmt
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseWhile() throws SyntaxError {
    	// while "(" expr ")" stmt
    	acceptIt();
    	// "(" expr ")" stmt
    	accept(Token.LEFTPAREN);
    	// expr ")" stmt
    	parseExpr();
    	// ")" stmt
    	accept(Token.RIGHTPAREN);
    	// stmt
    	parseStmt();
    }
    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseFor():
    //
    // For-stmt ::= for "(" asgnexpr? ";" expr? ";" asgnexpr ? ")" stmt
    //
    ///////////////////////////////////////////////////////////////////////////////	
	
    public void parseFor() throws SyntaxError {
    	// for "(" asgnexpr? ";" expr? ";" asgnexpr ? ")" stmt
    	acceptIt();
    	// "(" asgnexpr? ";" expr? ";" asgnexpr ? ")" stmt
    	accept(Token.LEFTPAREN);
    	// asgnexpr? ";" expr? ";" asgnexpr ? ")" stmt
    	if (currentToken.kind == Token.ID) {
    		acceptIt();
    		accept(Token.ASSIGN);
    		parseExpr();
    	}
    	// ";" expr? ";" asgnexpr ? ")" stmt
    	accept(Token.SEMICOLON);
    	// expr? ";" asgnexpr ? ")" stmt
    	if (isExpr(currentToken.kind)) {
    		parseExpr();
    	}
    	// ";" asgnexpr ? ")" stmt
    	accept(Token.SEMICOLON);
    	// asgnexpr ? ")" stmt
    	if (currentToken.kind == Token.ID) {
    		acceptIt();
    		accept(Token.ASSIGN);
    		parseExpr();
    	}
    	// ")" stmt
    	accept(Token.RIGHTPAREN);
    	// stmt
    	parseStmt();
    }

    
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseArglist():
    //
    // Arglist ::= "(" args? ")"
	// Args ::= expr ( "," expr) *
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseArglist() throws SyntaxError {
    	accept(Token.LEFTPAREN);
    	if (isExpr(currentToken.kind)) {
    		parseExpr();
    		while (currentToken.kind == Token.COMMA) {
    			acceptIt();
    			parseExpr();
    		}
    	}
    	accept(Token.RIGHTPAREN);
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseVarPart():
    //
    // VarPart ::= ( "[" INTLITERAL "]" )?  ( "=" initializer ) ? ( "," init_decl)* ";"
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseVarPart() throws SyntaxError {
	// to be completed by you...
    	if (currentToken.kind == Token.LEFTBRACKET) {
    		// ( "[" INTLITERAL "]" )?  ( "=" initializer ) ? ( "," init_decl)* ";"
    		acceptIt();
    		// INTLITERAL "]" )?  ( "=" initializer ) ? ( "," init_decl)* ";"
    		accept(Token.INTLITERAL);
    		// "]" )?  ( "=" initializer ) ? ( "," init_decl)* ";"
    		accept(Token.RIGHTBRACKET);
    		// ( "=" initializer ) ? ( "," init_decl)* ";"
    	}
    	
    	if (currentToken.kind == Token.ASSIGN) {
    		// ( "=" initializer ) ? ( "," init_decl)* ";"
    		acceptIt();
    		// initializer ) ? ( "," init_decl)* ";"
    		parseInitializer();
    	}
    	
    	while (currentToken.kind == Token.COMMA) {
    		// ( "," init_decl)* ";"
    		acceptIt();
    		// init_decl)* ";"
    		parseInitDecl();
    		// ";"
    	}
    	// ";"
    	accept(Token.SEMICOLON);
    }
    // to be completed by you...

	///////////////////////////////////////////////////////////////////////////////
	//
	// parseInitializer():
	//
	// initializer ::=  expr 
	// 				 |  “{“ expr ( “,” expr )*  “}” 
	//
	///////////////////////////////////////////////////////////////////////////////
	
	public void parseInitializer() throws SyntaxError {
		
		if (isExpr(currentToken.kind)) {
			// expr
			parseExpr();
		} else if (currentToken.kind == Token.LEFTBRACE) {
			// “{“ expr ( “,” expr )*  “}” 
			acceptIt();
			// expr ( “,” expr )*  “}”
			parseExpr();
			// ( “,” expr )*  “}”
			while (currentToken.kind == Token.COMMA) {
				acceptIt();
				//  expr )*  “}”
				parseExpr();
				// “}”
			}
			// “}”
			accept(Token.RIGHTBRACE);
		} else {
			throw(new SyntaxError());
		}
		
		/*
		if (currentToken.kind == Token.LEFTBRACE) {
			acceptIt();
			parseExpr();
			while(currentToken.kind == Token.COMMA)
			{
				acceptIt();
				parseExpr();
			}
			accept(Token.RIGHTBRACE);
		} else {
			parseExpr();
		}
		*/
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	//
	// parseInitDecl():
	//
	// initializer ::= declarator (“=” initializer)? 
	//
	///////////////////////////////////////////////////////////////////////////////
	
	public void parseInitDecl() throws SyntaxError {
		// declarator (“=” initializer)? 
		parseDeclarator();
		// (“=” initializer)?
		if (currentToken.kind == Token.ASSIGN) {
			// (“=” initializer)?
			acceptIt();
			// initializer)?
			parseInitializer();
		}
	}
	
	
	///////////////////////////////////////////////////////////////////////////////
	//
	// parseExpr():
	//
	//
	///////////////////////////////////////////////////////////////////////////////
	
	public void parseExpr() throws SyntaxError {
		parseTerminalExpr();
		while (isOP(currentToken.kind)) {
			acceptIt();
			parseTerminalExpr();
		}
		
		if(isEqual(currentToken.kind)) {
			acceptIt();
			parseExpr();
			while (isOP(currentToken.kind)) {
				acceptIt();
				parseTerminalExpr();
			}
		}
	}
	
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseTerminalExpr():
    //
    // unary-expr ::= primary-expr | "+" unary-expr | "-" unary-expr | "!" unary-expr
	// 
	// primary-expr ::= ID arglist? | ID "[" expr "]" | "(" expr ")" | intliteral | boolliterral | floatliteral | stringliteral
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseTerminalExpr() throws SyntaxError {
    	while (isUnary(currentToken.kind)) {
    		acceptIt();
    	}
    	
    	switch (currentToken.kind) {
    	case Token.ID:
    		acceptIt();
    		if (currentToken.kind == Token.LEFTPAREN) {
    			acceptIt();
    			if (isExpr(currentToken.kind)) {
    				parseExpr();
    				while (currentToken.kind == Token.COMMA) {
    					acceptIt();
    					parseExpr();
    				}
    			}
    			accept(Token.RIGHTPAREN);
    		} else if (currentToken.kind == Token.LEFTBRACKET) {
    			acceptIt();
    			parseExpr();
    			accept(Token.RIGHTBRACKET);
    		}
    		break;
    	case Token.LEFTPAREN:
    		acceptIt();
    		parseExpr();
    		accept(Token.RIGHTPAREN);
    		break;
    	case Token.BOOLLITERAL:
    	case Token.INTLITERAL:
    	case Token.FLOATLITERAL:
    	case Token.STRINGLITERAL:
    		acceptIt();
    		break;
    	default:
    		//throw(new SyntaxError());
    		break;
    	}
    }
}
