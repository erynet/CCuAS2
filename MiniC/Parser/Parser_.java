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
	
	boolean isTypeStmt(int token) {
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
	
	boolean isTypeExpr(int token) {
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
	
	boolean isTypeUnary(int token)
	{
		if(token == Token.PLUS ||
			token == Token.MINUS ||
			token == Token.NOT)	
		{
			return true;
		}
		else
		{
			return false;
		}
	}
	
	boolean isTypeOP(int token)
	{
		if(token == Token.PLUS ||
			token == Token.MINUS ||
			token == Token.OR ||
			token == Token.AND ||
			token == Token.TIMES ||
			token == Token.DIV)
		{
			return true;
		}
		else
		{
			return false;
		}
	}
		
	boolean isTypeEqual(int token)
	{
		if(token == Token.EQ ||
			token == Token.NOTEQ ||
			token == Token.LESS ||
			token == Token.LESSEQ ||
			token == Token.GREATER ||
			token == Token.GREATEREQ)
		{
			return true;
		}
		else
		{
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
    //					TypeSpecifier
    ///////////////////////////////////////////////////////////////////////////////

    public void parseProgram() throws SyntaxError {
		while (isTypeSpecifier(currentToken.kind)) {
			acceptIt();
			accept(Token.ID);
			if(currentToken.kind == Token.LEFTPAREN) {
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
        acceptIt();
        if (isTypeSpecifier(currentToken.kind)) {
			parseParamsList();
		}
		accept(Token.RIGHTPAREN);
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
		
		if(isTypeSpecifier(currentToken.kind)) {
			acceptIt();
			parseDeclarator();
		}
		
		while(currentToken.kind == Token.COMMA) {
			acceptIt();
			if(isTypeSpecifier(currentToken.kind)) {
				acceptIt();
				parseDeclarator();
			}
			else
			{
				// error
			}
		}

    } 
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseDeclarator()
    //
    // Declarator ::= ID | ID "[" intliterral "]"
    //
    ///////////////////////////////////////////////////////////////////////////////
	
	public void parseDeclarator() throws SyntaxError {
		accept(Token.ID);
		if(currentToken.kind == Token.LEFTBRACKET)
		{
			acceptIt();
			accept(Token.INTLITERAL);
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
		accept(Token.LEFTBRACE);
		
		while(isTypeSpecifier(currentToken.kind))
		{
			acceptIt();
			accept(Token.ID);
			parseVarPart();
		}
		while(isTypeStmt(currentToken.kind))
		{
			parseStmt();
		}
		
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
		switch(currentToken.kind)
		{
		case Token.LEFTBRACE:
			parseCompoundStmt();
			break;
		case Token.IF:
			parseIf();
			break;
		case Token.WHILE:
			parseWhile();
			break;
		case Token.FOR:
			parseFor();
			break;
		case Token.RETURN:
			acceptIt();
			
			if(isTypeExpr(currentToken.kind))
			{
				parseExpr();
			}
			accept(Token.SEMICOLON);
			break;
		case Token.ID:
			acceptIt();
		
			if(currentToken.kind == Token.ASSIGN)
			{
				acceptIt();
				parseExpr();
				accept(Token.SEMICOLON);
			}
			else if(currentToken.kind == Token.LEFTBRACKET)
			{
				acceptIt();
				parseExpr();
				accept(Token.RIGHTBRACKET);
				accept(Token.ASSIGN);
				parseExpr();
				accept(Token.SEMICOLON);
			}
			else
			{
				parseArglist();
				accept(Token.SEMICOLON);
			}
			break;
		}
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
		if(isTypeExpr(currentToken.kind))
		{
			parseExpr();
			while(currentToken.kind == Token.COMMA)
			{
				acceptIt();
				parseExpr();
			}
		}
		accept(Token.RIGHTPAREN);
	}
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseIf():
    //
    // if-stmt ::= if "(" expr ")" stmt (else stmt) ?
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseIf() throws SyntaxError {
		acceptIt();
		accept(Token.LEFTPAREN);
		parseExpr();
		accept(Token.RIGHTPAREN);
		
		parseStmt();
		
		if(currentToken.kind == Token.ELSE)
		{
			acceptIt();
			parseStmt();
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
		acceptIt();
		accept(Token.LEFTPAREN);
		parseExpr();
		accept(Token.RIGHTPAREN);
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
		acceptIt();
		accept(Token.LEFTPAREN);
		if(currentToken.kind == Token.ID)
		{
			acceptIt();
			accept(Token.ASSIGN);
			parseExpr();
		}
		accept(Token.SEMICOLON);
		
		if(isTypeExpr(currentToken.kind))
		{
			parseExpr();
		}
		accept(Token.SEMICOLON);
		
		if(currentToken.kind == Token.ID)
		{
			acceptIt();
			accept(Token.ASSIGN);
			parseExpr();
		}
		accept(Token.RIGHTPAREN);
		parseStmt();
    }

    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseVarPart():
    //
    // VarPart ::= ( "[" INTLITERAL "]" )?  ( "=" initializer ) ? ( "," init_decl)* ";"
    //
    ///////////////////////////////////////////////////////////////////////////////

    public void parseVarPart() throws SyntaxError {
		if(currentToken.kind == Token.LEFTBRACKET)
		{
			acceptIt();
			accept(Token.INTLITERAL);
			accept(Token.RIGHTBRACKET);
		}
		if(currentToken.kind == Token.ASSIGN)
		{
			acceptIt();
			parseInitializer();
		}
		while(currentToken.kind == Token.COMMA)
		{
			acceptIt();
			parseDeclarator();
			if(currentToken.kind == Token.ASSIGN)
			{
				acceptIt();
				parseInitializer();
			}
		}
		accept(Token.SEMICOLON);
    }
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseExpr():
    //
    // 
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseExpr() throws SyntaxError {
		parseTerminalExpr();
		while(isTypeOP(currentToken.kind))
		{
			acceptIt();
			parseTerminalExpr();
		}
		
		if(isTypeEqual(currentToken.kind))
		{
			acceptIt();
			parseExpr();
			while(isTypeOP(currentToken.kind))
			{
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
		while(isTypeUnary(currentToken.kind))
		{
			acceptIt();
		}
		
		switch(currentToken.kind)
		{
		case Token.ID:
			acceptIt();
			if(currentToken.kind == Token.LEFTPAREN)
			{
				acceptIt();
				if(isTypeExpr(currentToken.kind))
				{
					parseExpr();
					while(currentToken.kind == Token.COMMA)
					{
						acceptIt();
						parseExpr();
					}
				}
				accept(Token.RIGHTPAREN);
			}
			else if(currentToken.kind == Token.LEFTBRACKET)
			{
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
			// error
			break;
		}

    }
	
    ///////////////////////////////////////////////////////////////////////////////
    //
    // parseInitializer():
    //
    // Initializer ::= expr | "{" expr ( "," expr)* "}"
    //
    ///////////////////////////////////////////////////////////////////////////////
	
    public void parseInitializer() throws SyntaxError {
		if(currentToken.kind == Token.LEFTBRACE)
		{
			acceptIt();
			parseExpr();
			while(currentToken.kind == Token.COMMA)
			{
				acceptIt();
				parseExpr();
			}
			accept(Token.RIGHTBRACE);
		}
		else
		{
			parseExpr();
		}

    }
}
