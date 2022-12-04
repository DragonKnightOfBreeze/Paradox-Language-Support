/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package icu.windea.pls.cwt.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;


/**
 * This class is a scanner generated by 
 * <a href="http://www.jflex.de/">JFlex</a> 1.7.0
 * from the specification file <tt>CwtLexer.flex</tt>
 */
public class CwtLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int WAITING_PROPERTY_KEY = 2;
  public static final int WATIING_PROPERTY_SEPARATOR = 4;
  public static final int WAITING_PROPERTY_VALUE = 6;
  public static final int WAITING_PROPERTY_VALUE_END = 8;
  public static final int WAITING_PROPERTY_END = 10;
  public static final int WAITING_OPTION = 12;
  public static final int WAITING_OPTION_TOP_STRING = 14;
  public static final int WAITING_OPTION_KEY = 16;
  public static final int WATIING_OPTION_SEPARATOR = 18;
  public static final int WAITING_OPTION_VALUE = 20;
  public static final int WAITING_OPTION_VALUE_TOP_STRING = 22;
  public static final int WAITING_OPTION_VALUE_END = 24;
  public static final int WAITING_OPTION_END = 26;
  public static final int WAITING_DOCUMENTATION = 28;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9,  9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14
  };

  /** 
   * Translates characters to character classes
   * Chosen bits are [7, 7, 7]
   * Total runtime size is 1928 bytes
   */
  public static int ZZ_CMAP(int ch) {
    return ZZ_CMAP_A[(ZZ_CMAP_Y[ZZ_CMAP_Z[ch>>14]|((ch>>7)&0x7f)]<<7)|(ch&0x7f)];
  }

  /* The ZZ_CMAP_Z table has 68 entries */
  static final char ZZ_CMAP_Z[] = zzUnpackCMap(
    "\1\0\103\200");

  /* The ZZ_CMAP_Y table has 256 entries */
  static final char ZZ_CMAP_Y[] = zzUnpackCMap(
    "\1\0\1\1\53\2\1\3\22\2\1\4\37\2\1\3\237\2");

  /* The ZZ_CMAP_A table has 640 entries */
  static final char ZZ_CMAP_A[] = zzUnpackCMap(
    "\11\0\1\1\1\3\2\2\1\3\22\0\1\1\1\6\1\11\1\4\7\0\1\21\1\0\1\21\1\24\1\0\1\22"+
    "\11\23\2\0\1\7\1\5\1\10\35\0\1\13\10\0\1\15\10\0\1\17\1\20\3\0\1\16\5\0\1"+
    "\14\1\0\1\25\1\0\1\12\7\0\1\2\32\0\1\1\337\0\1\1\177\0\13\1\35\0\2\2\5\0\1"+
    "\1\57\0\1\1\40\0");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\17\0\1\1\2\2\1\3\1\4\1\1\1\5\1\6"+
    "\3\1\2\7\1\10\1\11\1\2\2\12\2\5\1\13"+
    "\1\14\1\3\1\15\2\5\1\16\1\2\1\12\1\3"+
    "\1\16\1\5\3\16\2\17\1\12\1\20\1\21\1\2"+
    "\1\12\1\3\1\22\1\21\1\5\1\23\3\21\2\24"+
    "\1\25\2\26\1\27\1\5\1\30\1\31\1\32\2\5"+
    "\2\33\1\5\3\33\2\34\2\35\1\36\1\12\1\37"+
    "\2\20\1\40\4\0\1\4\1\0\1\1\1\0\1\1"+
    "\1\41\1\1\2\0\1\11\1\0\1\15\1\42\1\43"+
    "\1\4\1\0\1\16\1\0\1\16\1\44\1\16\3\0"+
    "\1\22\1\0\1\21\1\0\1\21\1\45\1\21\2\0"+
    "\1\27\1\0\1\32\1\46\1\22\1\0\1\33\1\0"+
    "\1\33\1\47\1\33\2\0\1\50\1\51\1\52\1\53"+
    "\1\54\1\55";

  private static int [] zzUnpackAction() {
    int [] result = new int[149];
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
    "\0\0\0\26\0\54\0\102\0\130\0\156\0\204\0\232"+
    "\0\260\0\306\0\334\0\362\0\u0108\0\u011e\0\u0134\0\u014a"+
    "\0\u0160\0\u0176\0\u018c\0\u01a2\0\u01b8\0\u01ce\0\u01a2\0\u01e4"+
    "\0\u01fa\0\u0210\0\u0226\0\u023c\0\u01a2\0\u0252\0\u0268\0\u0268"+
    "\0\u027e\0\u01a2\0\u0294\0\u01a2\0\u01a2\0\u02aa\0\u02c0\0\u02d6"+
    "\0\u02ec\0\u0302\0\u0318\0\u0318\0\u032e\0\u0344\0\u035a\0\u0370"+
    "\0\u0386\0\u039c\0\u03b2\0\u03c8\0\u03de\0\u03f4\0\u040a\0\u0420"+
    "\0\u0420\0\u0436\0\u01a2\0\u044c\0\u0462\0\u01a2\0\u0478\0\u048e"+
    "\0\u04a4\0\u04ba\0\u04d0\0\u01a2\0\u04e6\0\u01a2\0\u04fc\0\u0512"+
    "\0\u01a2\0\u01a2\0\u0528\0\u053e\0\u0554\0\u056a\0\u0580\0\u0596"+
    "\0\u05ac\0\u05c2\0\u05d8\0\u05ee\0\u0604\0\u061a\0\u01a2\0\u0630"+
    "\0\u0630\0\u0646\0\u0646\0\u065c\0\u0672\0\u0688\0\u069e\0\u06b4"+
    "\0\u06ca\0\u014a\0\u01ce\0\u0688\0\u06e0\0\u06f6\0\u014a\0\u070c"+
    "\0\u027e\0\u0294\0\u01a2\0\u0722\0\u01a2\0\u01a2\0\u0738\0\u0302"+
    "\0\u035a\0\u0688\0\u074e\0\u0764\0\u0302\0\u077a\0\u0790\0\u07a6"+
    "\0\u07bc\0\u040a\0\u0462\0\u0790\0\u07d2\0\u07e8\0\u040a\0\u07fe"+
    "\0\u04e6\0\u0512\0\u01a2\0\u0814\0\u01a2\0\u01a2\0\u056a\0\u0596"+
    "\0\u0790\0\u082a\0\u0840\0\u056a\0\u0856\0\u061a\0\u065c\0\u01a2"+
    "\0\u01a2\0\u070c\0\u077a\0\u07fe\0\u0856";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[149];
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
    "\1\20\2\21\1\22\1\23\1\24\1\20\1\25\1\20"+
    "\1\26\1\27\1\20\1\30\2\20\1\31\1\20\1\32"+
    "\1\33\1\34\1\20\1\35\1\36\1\37\1\40\1\41"+
    "\2\42\3\36\1\43\1\44\12\36\1\45\1\42\1\37"+
    "\1\40\1\41\1\46\1\47\1\50\1\51\2\42\1\44"+
    "\12\42\1\45\1\52\1\53\1\54\1\41\1\55\1\24"+
    "\1\52\1\56\1\52\1\57\1\44\1\52\1\60\2\52"+
    "\1\61\1\52\1\62\1\63\1\64\1\52\1\45\1\42"+
    "\3\65\1\46\5\42\1\44\12\42\1\45\1\42\3\66"+
    "\1\46\5\42\1\44\12\42\1\45\1\67\1\70\1\71"+
    "\1\41\1\72\1\73\1\67\1\74\1\67\1\75\1\76"+
    "\1\67\1\77\2\67\1\100\1\67\1\101\1\102\1\103"+
    "\1\67\1\104\1\105\3\42\1\105\1\106\4\105\1\106"+
    "\12\105\1\106\1\107\1\37\1\40\1\41\1\72\1\42"+
    "\3\107\1\110\1\111\12\107\1\112\1\42\1\37\1\40"+
    "\1\41\1\72\1\113\1\114\1\115\2\42\1\111\12\42"+
    "\1\112\1\116\1\70\1\71\1\41\1\23\1\73\1\116"+
    "\1\117\1\116\1\120\1\111\1\116\1\121\2\116\1\122"+
    "\1\116\1\123\1\124\1\125\1\116\1\112\1\126\3\42"+
    "\1\126\1\127\4\126\1\127\12\126\1\127\1\42\1\130"+
    "\1\131\1\41\1\72\5\42\1\111\12\42\1\112\1\42"+
    "\1\132\1\133\1\134\1\72\5\42\1\111\12\42\1\112"+
    "\1\135\1\37\1\40\1\41\1\72\21\135\1\20\2\136"+
    "\1\0\1\20\1\24\1\20\1\25\2\20\1\0\12\20"+
    "\2\0\2\21\1\22\1\0\1\24\1\137\1\140\17\0"+
    "\3\22\22\0\3\72\1\0\1\141\21\72\26\0\1\20"+
    "\2\136\1\0\1\20\1\24\1\20\1\25\1\142\1\20"+
    "\1\0\12\20\1\0\3\143\1\0\5\143\1\144\1\143"+
    "\1\145\12\143\1\20\2\136\1\0\1\20\1\24\1\20"+
    "\1\25\2\20\1\0\2\20\1\146\7\20\1\0\1\20"+
    "\2\136\1\0\1\20\1\24\1\20\1\25\2\20\1\0"+
    "\5\20\1\147\4\20\1\0\1\20\2\136\1\0\1\20"+
    "\1\24\1\20\1\25\2\20\1\0\7\20\1\33\1\34"+
    "\1\20\1\0\1\20\2\136\1\0\1\20\1\24\1\20"+
    "\1\25\2\20\1\0\11\20\1\150\1\0\1\20\2\136"+
    "\1\0\1\20\1\24\1\20\1\25\2\20\1\0\7\20"+
    "\2\34\1\150\1\0\1\36\3\0\1\36\1\0\4\36"+
    "\1\0\12\36\2\0\1\37\1\40\1\41\23\0\1\151"+
    "\2\41\22\0\3\152\1\0\5\152\1\153\1\152\1\154"+
    "\12\152\3\72\2\0\21\72\5\0\1\155\25\0\1\156"+
    "\30\0\1\156\15\0\1\52\2\136\1\0\1\52\1\24"+
    "\1\52\1\56\2\52\1\0\12\52\2\0\1\53\1\54"+
    "\1\41\1\0\1\24\1\137\1\140\16\0\3\72\1\0"+
    "\1\157\21\72\1\52\2\136\1\0\1\52\1\24\1\52"+
    "\1\56\1\160\1\52\1\0\12\52\1\0\3\161\1\0"+
    "\5\161\1\162\1\161\1\163\12\161\1\52\2\136\1\0"+
    "\1\52\1\24\1\52\1\56\2\52\1\0\2\52\1\164"+
    "\7\52\1\0\1\52\2\136\1\0\1\52\1\24\1\52"+
    "\1\56\2\52\1\0\5\52\1\165\4\52\1\0\1\52"+
    "\2\136\1\0\1\52\1\24\1\52\1\56\2\52\1\0"+
    "\7\52\1\63\1\64\1\52\1\0\1\52\2\136\1\0"+
    "\1\52\1\24\1\52\1\56\2\52\1\0\11\52\1\166"+
    "\1\0\1\52\2\136\1\0\1\52\1\24\1\52\1\56"+
    "\2\52\1\0\7\52\2\64\1\166\2\0\3\65\23\0"+
    "\3\66\22\0\1\67\2\167\1\0\1\67\1\73\1\67"+
    "\1\74\2\67\1\0\12\67\2\0\1\70\1\71\1\41"+
    "\1\0\1\73\1\170\1\171\16\0\3\72\1\0\22\72"+
    "\1\67\2\167\1\0\1\67\1\73\1\67\1\74\1\172"+
    "\1\67\1\0\12\67\1\0\3\173\1\0\5\173\1\174"+
    "\1\173\1\175\12\173\1\67\2\167\1\0\1\67\1\73"+
    "\1\67\1\74\2\67\1\0\2\67\1\176\7\67\1\0"+
    "\1\67\2\167\1\0\1\67\1\73\1\67\1\74\2\67"+
    "\1\0\5\67\1\177\4\67\1\0\1\67\2\167\1\0"+
    "\1\67\1\73\1\67\1\74\2\67\1\0\7\67\1\102"+
    "\1\103\1\67\1\0\1\67\2\167\1\0\1\67\1\73"+
    "\1\67\1\74\2\67\1\0\11\67\1\200\1\0\1\67"+
    "\2\167\1\0\1\67\1\73\1\67\1\74\2\67\1\0"+
    "\7\67\2\103\1\200\1\0\1\105\2\201\1\0\1\105"+
    "\1\0\4\105\1\0\12\105\1\0\1\107\3\0\1\107"+
    "\1\0\4\107\1\0\12\107\1\0\3\202\1\0\5\202"+
    "\1\203\1\202\1\204\12\202\5\0\1\205\25\0\1\206"+
    "\30\0\1\206\15\0\1\116\2\167\1\0\1\116\1\73"+
    "\1\116\1\117\2\116\1\0\12\116\1\0\1\116\2\167"+
    "\1\0\1\116\1\73\1\116\1\117\1\207\1\116\1\0"+
    "\12\116\1\0\3\210\1\0\5\210\1\211\1\210\1\212"+
    "\12\210\1\116\2\167\1\0\1\116\1\73\1\116\1\117"+
    "\2\116\1\0\2\116\1\213\7\116\1\0\1\116\2\167"+
    "\1\0\1\116\1\73\1\116\1\117\2\116\1\0\5\116"+
    "\1\214\4\116\1\0\1\116\2\167\1\0\1\116\1\73"+
    "\1\116\1\117\2\116\1\0\7\116\1\124\1\125\1\116"+
    "\1\0\1\116\2\167\1\0\1\116\1\73\1\116\1\117"+
    "\2\116\1\0\11\116\1\215\1\0\1\116\2\167\1\0"+
    "\1\116\1\73\1\116\1\117\2\116\1\0\7\116\2\125"+
    "\1\215\1\0\1\126\2\216\1\0\1\126\1\0\4\126"+
    "\1\0\12\126\2\0\1\130\1\131\1\41\23\0\1\132"+
    "\1\133\1\134\23\0\1\217\2\134\22\0\3\135\1\0"+
    "\22\135\1\0\2\136\2\0\1\24\1\137\1\140\23\0"+
    "\1\24\30\0\1\24\15\0\4\220\1\221\21\220\2\143"+
    "\2\0\22\143\1\20\2\136\1\0\1\20\1\24\1\20"+
    "\1\25\2\20\1\0\3\20\1\147\6\20\1\0\1\20"+
    "\2\136\1\0\1\20\1\24\1\20\1\25\2\20\1\0"+
    "\7\20\2\222\1\20\1\0\2\152\2\0\22\152\4\0"+
    "\1\221\21\0\2\161\2\0\22\161\1\52\2\136\1\0"+
    "\1\52\1\24\1\52\1\56\2\52\1\0\3\52\1\165"+
    "\6\52\1\0\1\52\2\136\1\0\1\52\1\24\1\52"+
    "\1\56\2\52\1\0\7\52\2\223\1\52\2\0\2\167"+
    "\2\0\1\73\1\170\1\171\23\0\1\73\30\0\1\73"+
    "\15\0\2\173\2\0\22\173\1\67\2\167\1\0\1\67"+
    "\1\73\1\67\1\74\2\67\1\0\3\67\1\177\6\67"+
    "\1\0\1\67\2\167\1\0\1\67\1\73\1\67\1\74"+
    "\2\67\1\0\7\67\2\224\1\67\1\0\2\202\2\0"+
    "\22\202\2\210\2\0\22\210\1\116\2\167\1\0\1\116"+
    "\1\73\1\116\1\117\2\116\1\0\3\116\1\214\6\116"+
    "\1\0\1\116\2\167\1\0\1\116\1\73\1\116\1\117"+
    "\2\116\1\0\7\116\2\225\1\116\1\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[2156];
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
    "\17\0\4\1\1\11\2\1\1\11\5\1\1\11\4\1"+
    "\1\11\1\1\2\11\25\1\1\11\2\1\1\11\5\1"+
    "\1\11\1\1\1\11\2\1\2\11\14\1\1\11\6\1"+
    "\4\0\1\1\1\0\1\1\1\0\3\1\2\0\1\11"+
    "\1\0\2\11\2\1\1\0\1\1\1\0\3\1\3\0"+
    "\1\1\1\0\1\1\1\0\3\1\2\0\1\11\1\0"+
    "\2\11\1\1\1\0\1\1\1\0\3\1\2\0\2\11"+
    "\4\1";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[149];
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
  public CwtLexer() {
    this((java.io.Reader)null);
  }
  private int optionDepth = 0;


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public CwtLexer(java.io.Reader in) {
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
            { yybegin(WAITING_PROPERTY_VALUE_END); return STRING_TOKEN;
            } 
            // fall through
          case 46: break;
          case 2: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 47: break;
          case 3: 
            { return COMMENT;
            } 
            // fall through
          case 48: break;
          case 4: 
            { yypushback(yylength()); yybegin(WAITING_PROPERTY_KEY);
            } 
            // fall through
          case 49: break;
          case 5: 
            { return BAD_CHARACTER;
            } 
            // fall through
          case 50: break;
          case 6: 
            { return RIGHT_BRACE;
            } 
            // fall through
          case 51: break;
          case 7: 
            { yybegin(WAITING_PROPERTY_VALUE_END); return INT_TOKEN;
            } 
            // fall through
          case 52: break;
          case 8: 
            { return LEFT_BRACE;
            } 
            // fall through
          case 53: break;
          case 9: 
            { yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_TOKEN;
            } 
            // fall through
          case 54: break;
          case 10: 
            { yybegin(YYINITIAL); return WHITE_SPACE;
            } 
            // fall through
          case 55: break;
          case 11: 
            { yybegin(YYINITIAL); return RIGHT_BRACE;
            } 
            // fall through
          case 56: break;
          case 12: 
            { yybegin(YYINITIAL); return LEFT_BRACE;
            } 
            // fall through
          case 57: break;
          case 13: 
            { yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 58: break;
          case 14: 
            { yybegin(WAITING_PROPERTY_END); return STRING_TOKEN;
            } 
            // fall through
          case 59: break;
          case 15: 
            { yybegin(WAITING_PROPERTY_END); return INT_TOKEN;
            } 
            // fall through
          case 60: break;
          case 16: 
            { yybegin(YYINITIAL);  return WHITE_SPACE;
            } 
            // fall through
          case 61: break;
          case 17: 
            { if(optionDepth==0){
    	yypushback(yylength()); yybegin(WAITING_OPTION_TOP_STRING);
   	}else{
    	yybegin(WAITING_OPTION_VALUE_END); return STRING_TOKEN;
    }
            } 
            // fall through
          case 62: break;
          case 18: 
            { yypushback(yylength()); yybegin(WAITING_OPTION_KEY);
            } 
            // fall through
          case 63: break;
          case 19: 
            { optionDepth--; return RIGHT_BRACE;
            } 
            // fall through
          case 64: break;
          case 20: 
            { yybegin(WAITING_OPTION_VALUE_END); return INT_TOKEN;
            } 
            // fall through
          case 65: break;
          case 21: 
            { optionDepth++; return LEFT_BRACE;
            } 
            // fall through
          case 66: break;
          case 22: 
            { yybegin(WAITING_OPTION_VALUE_END); return STRING_TOKEN;
            } 
            // fall through
          case 67: break;
          case 23: 
            { yybegin(WATIING_OPTION_SEPARATOR); return OPTION_KEY_TOKEN;
            } 
            // fall through
          case 68: break;
          case 24: 
            { yybegin(WAITING_OPTION); optionDepth--; return RIGHT_BRACE;
            } 
            // fall through
          case 69: break;
          case 25: 
            { yybegin(WAITING_OPTION); optionDepth++; return LEFT_BRACE;
            } 
            // fall through
          case 70: break;
          case 26: 
            { yybegin(WAITING_OPTION_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 71: break;
          case 27: 
            { if(optionDepth==0){
    	yypushback(yylength()); yybegin(WAITING_OPTION_VALUE_TOP_STRING);
   	}else{
    	yybegin(WAITING_OPTION_END); return STRING_TOKEN;
    }
            } 
            // fall through
          case 72: break;
          case 28: 
            { yybegin(WAITING_OPTION_END); return INT_TOKEN;
            } 
            // fall through
          case 73: break;
          case 29: 
            { yybegin(WAITING_OPTION_END); return STRING_TOKEN;
            } 
            // fall through
          case 74: break;
          case 30: 
            { yybegin(WAITING_OPTION); return WHITE_SPACE;
            } 
            // fall through
          case 75: break;
          case 31: 
            { yybegin(WAITING_OPTION);  return WHITE_SPACE;
            } 
            // fall through
          case 76: break;
          case 32: 
            { yybegin(YYINITIAL); return DOCUMENTATION_TOKEN;
            } 
            // fall through
          case 77: break;
          case 33: 
            { yybegin(WAITING_PROPERTY_VALUE_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 78: break;
          case 34: 
            { yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            } 
            // fall through
          case 79: break;
          case 35: 
            { yybegin(WAITING_OPTION); return OPTION_START;
            } 
            // fall through
          case 80: break;
          case 36: 
            { yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 81: break;
          case 37: 
            { yybegin(WAITING_OPTION_VALUE_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 82: break;
          case 38: 
            { yybegin(WAITING_OPTION_VALUE); return NOT_EQUAL_SIGN;
            } 
            // fall through
          case 83: break;
          case 39: 
            { yybegin(WAITING_OPTION_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 84: break;
          case 40: 
            { yypushback(1); yybegin(WAITING_OPTION); return OPTION_START;
            } 
            // fall through
          case 85: break;
          case 41: 
            { yybegin(WAITING_DOCUMENTATION); return DOCUMENTATION_START;
            } 
            // fall through
          case 86: break;
          case 42: 
            { yybegin(WAITING_PROPERTY_VALUE_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 87: break;
          case 43: 
            { yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 88: break;
          case 44: 
            { yybegin(WAITING_OPTION_VALUE_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 89: break;
          case 45: 
            { yybegin(WAITING_OPTION_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 90: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
