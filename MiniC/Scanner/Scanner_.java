package MiniC.Scanner;

import MiniC.Scanner.SourceFile;
import MiniC.Scanner.Token;
//import java.nio.channels.FileChannel;
//import java.util.*;

public final class Scanner {

	private SourceFile sourceFile;

	private char currentChar;
	private boolean verbose;
	private StringBuffer currentLexeme;
	private boolean currentlyScanningToken;
	private int currentLineNr;
	private int currentColNr;

	private boolean isDigit(char c) {
		return (c >= '0' && c <= '9');
	}
	
	private boolean isLowerCase(char c) {
		return (c >= 'a' && c <= 'z');
	}
	
	private boolean isUpperCase(char c) {
		return (c >= 'A' && c <= 'Z');
	}
	
	private boolean isAlphabet(char c) {
		return isLowerCase(c) || isUpperCase(c);
	}
	
	private boolean isMemberOfLiteral(char c) {
		return isLowerCase(c) || isUpperCase(c) || isDigit(c) || (c == '_');
	}
	
	private boolean isHaveReservedWord() {
		String tempStr = currentLexeme.toString();
		switch(tempStr) {
			case "bool":
				return true;
			case "else":
				return true;
			case "float":
				return true;
			case "for":
				return true;
			case "if":
				return true;
			case "int":
				return true;
			case "return":
				return true;
			case "void":
				return true;
			case "while":
				return true;
			default:
				return false;
		}
	}
	
	private int matchReservedWord() {
		String tempStr = currentLexeme.toString();
		switch(tempStr) {
			case "bool":
				return Token.BOOL;
			case "else":
				return Token.ELSE;
			case "float":
				return Token.FLOAT;
			case "for":
				return Token.FOR;
			case "if":
				return Token.IF;
			case "int":
				return Token.INT;
			case "return":
				return Token.RETURN;
			case "void":
				return Token.VOID;
			case "while":
				return Token.WHILE;
			default:
				return Token.ERROR;
		}
	}
///////////////////////////////////////////////////////////////////////////////

	public Scanner(SourceFile source) {
		sourceFile = source;
		currentChar = sourceFile.readChar();
		verbose = false;
		currentLineNr = 1;
		currentColNr= 1;
	}

	public void enableDebugging() {
		verbose = true;
	}

	// takeIt appends the current character to the current token, and gets
	// the next character from the source program (or the to-be-implemented
	// "untake" buffer in case of look-ahead characters that got 'pushed back'
	// into the input stream).

	private void takeIt() {
		if (currentlyScanningToken) {
			currentLexeme.append(currentChar);
		}
		//System.out.println("nr : " + currentColNr + "char : " + currentChar);
		currentChar = sourceFile.readChar();
		currentColNr++;
	}
	
	private void unTake() {
	//	long currentPos = sourceFile.readPos();
	//	sourceFile.setPos(currentPos - 1);
	//	currentColNr--;
		return;
	}

