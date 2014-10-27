package MiniC.Scanner;

import MiniC.Scanner.SourceFile;
import MiniC.Scanner.Token;
import java.nio.channels.FileChannel;
//import java.util.*;

public final class Scanner {

	private SourceFile sourceFile;
	private FileChannel sourceFileChannel;

	private char previousChar;
	private char currentChar;
	private boolean verbose;
	private StringBuffer currentLexeme;
	private boolean currentlyScanningToken;
	private int currentLineNr;
	private int currentColNr;
	
	private long snapShotPos;
	private StringBuffer snapShotLexeme;
	private char snapShotCurrentChar;
	private int snapShotLineNr;
	private int snapShotColNr;
	
	public int startLineNr;
	public int startColNr;
	//public int endLineNr;
	
	private boolean needNewLineFlag = false;

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
		return isAlphabet(c) || isDigit(c) || (c == '_');
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
			case "true":
				return true;
			case "false":
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
			case "true":
				return Token.BOOLLITERAL;
			case "false":
				return Token.BOOLLITERAL;
			default:
				return Token.ERROR;
		}
	}
///////////////////////////////////////////////////////////////////////////////

	public Scanner(SourceFile source) {
		sourceFile = source;
		sourceFileChannel = sourceFile.source.getChannel();
		previousChar = '\u0000';
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
		previousChar = currentChar;
		currentChar = sourceFile.readChar();
		currentColNr++;
	}
	
	private void skipIt() {
		previousChar = currentChar;
		currentChar = sourceFile.readChar();
		currentColNr++;
	}
	
	private void emptyIt() {
		currentLexeme = new StringBuffer();
	}
	
	private void snapShot() {
		try {
			snapShotPos = sourceFileChannel.position();
		} catch (java.io.IOException e) {
			return;
		}
		//snapShotLexeme = (StringBuffer)((StringBuffer)currentLexeme).clone();
		//for (int i = 0; i < currentLexeme.length(); i++) {
		//	snapShotLexeme.append(currentLexeme.charAt(i));
		//}
		snapShotLexeme = new StringBuffer(currentLexeme.toString());
		snapShotCurrentChar = currentChar;
		snapShotLineNr = currentLineNr;
		snapShotColNr = currentColNr;
	}
	
	private void rollBack() {
		try {
			sourceFileChannel.position(snapShotPos);
		} catch (java.io.IOException e) {
			return;
		}
		currentLexeme = snapShotLexeme;
		currentChar = snapShotCurrentChar;
		currentLineNr = snapShotLineNr;
		currentColNr = snapShotColNr;
	}
	
	//private void unTake() {
	//	long currentPos = sourceFile.readPos();
	//	sourceFile.setPos(currentPos - 1);
	//	currentColNr--;
	//	return;
	//}

	private int scanToken() {
		//boolean numericFlag = false;
		//boolean strModeFlag = false;
		//현재 줄번호를 백업한다.
		startLineNr = currentLineNr;
		startColNr = currentColNr;

		switch (currentChar) {
			case '\n':
				skipIt();
				startLineNr++;
				currentLineNr++;
				//currentLineNr = 1;
				//currentColNr = 1;
				startColNr = 1;
				
				//debug
				//System.out.println("D3! / startColNr : " + startColNr + " currentColNr : " +  currentColNr);
				//debug
				
				return scanToken();
				
			case ' ':
				skipIt();
				currentColNr++;
				return scanToken();

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
				if (currentChar == '.' || currentChar == 'e' || currentChar == 'E') {
					//numericFlag = true
					//이러면 소숫점 자리란 의미이므로
					//System.out.println("zxc");
				}
				else {
					//이러면 소숫점이 아니므로 
					return Token.INTLITERAL;
				}
				
				if (currentChar == '.') {
					//소숫점이므로 take 해야 한다.
					takeIt();
					//예시, 132.
					
					//이 뒤로는 숫자만이 와야 한다. 아니라면 바로 잘라내야 한다.
					if (isDigit(currentChar)) {
						//숫자가 온다.
						while (true) {
							takeIt();
							if (!isDigit(currentChar)) {
								break;
							}
						}
						//예시 132.45
						
						//이제 이 뒤로 올 수 있는것은, e,E 뿐이다.
						if (currentChar == 'e' || currentChar == 'E') {
							//스냅샷을 찍는다.
							snapShot();
							
							//e 나 E 인 경우
							takeIt();
							//예시 132.45e, 132.45E
							
							//이 뒤로는 부호가 올 수도 있고, 안올 수도 있다.
							//그걸 지나고 나면 무조껀 숫자가 와야 한다. 이건 부호의 존재여부와 관계없다.
							
							if (currentChar == '+' || currentChar == '-') {
								takeIt();
								//예시 132.45e+, 132.45E-
								if (isDigit(currentChar)) {
									//여기서 뒤따르는게 숫자라면 이것까지 걷어가면 된다.
									while (true) {
										takeIt();
										if (!isDigit(currentChar)) {
											break;
										}
										//예시, 132.45e+42, 132.45E-42
									}
									return Token.FLOATLITERAL;
								}
								else {
									//이건 그냥 에러다
									//롤백 처리가 필요하지만
									//예시 132.45e+ , 132.45E-
									//return Token.ERROR;
									rollBack();
									return Token.FLOATLITERAL;
								}
							}
							else {
								//예시 132.45e, 132.45E
								if (isDigit(currentChar)) {
									//여기서 뒤따르는게 숫자라면 이것까지 걷어가면 된다.
									while (true) {
										takeIt();
										if (!isDigit(currentChar)) {
											break;
										}
										//예시, 132.45e42, 132.45E42
									}
									return Token.FLOATLITERAL;
								}
								else {
									//이건 그냥 에러다
									//롤백 처리가 필요하지만 그건 좀더 나중에 생각해보자.
									//예시 132.45e, 132.45E
									//return Token.ERROR;
									rollBack();
									return Token.FLOATLITERAL;
								}
							}
						}
						else {
							//이건 즉시 리턴할 필요가 있다.
							return Token.FLOATLITERAL;
							//예시 132.45
						}
					}
					else {
						//바로 잘라낸다.
						return Token.FLOATLITERAL;
					}
				}
				else {
					//스냅샷을 찍는다.
					snapShot();
					
					//앞에서 걸러 냈으므로 뒤에는 무조껀 e E
					takeIt();
					//예시 132e, 132E
					//이제 뒤에는 -+ 가 있거나 숫자가 있거나 둘중 하나이며 예외가 있다면 에러이다.
					if (currentChar == '+' || currentChar == '-') {
						takeIt();
						//예시 132e+, 132e-
						//이제 뒤에는 무조껀 숫자가 오지 않으면 롤백이 필요한 에러이다.
						if (isDigit(currentChar)) {
							while (true) {
								takeIt();
								if (!isDigit(currentChar)) {
									break;
								}
							}
							//예시 132e+45, 132e-42
							return Token.FLOATLITERAL;
						}
						else {
							// 롤백이 필요한 에러이다.
							//예시 132e+, 132E-
							//return Token.ERROR;
							rollBack();
							return Token.FLOATLITERAL;
						}
					}
					else if (isDigit(currentChar)) {
						while (true) {
							takeIt();
							if (!isDigit(currentChar)) {
								break;
							}
						}
						//예시 132e45, 132E42
						return Token.FLOATLITERAL;
					}
					else {
						//에러이며 롤백후 리턴해야 할것이다.
						//예시 132E, 132e
						//return Token.ERROR;
						rollBack();
						return Token.FLOATLITERAL;
					}
				}
			case '.':
				takeIt();
				//이제 여기에서 뒤에 오는게 숫자냐 숫자가 아니냐에 따라 소숫점이냐 아니냐가 갈림.
				//숫자가 아니면 그냥 바로 에러고, 숫자면 거기까지로 인정해서 소숫점이다.
				if (isDigit(currentChar)) {
					while (true) {
						takeIt();
						if (!isDigit(currentChar)) {
							break;
						}
					}
					
					//여기에서 이제 이어지는 넘이 e 나 E 라면 이어가는 거고, 아니라면 바로 리턴이다.
					if (currentChar == 'e' || currentChar == 'E') {
						//스냅샷을 찍는다.
						snapShot();
						
						takeIt();
						//예시, .12e, .12E
						//이제 여기까지 온 이상, 뒤에 부호나 숫자가 안오면 에러다
						if (currentChar == '+' || currentChar == '-') {
							//부호
							takeIt();
							//예시 .12e+, .12E-
							//이제 부호를 가졌으니, 뒤에 오는게 숫자가 아니라면 에러다
							if (isDigit(currentChar)) {
								while (true) {
									takeIt();
									if (!isDigit(currentChar)) {
										break;
									}
								}
								//예시 .12e+72 .12E-34
								return Token.FLOATLITERAL;
							}
							else {
								//숫자가 아니니 에러다
								//예시 : .12e+z 12E-y
								//return Token.ERROR;
								rollBack();
								return Token.FLOATLITERAL;
							}
						}
						else if (isDigit(currentChar)) {
							//그냥 숫자
							while (true) {
								takeIt();
								if (!isDigit(currentChar)) {
									break;
								}
							}
							//예시 .12e34 .12E34
							return Token.FLOATLITERAL;
						}
						else {
							//에러
							//예시 .12ec .12Ez
							//롤백이 필요하다.
							//return Token.ERROR;
							rollBack();
							return Token.FLOATLITERAL;
						}
					}
					else {
						//e 나 E 가 아니므로, 바로 리턴해 버린다make.
						//예시 : .3232a
						return Token.FLOATLITERAL;
					}
				}
				else {
					//바로 에러다
					//예시 .a 같은 경우
					//구제불능
					return Token.ERROR;
				}
			
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
			case 'Z':	case '_':
				while (true) {
					takeIt();					
					if (!isMemberOfLiteral(currentChar)) {
						//unTake();
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
				if (currentChar == '/') {
					// 한줄 주석
					// 예시 : //
					while (true) {
						skipIt();
						if (currentChar == '\n') {
							skipIt();
							emptyIt();
							currentLineNr++;
							currentColNr = 0;
							return scanToken();
						}
						if (currentChar == '\r') {
							skipIt();
							if (currentChar == '\n') {
								skipIt();
								emptyIt();
								currentLineNr++;
								currentColNr = 0;
								return scanToken();
							}
						}
					}
				}
				else if (currentChar == '*') {
					//debug
					//System.out.println("Start Of Comment! : " + currentChar);
					//debug
					// 여러줄 주석
					// 예시 : /*
					while (true) {
						skipIt();
						if ((previousChar == '*') && (currentChar == '/')){
							//예시 /* ~~~ */
							skipIt();
							emptyIt();
							return scanToken();
						}
						else if(currentChar == '\n') {
							currentLineNr++;
							currentColNr = 0;
							startColNr = 1;
							//debug
							//System.out.println("D1! / startColNr : " + startColNr + " currentColNr : " +  currentColNr);
							//debug
						}
						else if((previousChar == '\r') && (currentChar == '\n')) {
							currentLineNr++;
							currentColNr = 0;
							startColNr = 1;
							//debug
							//System.out.println("D2! / startColNr : " + startColNr + " currentColNr : " +  currentColNr);
							//debug
						}
						else if(currentChar == '\u0000') {
							System.out.println("ERROR: unterminated multi-line comment.");
							emptyIt();
							return scanToken();
						}
						else {
						}
					}
				}
				else {
					//위에꺼에 해당이 안되는 경우
					//debug
					//System.out.println("D12! : " + currentChar);
					//debug
					
					return Token.DIV;
				}
				
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
			case '"':
				// skipIt();
				// while (true) {
					// if (currentChar == '"') {
						// skipIt();
						// break;
					// }
					// else if (currentChar == '\u0000') {
						// return Token.ERROR;
					// }
					// else if ((previousChar == '\\') && (currentChar == 'n')) {
					// }
					// else {
					// }
					
				// }
				skipIt();
				while (true) {
					if (currentChar == '"') {
						skipIt();
						break;
					}
					else if (currentChar == '\n') {
						skipIt();
						System.out.println("ERROR: unterminated string literal");
						//currentLineNr++;
						needNewLineFlag = true;
						currentColNr--;
						break;
					}
					else if (currentChar == '\u0000') {
						//문자열이 닫히기도 전에 널 스트링이 먼저 왓으니 이건 에러로 처리
						return Token.ERROR;
					}
					else {
						if (currentChar == '\\') {
							//일단 백슬래시가 들어온 상태
							takeIt();
							if (currentChar == 'n') {
								//이건 문제없음
								takeIt();
							}
							else {
								//문제있음
								takeIt();
								System.out.println("ERROR : illegal escape sequence");
							}
						}
						else {
							takeIt();
						}
					}
				}
				//예시 " ~~~ "
				return Token.STRINGLITERAL;
				
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
		
		//pos.StartLine = currentLineNr;
		//pos.EndLine = currentLineNr;
		//if (currentColNr == 0) {
		//	pos.StartCol = 1;
		//}
		//else {
		//	pos.StartCol = currentColNr;
		//}
		kind = scanToken();
		pos.StartLine = startLineNr;
		pos.EndLine = currentLineNr;
		if (startColNr == 0) {
			pos.StartCol = 1;
		}
		else {
			//debug
			//System.out.println("D4 / startColNr : " + startColNr);
			//debug
			if(kind == Token.EOF)
				pos.StartCol = 1;
			else
				pos.StartCol = startColNr;
		}
		
		currentToken = new Token(kind, currentLexeme.toString(), pos);
		//if (currentChar == '\u0000') {
		//	pos.EndCol = (currentColNr - 1);v
		//}
		//else {
		//	pos.EndCol = currentColNr;
		//}
		pos.EndCol = currentColNr - 1;
		
		if (needNewLineFlag) {
			currentLineNr++;
			currentColNr = 0;
			needNewLineFlag = false;
		}
		
		if(kind == Token.EOF)
			pos.EndCol = 1;
		if (verbose)
			currentToken.print();
		return currentToken;
	}

}
