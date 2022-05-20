/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package icu.windea.pls.localisation.psi;

import com.intellij.openapi.project.*;import com.intellij.psi.TokenType;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import static icu.windea.pls.StdlibExtensionsKt.*;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>ParadoxLocalisationLexer.flex</tt>
 */
public class ParadoxLocalisationLexer implements com.intellij.lexer.FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int WAITING_LOCALE_COLON = 2;
  public static final int WAITING_LOCALE_END = 4;
  public static final int WAITING_PROPERTY_COLON = 6;
  public static final int WAITING_PROPERTY_NUMBER = 8;
  public static final int WAITING_PROPERTY_VALUE = 10;
  public static final int WAITING_PROPERTY_END = 12;
  public static final int WAITING_RICH_TEXT = 14;
  public static final int WAITING_PROPERTY_REFERENCE = 16;
  public static final int WAITING_PROPERTY_REFERENCE_PARAMETER_TOKEN = 18;
  public static final int WAITING_ICON = 20;
  public static final int WAITING_ICON_NAME_FINISHED = 22;
  public static final int WAITING_ICON_PARAMETER = 24;
  public static final int WAITING_COMMAND_SCOPE_OR_FIELD = 26;
  public static final int WAITING_COMMAND_SEPARATOR = 28;
  public static final int WAITING_COLOR_ID = 30;
  public static final int WAITING_COLORFUL_TEXT = 32;
  public static final int WAITING_CHECK_PROPERTY_REFERENCE_START = 34;
  public static final int WAITING_CHECK_ICON_START = 36;
  public static final int WAITING_CHECK_COMMAND_START = 38;
  public static final int WAITING_CHECK_COLORFUL_TEXT_START = 40;
  public static final int WAITING_CHECK_RIGHT_QUOTE = 42;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9,  9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15, 
     7,  7, 16, 16, 17, 17, 18, 18, 19, 19, 20, 20
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [9, 6, 6]
   * Total runtime size is 4256 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[(ZZ_CMAP_Z[ch>>12]<<6)|((ch>>6)&0x3f)]<<6)|(ch&0x3f)];
  }

  /* The ZZ_CMAP_Z table has 272 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\6\4\1\5\4\4\1\6\1\7\1\10\4\4\1\11\6\4\1\12\1\13\361\4");

  /* The ZZ_CMAP_Y table has 768 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\1\2\26\3\1\4\1\3\1\5\3\3\1\6\5\3\1\7\1\3\1\7\1\3\1\7\1\3\1\7\1\3"+
    "\1\7\1\3\1\7\1\3\1\7\1\3\1\7\1\3\1\7\1\3\1\7\1\3\1\10\1\3\1\10\1\4\4\3\1\6"+
    "\1\10\27\3\1\11\4\3\1\4\1\10\4\3\1\12\1\3\1\10\2\3\1\13\2\3\1\10\1\5\2\3\1"+
    "\13\16\3\1\14\1\15\76\3\1\11\227\3\1\4\12\3\1\10\1\6\2\3\1\16\1\3\1\10\5\3"+
    "\1\5\114\3\1\10\25\3\1\4\56\3\1\7\1\3\1\5\1\17\2\3\1\10\3\3\1\5\5\3\1\10\1"+
    "\3\1\10\5\3\1\10\1\3\1\6\1\5\6\3\1\4\15\3\1\10\67\3\1\4\3\3\1\10\61\3\1\20"+
    "\105\3\1\10\32\3");

  /* The ZZ_CMAP_A table has 1088 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\1\1\3\2\2\1\3\22\0\1\1\1\32\1\5\1\4\1\30\1\17\1\0\1\7\2\0\2\23\1\0"+
    "\1\22\1\16\1\20\12\10\1\27\2\0\1\23\2\0\1\24\32\25\1\14\1\11\1\15\1\0\1\21"+
    "\1\0\15\26\1\13\3\26\1\13\1\26\1\13\6\26\1\0\1\33\10\0\1\2\32\0\1\1\2\0\1"+
    "\31\3\0\1\12\170\0\12\6\106\0\12\6\6\0\12\6\134\0\12\6\40\0\12\6\46\0\1\1"+
    "\105\0\12\6\60\0\12\6\6\0\12\6\46\0\13\1\35\0\2\2\5\0\1\1\57\0\1\1\60\0\12"+
    "\6\26\0\12\6\74\0\12\6\16\0\62\6");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\25\0\1\1\1\2\2\3\1\4\2\5\1\6\2\7"+
    "\1\10\1\11\1\12\1\13\1\7\1\14\1\15\1\16"+
    "\2\17\1\7\1\1\1\20\1\21\1\22\1\23\1\24"+
    "\1\7\1\25\1\26\1\27\1\30\1\24\1\31\1\32"+
    "\1\33\1\34\1\35\1\36\1\37\1\1\1\40\1\41"+
    "\1\42\1\43\1\44\1\7\1\20\1\45\1\46\1\47"+
    "\1\50\1\51\1\52\3\0\1\53\1\54\1\55\1\56"+
    "\1\0\1\57\1\60\1\61\1\46\1\47\1\50\1\51"+
    "\1\52\1\62";

  private static int [] zzUnpackAction() {
    int [] result = new int[92];
    int offset = 0;
    offset = zzUnpackAction(ZZ_ACTION_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAction(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /** 
   * Translates a state to a row index in the transition table
   */
  private static final int [] ZZ_ROWMAP = zzUnpackRowMap();

  private static final String ZZ_ROWMAP_PACKED_0 =
    "\0\0\0\34\0\70\0\124\0\160\0\214\0\250\0\304"+
    "\0\340\0\374\0\u0118\0\u0134\0\u0150\0\u016c\0\u0188\0\u01a4"+
    "\0\u01c0\0\u01dc\0\u01f8\0\u0214\0\u0230\0\u024c\0\u0268\0\u0268"+
    "\0\u0284\0\u02a0\0\u02bc\0\u02d8\0\u02f4\0\u02f4\0\u0310\0\u024c"+
    "\0\u032c\0\u024c\0\u0348\0\u0348\0\u0364\0\u024c\0\u024c\0\u0380"+
    "\0\u039c\0\u039c\0\u03b8\0\u03d4\0\u03f0\0\u024c\0\u024c\0\u040c"+
    "\0\u040c\0\u0428\0\u024c\0\u024c\0\u024c\0\u0444\0\u0460\0\u047c"+
    "\0\u024c\0\u024c\0\u024c\0\u024c\0\u0498\0\u04b4\0\u024c\0\u024c"+
    "\0\u024c\0\u024c\0\u04d0\0\u04d0\0\u04ec\0\u024c\0\u0508\0\u0524"+
    "\0\u0540\0\u055c\0\u0578\0\u0284\0\u0594\0\u0310\0\u024c\0\u024c"+
    "\0\u024c\0\u024c\0\u04b4\0\u024c\0\u024c\0\u024c\0\u024c\0\u024c"+
    "\0\u024c\0\u024c\0\u024c\0\u0594";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[92];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /** 
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpackTrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\26\1\27\1\30\1\31\1\32\2\26\2\33\2\26"+
    "\1\34\2\26\1\33\2\26\1\34\1\33\2\26\1\33"+
    "\1\34\6\26\1\35\1\36\1\37\23\26\1\40\5\26"+
    "\1\35\1\36\1\37\1\41\30\26\1\35\1\36\1\37"+
    "\23\26\1\42\5\26\1\43\1\44\1\37\2\26\1\45"+
    "\1\26\1\45\24\26\1\35\1\36\1\37\1\26\1\46"+
    "\27\26\1\35\1\36\1\37\1\41\1\47\26\26\1\50"+
    "\1\51\1\52\1\37\1\50\1\47\3\50\1\53\1\54"+
    "\1\50\1\55\2\50\1\26\10\50\1\56\1\57\2\50"+
    "\1\26\1\60\1\61\1\37\1\26\1\47\1\26\2\62"+
    "\1\26\1\54\1\62\1\63\1\26\1\62\2\26\2\62"+
    "\2\26\2\62\1\26\1\64\2\26\1\65\1\26\2\66"+
    "\2\26\1\47\2\26\1\67\1\26\1\54\5\67\2\26"+
    "\2\67\1\26\2\67\1\26\1\64\4\26\1\60\1\61"+
    "\1\37\1\26\1\47\2\26\2\70\1\54\1\70\1\71"+
    "\3\26\3\70\2\26\2\70\1\26\1\72\1\73\1\26"+
    "\1\74\1\26\1\60\1\61\1\37\1\26\1\47\4\26"+
    "\1\54\1\26\1\71\13\26\1\72\1\73\1\26\1\74"+
    "\1\26\1\60\1\61\1\37\1\26\1\47\2\26\1\75"+
    "\1\26\1\54\1\75\3\26\1\75\2\26\2\75\1\26"+
    "\2\75\1\26\1\72\1\73\3\26\1\35\1\36\1\37"+
    "\1\26\1\47\2\26\1\76\1\26\1\54\1\76\1\26"+
    "\1\77\3\26\1\76\2\26\4\76\1\100\4\26\1\35"+
    "\1\36\1\37\1\26\1\47\4\26\1\54\2\26\1\77"+
    "\1\101\11\26\1\100\3\26\1\102\1\103\1\104\1\37"+
    "\1\102\1\47\4\102\1\105\1\106\11\102\2\106\5\102"+
    "\30\26\1\107\34\26\1\110\16\26\1\111\31\26\1\112"+
    "\26\26\1\113\26\26\35\0\1\27\1\30\1\31\31\0"+
    "\1\114\2\31\30\0\3\32\1\0\30\32\7\0\2\33"+
    "\2\0\1\33\2\0\1\33\2\0\2\33\2\0\2\33"+
    "\14\0\2\33\2\0\1\34\2\0\1\33\2\0\1\34"+
    "\1\33\2\0\1\33\1\34\1\115\5\0\1\35\1\36"+
    "\1\37\31\0\1\116\2\37\30\0\3\41\1\0\1\41"+
    "\1\0\26\41\1\0\1\43\1\44\1\37\36\0\1\45"+
    "\1\0\1\45\23\0\3\50\1\0\1\50\1\0\3\50"+
    "\2\0\1\50\1\0\2\50\1\0\10\50\2\0\3\50"+
    "\1\51\1\52\1\37\1\50\1\0\3\50\2\0\1\50"+
    "\1\0\2\50\1\0\10\50\2\0\2\50\2\117\2\0"+
    "\1\117\1\120\4\117\3\120\2\117\1\120\10\117\2\120"+
    "\2\117\32\0\1\121\15\0\1\122\20\0\1\60\1\61"+
    "\1\37\37\0\2\62\2\0\1\62\2\0\1\62\2\0"+
    "\2\62\2\0\2\62\6\0\2\66\41\0\1\67\2\0"+
    "\5\67\2\0\2\67\1\0\2\67\15\0\2\70\1\0"+
    "\1\70\4\0\3\70\2\0\2\70\15\0\1\75\2\0"+
    "\1\75\3\0\1\75\2\0\2\75\1\0\2\75\15\0"+
    "\1\123\2\0\1\123\1\0\1\124\1\125\2\0\1\123"+
    "\2\0\4\123\5\0\1\103\1\104\1\37\62\0\1\126"+
    "\1\0\1\107\1\127\2\0\1\107\1\127\22\107\1\127"+
    "\3\107\2\130\2\0\30\130\1\131\2\111\1\0\4\131"+
    "\1\111\2\131\1\111\2\131\1\111\2\131\1\111\2\131"+
    "\4\111\4\131\2\132\2\0\30\132\3\113\1\0\1\113"+
    "\1\133\26\113\1\0\2\115\1\134\30\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[1456];
    int offset = 0;
    offset = zzUnpackTrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackTrans(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      value--;
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }


  /* error codes */
  private static final int ZZ_UNKNOWN_ERROR = 0;
  private static final int ZZ_NO_MATCH = 1;
  private static final int ZZ_PUSHBACK_2BIG = 2;

  /* error messages for the codes above */
  private static final String[] ZZ_ERROR_MSG = {
    "Unknown internal scanner error",
    "Error: could not match input",
    "Error: pushback value was too large"
  };

  /**
   * ZZ_ATTRIBUTE[aState] contains the attributes of state <code>aState</code>
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\25\0\1\11\11\1\1\11\1\1\1\11\3\1\2\11"+
    "\6\1\2\11\3\1\3\11\3\1\4\11\2\1\4\11"+
    "\3\1\1\11\5\1\3\0\4\11\1\0\10\11\1\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[92];
    int offset = 0;
    offset = zzUnpackAttribute(ZZ_ATTRIBUTE_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackAttribute(String packed, int offset, int [] result) {
    int i = 0;       /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length();
    while (i < l) {
      int count = packed.charAt(i++);
      int value = packed.charAt(i++);
      do result[j++] = value; while (--count > 0);
    }
    return j;
  }

  /** the input device */
  private java.io.Reader zzReader;

  /** the current state of the DFA */
  private int zzState;

  /** the current lexical state */
  private int zzLexicalState = YYINITIAL;

  /** this buffer contains the current text to be matched and is
      the source of the yytext() string */
  private CharSequence zzBuffer = "";

  /** the textposition at the last accepting state */
  private int zzMarkedPos;

  /** the current text position in the buffer */
  private int zzCurrentPos;

  /** startRead marks the beginning of the yytext() string in the buffer */
  private int zzStartRead;

  /** endRead marks the last character in the buffer, that has been read
      from input */
  private int zzEndRead;

  /**
   * zzAtBOL == true <=> the scanner is currently at the beginning of a line
   */
  private boolean zzAtBOL = true;

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** denotes if the user-EOF-code has already been executed */
  private boolean zzEOFDone;

  /* user code: */
	public Project project;
		
    private boolean noIndent = true;
    private int depth = 0;
    private int commandLocation = 0;
    private int propertyReferenceLocation = 0;
    private boolean inIconName = false;
    
    public ParadoxLocalisationLexer(Project propect) {
        this((java.io.Reader)null);
        this.project = project;
    }
	
    private void increaseDepth(){
	    depth++;
    }
    
    private void decreaseDepth(){
	    if(depth > 0) depth--;
    }
    
    private int nextStateForText(){
      return depth <= 0 ? WAITING_RICH_TEXT : WAITING_COLORFUL_TEXT;
    }
    
    private int nextStateForCommand(){
      if(commandLocation == 0) return nextStateForText();
      else if (commandLocation == 1) return WAITING_PROPERTY_REFERENCE;
      else if (commandLocation == 2) return WAITING_ICON;
      else return nextStateForText();
    }
    
    private int nextStateForPropertyReference(){
      if(propertyReferenceLocation == 0) return nextStateForText();
      else if (propertyReferenceLocation == 2) return WAITING_ICON;
      else if (propertyReferenceLocation == 3) return WAITING_COMMAND_SCOPE_OR_FIELD;
      else return nextStateForText();
    }
    
    private boolean isPropertyReferenceStart(){
		  if(yylength() <= 1) return false;
	    char c = yycharat(yylength()-1);
	    return !Character.isWhitespace(c) && c != '"';
    }
    
    private boolean isIconStart(){
		  if(yylength() != 2) return false;
	    char c = yycharat(1);
	    return isExactLetter(c) || isExactDigit(c) || c == '_';
    }
    
    private boolean isCommandStart(){
		  if(yylength() <= 1) return false;
	    return yycharat(yylength()-1) == ']';
    }
    
    private boolean isColorfulTextStart(){
		  if(yylength() != 2) return false;
	    return isExactLetter(yycharat(1));
    }
    
    private boolean isRightQuote(){
		  if(yylength() == 1) return true;
	    return yycharat(yylength()-1) != '"';
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public ParadoxLocalisationLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** 
   * Unpacks the compressed character translation table.
   *
   * @param packed   the packed character translation table
   * @return         the unpacked character translation table
   */
  private static char [] zzUnpackCMap(String packed) {
    int size = 0;
    for (int i = 0, length = packed.length(); i < length; i += 2) {
      size += packed.charAt(i);
    }
    char[] map = new char[size];
    int i = 0;  /* index in packed string  */
    int j = 0;  /* index in unpacked array */
    while (i < packed.length()) {
      int  count = packed.charAt(i++);
      char value = packed.charAt(i++);
      do map[j++] = value; while (--count > 0);
    }
    return map;
  }

  public final int getTokenStart() {
    return zzStartRead;
  }

  public final int getTokenEnd() {
    return getTokenStart() + yylength();
  }

  public void reset(CharSequence buffer, int start, int end, int initialState) {
    zzBuffer = buffer;
    zzCurrentPos = zzMarkedPos = zzStartRead = start;
    zzAtEOF  = false;
    zzAtBOL = true;
    zzEndRead = end;
    yybegin(initialState);
  }

  /**
   * Refills the input buffer.
   *
   * @return      {@code false}, iff there was new input.
   *
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  private boolean zzRefill() throws java.io.IOException {
    return true;
  }


  /**
   * Returns the current lexical state.
   */
  public final int yystate() {
    return zzLexicalState;
  }


  /**
   * Enters a new lexical state
   *
   * @param newState the new lexical state
   */
  public final void yybegin(int newState) {
    zzLexicalState = newState;
  }


  /**
   * Returns the text matched by the current regular expression.
   */
  public final CharSequence yytext() {
    return zzBuffer.subSequence(zzStartRead, zzMarkedPos);
  }


  /**
   * Returns the character at position {@code pos} from the
   * matched text.
   *
   * It is equivalent to yytext().charAt(pos), but faster
   *
   * @param pos the position of the character to fetch.
   *            A value from 0 to yylength()-1.
   *
   * @return the character at position pos
   */
  public final char yycharat(int pos) {
    return zzBuffer.charAt(zzStartRead+pos);
  }


  /**
   * Returns the length of the matched text region.
   */
  public final int yylength() {
    return zzMarkedPos-zzStartRead;
  }


  /**
   * Reports an error that occurred while scanning.
   *
   * In a wellformed scanner (no or only correct usage of
   * yypushback(int) and a match-all fallback rule) this method
   * will only be called with things that "Can't Possibly Happen".
   * If this method is called, something is seriously wrong
   * (e.g. a JFlex bug producing a faulty scanner etc.).
   *
   * Usual syntax/scanner level error handling should be done
   * in error fallback rules.
   *
   * @param   errorCode  the code of the errormessage to display
   */
  private void zzScanError(int errorCode) {
    String message;
    try {
      message = ZZ_ERROR_MSG[errorCode];
    }
    catch (ArrayIndexOutOfBoundsException e) {
      message = ZZ_ERROR_MSG[ZZ_UNKNOWN_ERROR];
    }

    throw new Error(message);
  }


  /**
   * Pushes the specified amount of characters back into the input stream.
   *
   * They will be read again by then next call of the scanning method
   *
   * @param number  the number of characters to be read again.
   *                This number must not be greater than yylength()!
   */
  public void yypushback(int number)  {
    if ( number > yylength() )
      zzScanError(ZZ_PUSHBACK_2BIG);

    zzMarkedPos -= number;
  }


  /**
   * Resumes scanning until the next regular expression is matched,
   * the end of input is encountered or an I/O-Error occurs.
   *
   * @return      the next token
   * @exception   java.io.IOException  if any I/O-Error occurs
   */
  public IElementType advance() throws java.io.IOException {
    int zzInput;
    int zzAction;

    // cached fields:
    int zzCurrentPosL;
    int zzMarkedPosL;
    int zzEndReadL = zzEndRead;
    CharSequence zzBufferL = zzBuffer;

    int [] zzTransL = ZZ_TRANS;
    int [] zzRowMapL = ZZ_ROWMAP;
    int [] zzAttrL = ZZ_ATTRIBUTE;

    while (true) {
      zzMarkedPosL = zzMarkedPos;

      zzAction = -1;

      zzCurrentPosL = zzCurrentPos = zzStartRead = zzMarkedPosL;

      zzState = ZZ_LEXSTATE[zzLexicalState];

      // set up zzAction for empty match case:
      int zzAttributes = zzAttrL[zzState];
      if ( (zzAttributes & 1) == 1 ) {
        zzAction = zzState;
      }


      zzForAction: {
        while (true) {

          if (zzCurrentPosL < zzEndReadL) {
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
            zzCurrentPosL += Character.charCount(zzInput);
          }
          else if (zzAtEOF) {
            zzInput = YYEOF;
            break zzForAction;
          }
          else {
            // store back cached positions
            zzCurrentPos  = zzCurrentPosL;
            zzMarkedPos   = zzMarkedPosL;
            boolean eof = zzRefill();
            // get translated positions and possibly new buffer
            zzCurrentPosL  = zzCurrentPos;
            zzMarkedPosL   = zzMarkedPos;
            zzBufferL      = zzBuffer;
            zzEndReadL     = zzEndRead;
            if (eof) {
              zzInput = YYEOF;
              break zzForAction;
            }
            else {
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL/*, zzEndReadL*/);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + ZZ_CMAP(zzInput) ];
          if (zzNext == -1) break zzForAction;
          zzState = zzNext;

          zzAttributes = zzAttrL[zzState];
          if ( (zzAttributes & 1) == 1 ) {
            zzAction = zzState;
            zzMarkedPosL = zzCurrentPosL;
            if ( (zzAttributes & 8) == 8 ) break zzForAction;
          }

        }
      }

      // store back cached position
      zzMarkedPos = zzMarkedPosL;

      if (zzInput == YYEOF && zzStartRead == zzCurrentPos) {
        zzAtEOF = true;
        return null;
      }
      else {
        switch (zzAction < 0 ? zzAction : ZZ_ACTION[zzAction]) {
          case 1: 
            { return TokenType.BAD_CHARACTER;
            } 
            // fall through
          case 51: break;
          case 2: 
            { noIndent=false; return WHITE_SPACE;
            } 
            // fall through
          case 52: break;
          case 3: 
            { noIndent=true; return WHITE_SPACE;
            } 
            // fall through
          case 53: break;
          case 4: 
            { return COMMENT;
            } 
            // fall through
          case 54: break;
          case 5: 
            { yybegin(WAITING_PROPERTY_COLON); return PROPERTY_KEY_ID;
            } 
            // fall through
          case 55: break;
          case 6: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 56: break;
          case 7: 
            { noIndent=true; yybegin(YYINITIAL); return WHITE_SPACE;
            } 
            // fall through
          case 57: break;
          case 8: 
            { yybegin(WAITING_LOCALE_END); return COLON;
            } 
            // fall through
          case 58: break;
          case 9: 
            { return END_OF_LINE_COMMENT;
            } 
            // fall through
          case 59: break;
          case 10: 
            { yybegin(WAITING_PROPERTY_NUMBER); return COLON;
            } 
            // fall through
          case 60: break;
          case 11: 
            { yybegin(WAITING_PROPERTY_VALUE); return WHITE_SPACE;
            } 
            // fall through
          case 61: break;
          case 12: 
            { yybegin(WAITING_PROPERTY_VALUE); return NUMBER;
            } 
            // fall through
          case 62: break;
          case 13: 
            { yybegin(WAITING_RICH_TEXT); return LEFT_QUOTE;
            } 
            // fall through
          case 63: break;
          case 14: 
            { yypushback(yylength()); yybegin(WAITING_CHECK_RIGHT_QUOTE);
            } 
            // fall through
          case 64: break;
          case 15: 
            { return STRING_TOKEN;
            } 
            // fall through
          case 65: break;
          case 16: 
            { yypushback(yylength()); yybegin(WAITING_CHECK_COLORFUL_TEXT_START);
            } 
            // fall through
          case 66: break;
          case 17: 
            { commandLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);
            } 
            // fall through
          case 67: break;
          case 18: 
            { propertyReferenceLocation=0; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);
            } 
            // fall through
          case 68: break;
          case 19: 
            { yypushback(yylength()); yybegin(WAITING_CHECK_ICON_START);
            } 
            // fall through
          case 69: break;
          case 20: 
            { yybegin(nextStateForText()); return WHITE_SPACE;
            } 
            // fall through
          case 70: break;
          case 21: 
            { return PROPERTY_REFERENCE_ID;
            } 
            // fall through
          case 71: break;
          case 22: 
            { commandLocation=1; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);
            } 
            // fall through
          case 72: break;
          case 23: 
            { yybegin(nextStateForPropertyReference()); return PROPERTY_REFERENCE_END;
            } 
            // fall through
          case 73: break;
          case 24: 
            { yybegin(WAITING_PROPERTY_REFERENCE_PARAMETER_TOKEN); return PARAMETER_SEPARATOR;
            } 
            // fall through
          case 74: break;
          case 25: 
            { return PROPERTY_REFERENCE_PARAMETER_TOKEN;
            } 
            // fall through
          case 75: break;
          case 26: 
            { yybegin(WAITING_ICON_NAME_FINISHED); return ICON_ID;
            } 
            // fall through
          case 76: break;
          case 27: 
            { commandLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_COMMAND_START);
            } 
            // fall through
          case 77: break;
          case 28: 
            { propertyReferenceLocation=2; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);
            } 
            // fall through
          case 78: break;
          case 29: 
            { yybegin(nextStateForText()); return ICON_END;
            } 
            // fall through
          case 79: break;
          case 30: 
            { yybegin(WAITING_ICON_PARAMETER); return PARAMETER_SEPARATOR;
            } 
            // fall through
          case 80: break;
          case 31: 
            { return ICON_PARAMETER;
            } 
            // fall through
          case 81: break;
          case 32: 
            { yybegin(nextStateForCommand()); return COMMAND_END;
            } 
            // fall through
          case 82: break;
          case 33: 
            { propertyReferenceLocation=3; yypushback(yylength()); yybegin(WAITING_CHECK_PROPERTY_REFERENCE_START);
            } 
            // fall through
          case 83: break;
          case 34: 
            { yybegin(WAITING_COMMAND_SCOPE_OR_FIELD); return COMMAND_SEPARATOR;
            } 
            // fall through
          case 84: break;
          case 35: 
            { yypushback(yylength()); yybegin(WAITING_COLORFUL_TEXT);
            } 
            // fall through
          case 85: break;
          case 36: 
            { yybegin(WAITING_COLORFUL_TEXT); return WHITE_SPACE;
            } 
            // fall through
          case 86: break;
          case 37: 
            { yybegin(WAITING_COLORFUL_TEXT); return COLOR_ID;
            } 
            // fall through
          case 87: break;
          case 38: 
            { //特殊处理
    //如果匹配到的字符串长度大于1，且最后一个字符不为空白或双引号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isPropertyReferenceStart = isPropertyReferenceStart();
	yypushback(yylength()-1);
	if(isPropertyReferenceStart){
		yybegin(WAITING_PROPERTY_REFERENCE);
		return PROPERTY_REFERENCE_START;
	}else{
		yybegin(nextStateForText());
		return STRING_TOKEN;
	}
            } 
            // fall through
          case 88: break;
          case 39: 
            { //特殊处理
    //如果匹配到的字符串的第2个字符存在且为字母、数字或下划线，则认为代表图标的开始
    //否则认为是常规字符串
    boolean isIconStart = isIconStart();
    yypushback(yylength()-1);
    if(isIconStart){
    	  yybegin(WAITING_ICON);
    	  return ICON_START;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
            } 
            // fall through
          case 89: break;
          case 40: 
            { //特殊处理
    //除了可以通过连续的两个左方括号转义之外
    //如果匹配到的字符串长度大于1，且最后一个字符为右方括号，则认为代表命令的开始
    //否则认为是常规字符串
    boolean isCommandStart = isCommandStart();
    yypushback(yylength()-1);
    if(isCommandStart){
	    yybegin(WAITING_COMMAND_SCOPE_OR_FIELD);
	    return COMMAND_START;
    } else {
	    yybegin(nextStateForText());
	    return STRING_TOKEN;
    }
            } 
            // fall through
          case 90: break;
          case 41: 
            { //特殊处理
    //如果匹配到的字符串的第2个字符存在且为字母，则认为代表彩色文本的开始
    //否则认为是常规字符串
    boolean isColorfulTextStart = isColorfulTextStart();
    yypushback(yylength()-1);
    if(isColorfulTextStart){
        yybegin(WAITING_COLOR_ID);
        increaseDepth();
        return COLORFUL_TEXT_START;
    }else{
        yybegin(nextStateForText());
        return STRING_TOKEN;
    }
            } 
            // fall through
          case 91: break;
          case 42: 
            { //特殊处理
      //如果匹配到的字符串长度为1，或者最后一个字符不是双引号，则认为代表本地化富文本的结束
      //否则认为是常规字符串
      boolean isRightQuote = isRightQuote();
      yypushback(yylength()-1);
      if(isRightQuote){
          yybegin(WAITING_PROPERTY_END);
          return RIGHT_QUOTE;
      }else{
          yybegin(nextStateForText());
          return STRING_TOKEN;
      }
            } 
            // fall through
          case 92: break;
          case 43: 
            { return INVALID_ESCAPE_TOKEN;
            } 
            // fall through
          case 93: break;
          case 44: 
            { return VALID_ESCAPE_TOKEN;
            } 
            // fall through
          case 94: break;
          case 45: 
            { decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;
            } 
            // fall through
          case 95: break;
          case 46: 
            { return DOUBLE_LEFT_BRACKET;
            } 
            // fall through
          case 96: break;
          case 47: 
            { yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_FIELD_ID;
            } 
            // fall through
          case 97: break;
          case 48: 
            { yypushback(1); yybegin(WAITING_COMMAND_SEPARATOR); return COMMAND_SCOPE_ID;
            } 
            // fall through
          case 98: break;
          case 49: 
            { decreaseDepth(); decreaseDepth(); yybegin(nextStateForText()); return COLORFUL_TEXT_END;
            } 
            // fall through
          case 99: break;
          case 50: 
            { //同一本地化文件中是可以有多个locale的，这是为了兼容localisation/languages.yml
	//locale应该在之后的冒号和换行符之间没有任何字符或者只有空白字符
    CharSequence text = yytext();
    int length = text.length();
    int i = length - 2;
    while(i >= 0){
 	    char c = text.charAt(i);
 	    if(c == ':') {
 		    int pushback = length - i;
			yypushback(pushback);
			//locale之前必须没有任何缩进
			if(noIndent){
 		        yybegin(WAITING_LOCALE_COLON);
 		        return LOCALE_ID;
			} else {
				yybegin(WAITING_PROPERTY_COLON);
				return PROPERTY_KEY_ID;
			}
 	    }
 	    i--;
    }
    return TokenType.BAD_CHARACTER; //不应该出现
            } 
            // fall through
          case 100: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