	private int scanToken() {

		switch (currentChar) {

			case '0':	case '1':	case '2':	case '3':	case '4':
			case '5':	case '6':	case '7':	case '8':	case '9':
				//takeIt();
				//while (isDigit(currentChar)) {
				//	takeIt();
				//}
				//takeIt();
				while (true) {
					takeIt();
					if (!isDigit(currentChar)) {
						//unTake();
						break;
						//only int
					}
				}
				// Note: code for floating point literals is missing here...
				return Token.INTLITERAL;
			case 'a':	case 'b':	case 'c':	case 'd':	case 'e':
			case 'f':	case 'g':	case 'h':	case 'i':	case 'j':
			case 'k':	case 'l':	case 'm':	case 'n':	case 'o':
			case 'p':	case 'q':	case 'r':	case 's':	case 't':
			case 'u':	case 'v':	case 'w':	case 'x':	case 'y':
			case 'z':
			case 'A':	case 'B':	case 'C':	case 'D':	case 'E':
			case 'F':	case 'G':	case 'H':	case 'I':	case 'J':
			case 'K':	case 'L':	case 'M':	case 'N':	case 'O':
			case 'P':	case 'Q':	case 'R':	case 'S':	case 'T':
			case 'U':	case 'V':	case 'W':	case 'X':	case 'Y':
			case 'Z':
				while (true) {
					takeIt();					
					if (!isMemberOfLiteral(currentChar)) {
						unTake();
						break;
					}
				}
				
				if (isHaveReservedWord()) {
					//예약어를 가지고 있다.
					return matchReservedWord();
				}
				else {
					//예약어를 가지고 있지 않다.
					return Token.ID;
				}
			
			
			case '=':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.EQ;
				}
				else {
					//unTake();
					return Token.ASSIGN;
				}
			case '|':
				takeIt();
				if(currentChar == '|') {
					takeIt();
					return Token.OR;
				}
				else {
					//unTake();
					return Token.ERROR;
				}
			case '&':
				takeIt();
				if(currentChar == '&') {
					takeIt();
					return Token.AND;
				}
				else {
					//unTake();
					return Token.ERROR;
				}
			case '!':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.NOTEQ;
				}
				else {
					//unTake();
					return Token.NOT;
				}
			case '<':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.LESSEQ;
				}
				else {
					//unTake();
					return Token.LESS;
				}
			case '>':
				takeIt();
				if(currentChar == '=') {
					takeIt();
					return Token.GREATEREQ;
				}
				else {
					//unTake();
					return Token.GREATER;
				}
			case '+':
				takeIt();
				return Token.PLUS;
			case '-':
				takeIt();
				return Token.MINUS;
			case '*':
				takeIt();
				return Token.TIMES;
			case '/':
				takeIt();
				return Token.DIV;
				
			case '{':
				takeIt();
				return Token.LEFTBRACE;
			case '}':
				takeIt();
				return Token.RIGHTBRACE;
			case '[':
				takeIt();
				return Token.LEFTBRACKET;
			case ']':
				takeIt();
				return Token.RIGHTBRACKET;
			case '(':
				takeIt();
				return Token.LEFTPAREN;
			case ')':
				takeIt();
				return Token.RIGHTPAREN;
			case ',':
				takeIt();
				return Token.COMMA;
			case ';':
				takeIt();
				return Token.SEMICOLON;
				
			case '\u0000': // sourceFile.eot:
				currentLexeme.append('$');
				return Token.EOF;
			// Add code here for the remaining MiniC tokens...

			default:
				takeIt();
				//System.out.println("Error");
				return Token.ERROR;
		}
	}

	public Token scan () {
		Token currentToken;
		SourcePos pos;
		int kind;

		//takeIt();
		currentlyScanningToken = false;
		while (currentChar == ' ' || currentChar == '\f' || currentChar == '\n' || currentChar == '\r' || currentChar == '\t')
		{
//			takeIt();
			if (currentChar == '\n') {
				currentLineNr++;
				currentColNr = 0;
			}
			//System.out.println("Lexical Analysis ...");
			//System.out.println("T1");
			//System.out.println(currentColNr);
			takeIt();
			//System.out.println(currentColNr);
			//System.out.println("Lexical Analysis ...");
		} 

		currentlyScanningToken = true;
		currentLexeme = new StringBuffer("");
		pos = new SourcePos();
		// Note: currentLineNr and currentColNr are not maintained yet!
		pos.StartLine = currentLineNr;
		pos.EndLine = currentLineNr;
		if (currentColNr == 0) {
			pos.StartCol = 1;
		}
		else {
			pos.StartCol = currentColNr;
		}
		kind = scanToken();
		currentToken = new Token(kind, currentLexeme.toString(), pos);
		//if (currentChar == '\u0000') {
		//	pos.EndCol = (currentColNr - 1);
		//}
		//else {
		//	pos.EndCol = currentColNr;
		//}
		pos.EndCol = currentColNr - 1;
		
		if(kind == Token.EOF)
			pos.EndCol = 1;
		if (verbose)
			currentToken.print();
		return currentToken;
	}

}
