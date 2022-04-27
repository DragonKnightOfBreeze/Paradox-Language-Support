/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package icu.windea.pls.script.psi;

import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>ParadoxScriptLexer.flex</tt>
 */
public class ParadoxScriptLexer implements com.intellij.lexer.FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int WAITING_VARIABLE_EQUAL_SIGN = 2;
  public static final int WAITING_VARIABLE_VALUE = 4;
  public static final int WAITING_VARIABLE_END = 6;
  public static final int WAITING_PROPERTY = 8;
  public static final int WAITING_PROPERTY_KEY = 10;
  public static final int WATIING_PROPERTY_SEPARATOR = 12;
  public static final int WAITING_PROPERTY_VALUE = 14;
  public static final int WAITING_PROPERTY_END = 16;
  public static final int WAITING_CODE = 18;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9, 9
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [10, 6, 5]
   * Total runtime size is 4224 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[(ZZ_CMAP_Z[ch>>11]<<6)|((ch>>5)&0x3f)]<<5)|(ch&0x1f)];
  }

  /* The ZZ_CMAP_Z table has 544 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\1\4\1\5\1\6\15\5\1\7\1\10\11\5\1\11\1\12\1\5\1\13\1\14\11"+
    "\5\1\15\14\5\1\16\2\5\1\17\u01e2\5");

  /* The ZZ_CMAP_Y table has 1024 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\1\2\1\3\1\4\1\5\55\6\1\7\3\6\1\10\6\6\1\7\14\6\1\11\3\6\1\11\3\6"+
    "\1\11\3\6\1\11\3\6\1\11\3\6\1\11\3\6\1\11\3\6\1\11\3\6\1\11\3\6\1\11\2\6\1"+
    "\10\3\6\1\10\2\6\1\7\10\6\1\7\1\6\1\10\57\6\1\5\12\6\1\7\1\10\11\6\1\11\3"+
    "\6\1\10\5\6\1\12\5\6\1\10\2\6\1\10\4\6\1\12\35\6\1\13\1\14\1\15\175\6\1\5"+
    "\160\6\1\7\24\6\1\10\1\6\1\7\5\6\2\10\2\6\1\10\14\6\1\10\130\6\1\10\54\6\1"+
    "\7\35\6\1\11\3\6\1\10\1\6\1\16\4\6\1\10\10\6\1\10\12\6\1\10\3\6\1\10\13\6"+
    "\1\10\3\6\1\7\2\6\1\10\15\6\1\7\32\6\1\10\60\6\1\7\6\6\1\10\143\6\1\17\1\20"+
    "\12\6\1\10\65\6");

  /* The ZZ_CMAP_A table has 544 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\4\1\3\2\2\1\3\22\0\1\4\1\0\1\11\1\5\4\0\1\12\2\0\1\21\1\0\1\22\1\25"+
    "\1\0\1\23\11\24\2\0\1\42\1\10\1\40\1\0\1\6\32\7\1\41\1\13\1\37\1\0\1\7\1\0"+
    "\1\31\1\30\2\7\1\15\1\7\1\27\1\32\3\7\1\33\1\7\1\17\1\20\2\7\1\26\1\16\2\7"+
    "\1\33\2\7\1\14\1\7\1\34\1\0\1\36\7\0\1\2\32\0\1\1\77\0\12\35\46\0\12\35\14"+
    "\0\12\35\20\0\12\35\6\0\12\35\6\0\13\1\35\0\2\2\5\0\1\1\57\0\1\1\26\0\12\35"+
    "\16\0\62\35");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\12\0\1\1\1\2\2\3\1\4\3\2\3\1\2\5"+
    "\2\1\1\6\1\7\1\10\1\11\2\12\1\2\3\12"+
    "\2\13\1\2\1\14\1\3\2\15\1\2\1\16\1\17"+
    "\1\20\1\1\1\2\3\1\2\5\2\1\1\21\2\22"+
    "\1\14\1\6\1\23\1\0\1\24\2\0\1\25\3\0"+
    "\1\24\1\26\3\0\1\1\1\27\3\1\1\0\1\30"+
    "\1\0\1\12\1\31\1\12\2\0\1\15\1\32\1\33"+
    "\1\34\1\35\1\0\1\26\1\0\1\1\1\27\3\1"+
    "\1\36\1\26\2\0\1\24\1\0\1\37\2\1\1\40"+
    "\1\41\1\0\1\37\2\1\3\0\1\42";

  private static int [] zzUnpackAction() {
    int [] result = new int[119];
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
    "\0\0\0\43\0\106\0\151\0\214\0\257\0\322\0\365"+
    "\0\u0118\0\u013b\0\u015e\0\u0181\0\u01a4\0\u01c7\0\u01ea\0\u020d"+
    "\0\u0230\0\u0253\0\u0276\0\u0299\0\u02bc\0\u02df\0\u0302\0\u0325"+
    "\0\u0348\0\u0230\0\u0230\0\u036b\0\u0230\0\u038e\0\u03b1\0\u03d4"+
    "\0\u03f7\0\u041a\0\u043d\0\u0460\0\u0483\0\u04a6\0\u04c9\0\u04ec"+
    "\0\u050f\0\u0532\0\u0555\0\u0230\0\u0578\0\u059b\0\u05be\0\u05e1"+
    "\0\u0604\0\u0627\0\u064a\0\u066d\0\u0690\0\u06b3\0\u06d6\0\u06f9"+
    "\0\u071c\0\u073f\0\u0762\0\u071c\0\u0230\0\u0785\0\u0230\0\u07a8"+
    "\0\u0181\0\u07cb\0\u07ee\0\u0253\0\u0811\0\u0834\0\u07a8\0\u0857"+
    "\0\u087a\0\u0834\0\u089d\0\u015e\0\u08c0\0\u08e3\0\u0906\0\u03d4"+
    "\0\u0230\0\u0929\0\u094c\0\u038e\0\u096f\0\u04a6\0\u0992\0\u09b5"+
    "\0\u09d8\0\u0230\0\u0230\0\u0230\0\u05e1\0\u0230\0\u09fb\0\u0a1e"+
    "\0\u05be\0\u0a41\0\u0a64\0\u0a87\0\u0230\0\u0aaa\0\u0acd\0\u0af0"+
    "\0\u05e1\0\u0b13\0\u08c0\0\u0b36\0\u0b59\0\u096f\0\u0230\0\u0b7c"+
    "\0\u0a41\0\u0b9f\0\u0bc2\0\u0be5\0\u0c08\0\u0c2b\0\u0230";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[119];
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
    "\1\13\1\14\2\15\1\16\1\17\1\20\1\13\1\21"+
    "\1\22\2\13\1\23\2\13\1\24\1\13\2\25\1\26"+
    "\1\27\1\13\1\30\3\13\1\31\1\13\1\32\1\13"+
    "\1\33\4\13\1\21\1\14\2\15\1\16\1\34\2\21"+
    "\1\35\23\21\1\32\1\21\1\33\4\21\1\36\1\14"+
    "\2\15\1\16\1\37\1\21\1\36\1\21\1\40\2\36"+
    "\1\41\2\36\1\42\1\36\2\43\1\44\1\45\7\36"+
    "\1\32\1\36\1\33\4\36\1\21\1\46\2\47\1\50"+
    "\1\34\26\21\1\32\1\21\1\33\4\21\1\51\6\21"+
    "\1\51\1\21\1\52\22\51\1\21\1\51\1\21\4\51"+
    "\1\13\1\14\2\15\1\16\1\17\1\53\1\13\1\21"+
    "\1\22\2\13\1\23\2\13\1\24\1\13\2\25\1\26"+
    "\1\27\1\13\1\30\3\13\1\31\1\13\1\32\1\13"+
    "\1\33\4\13\1\21\1\14\2\15\1\16\1\34\2\21"+
    "\1\54\23\21\1\32\1\21\1\33\1\21\1\55\1\21"+
    "\1\56\1\57\1\14\2\15\1\16\1\34\1\53\1\57"+
    "\1\21\1\60\2\57\1\61\2\57\1\62\1\57\2\63"+
    "\1\64\1\65\1\57\1\66\3\57\1\67\1\57\1\32"+
    "\1\57\1\33\4\57\1\21\1\46\2\47\1\70\1\34"+
    "\26\21\1\32\1\21\1\33\4\21\1\71\1\72\1\73"+
    "\1\47\1\72\27\71\1\74\1\71\1\33\1\75\3\71"+
    "\1\13\4\76\3\13\1\77\1\100\22\13\1\0\1\13"+
    "\1\0\4\13\1\0\1\101\2\15\1\101\37\0\4\15"+
    "\37\0\1\101\2\15\1\16\36\0\3\17\1\0\37\17"+
    "\7\0\1\102\3\0\1\103\5\102\1\0\3\102\1\0"+
    "\6\102\52\0\1\104\2\105\1\76\1\105\3\104\1\106"+
    "\1\107\1\110\1\111\20\104\1\112\1\104\1\112\4\104"+
    "\1\13\4\76\3\13\1\77\1\100\3\13\1\113\16\13"+
    "\1\0\1\13\1\0\5\13\4\76\3\13\1\77\1\100"+
    "\6\13\1\114\13\13\1\0\1\13\1\0\5\13\4\76"+
    "\3\13\1\77\1\100\11\13\1\26\1\27\7\13\1\0"+
    "\1\13\1\0\5\13\4\76\3\13\1\77\1\100\13\13"+
    "\1\115\6\13\1\0\1\13\1\0\5\13\4\76\3\13"+
    "\1\77\1\100\11\13\2\27\1\115\6\13\1\0\1\13"+
    "\1\0\5\13\4\76\3\13\1\77\1\100\15\13\1\116"+
    "\4\13\1\0\1\13\1\0\5\13\4\76\3\13\1\77"+
    "\1\100\4\13\1\117\15\13\1\0\1\13\1\0\4\13"+
    "\3\34\1\0\37\34\1\36\4\0\3\36\2\0\22\36"+
    "\1\0\1\36\1\0\4\36\1\37\2\34\1\0\1\34"+
    "\3\37\2\34\22\37\1\34\1\37\1\34\4\37\3\120"+
    "\1\0\5\120\1\121\1\120\1\122\27\120\1\36\4\0"+
    "\3\36\2\0\3\36\1\123\16\36\1\0\1\36\1\0"+
    "\5\36\4\0\3\36\2\0\6\36\1\124\13\36\1\0"+
    "\1\36\1\0\5\36\4\0\3\36\2\0\11\36\1\44"+
    "\1\45\7\36\1\0\1\36\1\0\5\36\4\0\3\36"+
    "\2\0\13\36\1\125\6\36\1\0\1\36\1\0\5\36"+
    "\4\0\3\36\2\0\11\36\2\45\1\125\6\36\1\0"+
    "\1\36\1\0\4\36\1\0\1\126\2\47\1\126\37\0"+
    "\4\47\37\0\1\126\2\47\1\50\36\0\1\51\4\0"+
    "\3\51\1\0\23\51\1\0\1\51\1\0\4\51\1\52"+
    "\2\127\1\0\1\127\3\52\1\127\2\51\1\130\20\52"+
    "\1\127\1\52\1\127\4\52\7\0\1\131\3\0\1\103"+
    "\5\131\1\0\3\131\1\0\6\131\17\0\1\132\42\0"+
    "\1\133\27\0\1\134\2\0\1\57\4\0\3\57\2\0"+
    "\22\57\1\0\1\57\1\0\4\57\3\135\1\0\5\135"+
    "\1\136\1\135\1\137\27\135\1\57\4\0\3\57\2\0"+
    "\3\57\1\140\16\57\1\0\1\57\1\0\5\57\4\0"+
    "\3\57\2\0\6\57\1\141\13\57\1\0\1\57\1\0"+
    "\5\57\4\0\3\57\2\0\11\57\1\64\1\65\7\57"+
    "\1\0\1\57\1\0\5\57\4\0\3\57\2\0\13\57"+
    "\1\142\6\57\1\0\1\57\1\0\5\57\4\0\3\57"+
    "\2\0\11\57\2\65\1\142\6\57\1\0\1\57\1\0"+
    "\5\57\4\0\3\57\2\0\15\57\1\143\4\57\1\0"+
    "\1\57\1\0\5\57\4\0\3\57\2\0\4\57\1\144"+
    "\15\57\1\0\1\57\1\0\4\57\1\0\1\126\2\47"+
    "\1\70\36\0\3\71\1\0\32\71\2\0\4\71\1\72"+
    "\1\73\1\47\1\72\31\71\2\0\4\71\2\73\1\47"+
    "\1\73\31\71\2\0\3\71\1\0\4\76\3\0\1\77"+
    "\27\0\1\77\1\0\1\77\1\100\4\76\3\100\1\77"+
    "\23\100\1\0\1\100\1\0\4\100\7\0\1\102\4\0"+
    "\5\102\1\0\3\102\1\0\6\102\50\0\1\145\1\0"+
    "\1\112\2\105\1\76\1\105\3\112\1\106\1\146\1\135"+
    "\1\147\24\112\1\106\1\112\1\106\3\112\1\0\5\112"+
    "\1\146\1\135\1\147\27\112\1\110\2\150\1\76\1\150"+
    "\3\110\1\151\1\107\1\110\1\152\20\110\1\135\1\110"+
    "\1\135\4\110\1\104\1\105\2\76\1\105\3\104\1\106"+
    "\23\104\1\112\1\104\1\112\4\104\1\13\4\76\3\13"+
    "\1\77\1\100\4\13\1\114\15\13\1\0\1\13\1\0"+
    "\5\13\4\76\3\13\1\77\1\100\11\13\2\153\7\13"+
    "\1\0\1\13\1\0\5\13\4\76\3\13\1\77\1\100"+
    "\16\13\1\154\3\13\1\0\1\13\1\0\5\13\4\76"+
    "\3\13\1\77\1\100\16\13\1\155\2\13\1\155\1\0"+
    "\1\13\1\0\4\13\2\120\2\0\37\120\1\36\4\0"+
    "\3\36\2\0\4\36\1\124\15\36\1\0\1\36\1\0"+
    "\5\36\4\0\3\36\2\0\11\36\2\156\7\36\1\0"+
    "\1\36\1\0\4\36\3\127\1\0\5\127\1\157\1\0"+
    "\1\160\27\127\1\52\1\127\2\0\1\127\3\52\1\127"+
    "\23\52\1\127\1\52\1\127\4\52\7\0\1\131\4\0"+
    "\5\131\1\0\3\131\1\0\6\131\7\0\2\135\2\0"+
    "\37\135\1\57\4\0\3\57\2\0\4\57\1\141\15\57"+
    "\1\0\1\57\1\0\5\57\4\0\3\57\2\0\11\57"+
    "\2\161\7\57\1\0\1\57\1\0\5\57\4\0\3\57"+
    "\2\0\16\57\1\162\3\57\1\0\1\57\1\0\5\57"+
    "\4\0\3\57\2\0\16\57\1\163\2\57\1\163\1\0"+
    "\1\57\1\0\4\57\1\0\4\76\3\0\1\77\32\0"+
    "\2\112\2\0\37\112\1\135\2\150\1\76\1\150\3\135"+
    "\1\151\1\136\1\135\1\137\24\135\1\151\1\135\1\151"+
    "\1\110\1\150\2\76\1\150\3\110\1\151\23\110\1\135"+
    "\1\110\1\135\4\110\1\13\3\76\1\164\3\13\1\77"+
    "\1\100\17\13\1\155\2\13\1\165\1\13\1\0\5\13"+
    "\3\76\1\164\3\13\1\77\1\100\22\13\1\165\1\13"+
    "\1\0\4\13\2\127\2\0\37\127\1\57\3\0\1\166"+
    "\3\57\2\0\17\57\1\163\2\57\1\165\1\57\1\0"+
    "\5\57\3\0\1\166\3\57\2\0\22\57\1\165\1\57"+
    "\1\0\4\57\1\0\3\76\1\164\3\0\1\77\23\0"+
    "\1\165\3\0\1\77\1\0\1\77\4\0\1\165\16\0"+
    "\3\165\7\0\1\165\1\167\10\0\1\166\27\0\1\165"+
    "\6\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3150];
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
    "\12\0\6\1\1\11\10\1\2\11\1\1\1\11\16\1"+
    "\1\11\20\1\1\11\1\0\1\11\2\0\1\1\3\0"+
    "\2\1\3\0\5\1\1\0\1\11\1\0\3\1\2\0"+
    "\2\1\3\11\1\0\1\11\1\0\5\1\1\11\1\1"+
    "\2\0\1\1\1\0\4\1\1\11\1\0\3\1\3\0"+
    "\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[119];
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
  private int depth = 0;

  public int nextState(){
	  return depth <= 0 ? YYINITIAL : WAITING_PROPERTY_KEY;
}


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public ParadoxScriptLexer(java.io.Reader in) {
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
            { yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;
            } 
            // fall through
          case 35: break;
          case 2: 
            { return BAD_CHARACTER;
            } 
            // fall through
          case 36: break;
          case 3: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 37: break;
          case 4: 
            { return COMMENT;
            } 
            // fall through
          case 38: break;
          case 5: 
            { yybegin(WAITING_PROPERTY_END); return INT_TOKEN;
            } 
            // fall through
          case 39: break;
          case 6: 
            { depth++; yybegin(nextState()); return LEFT_BRACE;
            } 
            // fall through
          case 40: break;
          case 7: 
            { depth--; yybegin(nextState()); return RIGHT_BRACE;
            } 
            // fall through
          case 41: break;
          case 8: 
            { return END_OF_LINE_COMMENT;
            } 
            // fall through
          case 42: break;
          case 9: 
            { yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 43: break;
          case 10: 
            { yybegin(WAITING_VARIABLE_END); return STRING_TOKEN;
            } 
            // fall through
          case 44: break;
          case 11: 
            { yybegin(WAITING_VARIABLE_END); return INT_TOKEN;
            } 
            // fall through
          case 45: break;
          case 12: 
            { yybegin(nextState()); return WHITE_SPACE;
            } 
            // fall through
          case 46: break;
          case 13: 
            { yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_ID;
            } 
            // fall through
          case 47: break;
          case 14: 
            { yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 48: break;
          case 15: 
            { yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;
            } 
            // fall through
          case 49: break;
          case 16: 
            { yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;
            } 
            // fall through
          case 50: break;
          case 17: 
            { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE;
            } 
            // fall through
          case 51: break;
          case 18: 
            { return CODE_TEXT_TOKEN;
            } 
            // fall through
          case 52: break;
          case 19: 
            { yybegin(WAITING_PROPERTY_END); return CODE_END;
            } 
            // fall through
          case 53: break;
          case 20: 
            { yypushback(yylength()); yybegin(WAITING_PROPERTY);
            } 
            // fall through
          case 54: break;
          case 21: 
            { yybegin(WAITING_VARIABLE_EQUAL_SIGN); return VARIABLE_NAME_ID;
            } 
            // fall through
          case 55: break;
          case 22: 
            { yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;
            } 
            // fall through
          case 56: break;
          case 23: 
            { yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 57: break;
          case 24: 
            { yybegin(WAITING_VARIABLE_END); return QUOTED_STRING_TOKEN;
            } 
            // fall through
          case 58: break;
          case 25: 
            { yybegin(WAITING_VARIABLE_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 59: break;
          case 26: 
            { yybegin(WAITING_PROPERTY_END); return VARIABLE_REFERENCE_ID;
            } 
            // fall through
          case 60: break;
          case 27: 
            { yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;
            } 
            // fall through
          case 61: break;
          case 28: 
            { yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;
            } 
            // fall through
          case 62: break;
          case 29: 
            { yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            } 
            // fall through
          case 63: break;
          case 30: 
            { yybegin(WAITING_CODE); return CODE_START;
            } 
            // fall through
          case 64: break;
          case 31: 
            { yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 65: break;
          case 32: 
            { yybegin(WAITING_VARIABLE_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 66: break;
          case 33: 
            { yybegin(WATIING_PROPERTY_SEPARATOR); return QUOTED_PROPERTY_KEY_ID;
            } 
            // fall through
          case 67: break;
          case 34: 
            { yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;
            } 
            // fall through
          case 68: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
