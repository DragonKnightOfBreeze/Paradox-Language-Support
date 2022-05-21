/* The following code was generated by JFlex 1.7.0 tweaked for IntelliJ platform */

package icu.windea.pls.script.psi;

import com.intellij.openapi.project.*;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.config.cwt.*;
import icu.windea.pls.core.*;

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
  public static final int WAITING_INLINE_MATH = 18;
  public static final int WAITING_INLINE_MATH_OP = 20;
  public static final int WAITING_INLINE_MATH_PARAMETER = 22;
  public static final int WAITING_INLINE_MATH_PARAMETER_DEFAULT_VALUE = 24;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = { 
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7, 
     8,  8,  9,  9, 10, 10, 11, 11, 12, 12
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
    "\11\0\1\32\1\3\2\2\1\3\22\0\1\32\1\0\1\11\1\4\1\43\1\50\2\0\1\12\1\42\1\46"+
    "\1\45\1\0\1\21\1\23\1\47\1\22\11\7\2\0\1\40\1\10\1\36\1\0\1\5\32\6\1\37\1"+
    "\13\1\44\1\0\1\6\1\0\1\27\1\26\2\6\1\15\1\6\1\25\1\30\3\6\1\31\1\6\1\17\1"+
    "\20\2\6\1\24\1\16\2\6\1\31\2\6\1\14\1\6\1\33\1\41\1\35\7\0\1\2\32\0\1\1\77"+
    "\0\12\34\46\0\12\34\14\0\12\34\20\0\12\34\6\0\12\34\6\0\13\1\35\0\2\2\5\0"+
    "\1\1\57\0\1\1\26\0\12\34\16\0\62\34");

  /** 
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\15\0\1\1\1\2\1\3\1\4\1\5\2\4\3\1"+
    "\1\5\2\1\1\6\1\7\1\10\1\11\2\12\1\13"+
    "\1\4\3\12\1\13\1\2\2\14\2\15\1\4\1\16"+
    "\1\17\1\20\1\1\1\4\1\5\1\4\3\1\1\5"+
    "\2\1\1\21\1\14\1\22\1\23\1\24\1\23\1\25"+
    "\1\26\1\27\1\30\1\31\1\32\1\33\1\34\1\35"+
    "\1\36\1\37\1\40\2\41\1\0\1\42\1\0\1\42"+
    "\1\43\1\0\1\1\2\0\1\42\1\44\3\0\1\42"+
    "\1\1\1\45\2\1\1\12\1\0\1\46\1\0\1\12"+
    "\1\47\2\0\1\15\1\50\1\51\1\52\1\53\1\54"+
    "\1\55\1\1\1\0\1\44\1\0\1\1\1\45\2\1"+
    "\2\0\1\42\1\56\1\44\2\0\1\42\1\0\1\42"+
    "\2\1\1\57\1\60\2\0\1\50\1\56\2\1\1\23"+
    "\1\41\3\0\1\61";

  private static int [] zzUnpackAction() {
    int [] result = new int[145];
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
    "\0\0\0\51\0\122\0\173\0\244\0\315\0\366\0\u011f"+
    "\0\u0148\0\u0171\0\u019a\0\u01c3\0\u01ec\0\u0215\0\u023e\0\u0267"+
    "\0\u0290\0\u02b9\0\u02e2\0\u030b\0\u0334\0\u035d\0\u0386\0\u03af"+
    "\0\u03d8\0\u0401\0\u02e2\0\u02e2\0\u042a\0\u02e2\0\u0453\0\u047c"+
    "\0\u04a5\0\u04ce\0\u04f7\0\u0520\0\u0549\0\u0572\0\u059b\0\u059b"+
    "\0\u05c4\0\u05ed\0\u0616\0\u063f\0\u02e2\0\u0668\0\u0691\0\u06ba"+
    "\0\u06e3\0\u070c\0\u0735\0\u075e\0\u0787\0\u07b0\0\u07d9\0\u0802"+
    "\0\u082b\0\u0854\0\u0854\0\u087d\0\u08a6\0\u02e2\0\u08cf\0\u02e2"+
    "\0\u02e2\0\u02e2\0\u02e2\0\u02e2\0\u02e2\0\u02e2\0\u02e2\0\u02e2"+
    "\0\u08f8\0\u02e2\0\u02e2\0\u0921\0\u094a\0\u0973\0\u02e2\0\u099c"+
    "\0\u0215\0\u09c5\0\u09ee\0\u0a17\0\u030b\0\u0a40\0\u0a69\0\u099c"+
    "\0\u0a92\0\u0abb\0\u0a69\0\u030b\0\u0ae4\0\u0215\0\u0b0d\0\u0b36"+
    "\0\u0b5f\0\u04ce\0\u02e2\0\u0b88\0\u0bb1\0\u0453\0\u05c4\0\u0bda"+
    "\0\u0c03\0\u0c2c\0\u02e2\0\u02e2\0\u02e2\0\u0c55\0\u02e2\0\u0c7e"+
    "\0\u0735\0\u02e2\0\u0ca7\0\u0cd0\0\u06ba\0\u0cf9\0\u0d22\0\u0d4b"+
    "\0\u0d74\0\u099c\0\u0a17\0\u0973\0\u0d9d\0\u0dc6\0\u0735\0\u0def"+
    "\0\u0a92\0\u0e18\0\u0e41\0\u0b5f\0\u02e2\0\u0e6a\0\u0e93\0\u02e2"+
    "\0\u0c7e\0\u0ebc\0\u0ee5\0\u0d4b\0\u0d74\0\u0f0e\0\u0f37\0\u0f60"+
    "\0\u02e2";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[145];
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
    "\1\16\3\17\1\20\1\21\1\16\1\22\1\23\1\24"+
    "\2\16\1\25\2\16\1\26\1\16\1\27\1\30\1\16"+
    "\1\31\3\16\1\32\1\16\1\17\1\33\1\16\1\34"+
    "\7\16\1\27\3\16\1\23\3\17\1\35\3\23\1\36"+
    "\21\23\1\17\1\33\1\23\1\34\13\23\1\37\3\17"+
    "\1\40\1\23\1\37\1\41\1\23\1\42\2\37\1\43"+
    "\2\37\1\44\1\37\1\45\1\46\7\37\1\17\1\33"+
    "\1\37\1\34\7\37\1\45\3\37\1\23\1\47\1\50"+
    "\1\51\1\35\25\23\1\47\1\33\1\23\1\34\13\23"+
    "\1\52\5\23\2\52\1\23\1\53\20\52\2\23\1\52"+
    "\1\23\13\52\1\16\3\17\1\20\1\54\1\16\1\22"+
    "\1\23\1\24\2\16\1\25\2\16\1\26\1\16\1\27"+
    "\1\30\1\16\1\31\3\16\1\32\1\16\1\17\1\33"+
    "\1\16\1\34\7\16\1\27\3\16\1\23\3\17\1\35"+
    "\3\23\1\55\21\23\1\17\1\33\1\23\1\34\1\56"+
    "\1\23\1\57\10\23\1\60\3\17\1\35\1\61\1\60"+
    "\1\62\1\23\1\63\2\60\1\64\2\60\1\65\1\60"+
    "\1\66\1\67\1\60\1\70\3\60\1\71\1\60\1\17"+
    "\1\33\1\60\1\34\7\60\1\66\3\60\1\23\1\72"+
    "\1\73\1\51\1\35\25\23\1\72\1\33\1\23\1\34"+
    "\14\23\3\17\2\23\1\74\1\75\2\23\1\76\1\23"+
    "\5\74\1\23\1\77\1\23\6\74\1\17\6\23\1\100"+
    "\1\101\1\102\1\103\5\23\3\17\6\23\1\76\6\23"+
    "\1\104\10\23\1\17\6\23\1\100\1\101\1\23\1\103"+
    "\1\105\1\106\1\107\1\110\6\23\1\111\5\23\5\111"+
    "\3\23\6\111\7\23\1\112\1\23\1\113\14\23\1\114"+
    "\12\23\1\115\20\23\1\113\5\23\1\16\3\116\4\16"+
    "\1\117\1\120\20\16\1\116\1\0\1\16\1\0\1\121"+
    "\1\16\1\121\10\16\1\0\3\17\26\0\1\17\16\0"+
    "\3\20\1\0\45\20\6\0\1\122\4\0\1\123\5\122"+
    "\3\0\6\122\17\0\1\16\3\116\3\16\1\22\1\117"+
    "\1\120\10\16\1\22\1\124\6\16\1\116\1\0\1\16"+
    "\1\0\1\121\1\16\1\121\10\16\51\0\1\125\2\126"+
    "\1\116\4\125\1\127\1\130\1\131\1\132\16\125\1\126"+
    "\1\133\1\125\1\133\1\134\1\125\1\134\10\125\1\16"+
    "\3\116\4\16\1\117\1\120\3\16\1\135\14\16\1\116"+
    "\1\0\1\16\1\0\1\121\1\16\1\121\11\16\3\116"+
    "\4\16\1\117\1\120\6\16\1\136\11\16\1\116\1\0"+
    "\1\16\1\0\1\121\1\16\1\121\11\16\3\116\3\16"+
    "\1\22\1\117\1\120\10\16\1\30\7\16\1\116\1\0"+
    "\1\16\1\0\1\121\1\16\1\121\11\16\3\116\4\16"+
    "\1\117\1\120\11\16\1\124\6\16\1\116\1\0\1\16"+
    "\1\0\1\121\1\16\1\121\11\16\3\116\4\16\1\117"+
    "\1\120\13\16\1\137\4\16\1\116\1\0\1\16\1\0"+
    "\1\121\1\16\1\121\11\16\3\116\4\16\1\117\1\120"+
    "\4\16\1\140\13\16\1\116\1\0\1\16\1\0\1\121"+
    "\1\16\1\121\10\16\3\35\1\0\45\35\1\37\3\0"+
    "\4\37\2\0\20\37\2\0\1\37\1\0\13\37\1\40"+
    "\2\35\1\0\4\40\2\35\20\40\2\35\1\40\1\35"+
    "\13\40\1\37\3\0\3\37\1\41\2\0\10\37\1\41"+
    "\1\141\6\37\2\0\1\37\1\0\13\37\3\142\1\0"+
    "\5\142\1\143\1\142\1\144\35\142\1\37\3\0\4\37"+
    "\2\0\3\37\1\145\14\37\2\0\1\37\1\0\14\37"+
    "\3\0\4\37\2\0\6\37\1\146\11\37\2\0\1\37"+
    "\1\0\14\37\3\0\3\37\1\41\2\0\10\37\1\46"+
    "\7\37\2\0\1\37\1\0\14\37\3\0\4\37\2\0"+
    "\11\37\1\141\6\37\2\0\1\37\1\0\13\37\1\0"+
    "\1\47\1\50\1\51\26\0\1\47\17\0\1\147\2\51"+
    "\26\0\1\147\16\0\1\52\3\0\4\52\1\0\21\52"+
    "\2\0\1\52\1\0\13\52\1\53\2\150\1\0\4\53"+
    "\1\150\2\52\1\151\16\53\2\150\1\53\1\150\13\53"+
    "\6\0\1\152\4\0\1\123\5\152\3\0\6\152\27\0"+
    "\1\153\50\0\1\154\25\0\1\155\12\0\1\60\3\0"+
    "\4\60\2\0\20\60\2\0\1\60\1\0\13\60\6\0"+
    "\1\156\4\0\1\123\5\156\3\0\6\156\5\0\1\157"+
    "\11\0\1\60\3\0\3\60\1\62\2\0\10\60\1\62"+
    "\1\160\6\60\2\0\1\60\1\0\13\60\3\161\1\0"+
    "\5\161\1\162\1\161\1\163\35\161\1\60\3\0\4\60"+
    "\2\0\3\60\1\164\14\60\2\0\1\60\1\0\14\60"+
    "\3\0\4\60\2\0\6\60\1\165\11\60\2\0\1\60"+
    "\1\0\14\60\3\0\3\60\1\62\2\0\10\60\1\67"+
    "\7\60\2\0\1\60\1\0\14\60\3\0\4\60\2\0"+
    "\11\60\1\160\6\60\2\0\1\60\1\0\14\60\3\0"+
    "\4\60\2\0\13\60\1\166\4\60\2\0\1\60\1\0"+
    "\14\60\3\0\4\60\2\0\4\60\1\167\13\60\2\0"+
    "\1\60\1\0\13\60\1\0\1\72\1\73\1\51\26\0"+
    "\1\72\24\0\2\74\4\0\5\74\1\0\1\74\1\0"+
    "\6\74\26\0\1\75\12\0\1\75\1\170\50\0\1\170"+
    "\33\0\2\111\4\0\5\111\1\0\1\111\1\0\6\111"+
    "\26\0\1\114\12\0\1\114\1\171\50\0\1\171\26\0"+
    "\3\116\4\0\1\117\21\0\1\116\3\0\1\117\1\0"+
    "\1\117\10\0\1\120\3\116\4\120\1\117\21\120\1\116"+
    "\1\0\1\120\1\0\1\172\1\120\1\172\10\120\6\0"+
    "\2\122\4\0\5\122\1\0\1\122\1\0\6\122\56\0"+
    "\1\157\11\0\1\16\3\116\3\16\1\173\1\117\1\120"+
    "\10\16\1\173\7\16\1\116\1\0\1\16\1\0\1\121"+
    "\1\16\1\121\10\16\1\133\2\126\1\116\4\133\1\127"+
    "\1\174\1\161\1\175\16\133\1\126\3\133\1\127\1\133"+
    "\1\127\13\133\1\0\5\133\1\174\1\161\1\175\35\133"+
    "\1\131\2\176\1\116\4\131\1\177\1\130\1\131\1\200"+
    "\16\131\1\176\1\161\1\131\1\161\1\201\1\131\1\201"+
    "\10\131\1\125\1\126\2\116\4\125\1\127\21\125\1\126"+
    "\1\133\1\125\1\133\1\134\1\125\1\134\10\125\1\16"+
    "\3\116\4\16\1\117\1\120\4\16\1\136\13\16\1\116"+
    "\1\0\1\16\1\0\1\121\1\16\1\121\11\16\3\116"+
    "\4\16\1\117\1\120\14\16\1\202\3\16\1\116\1\0"+
    "\1\16\1\0\1\121\1\16\1\121\11\16\3\116\4\16"+
    "\1\117\1\120\14\16\1\203\2\16\1\203\1\116\1\0"+
    "\1\16\1\0\1\121\1\16\1\121\10\16\1\37\3\0"+
    "\3\37\1\204\2\0\10\37\1\204\7\37\2\0\1\37"+
    "\1\0\13\37\2\142\2\0\45\142\1\37\3\0\4\37"+
    "\2\0\4\37\1\146\13\37\2\0\1\37\1\0\13\37"+
    "\3\150\1\0\5\150\1\205\1\0\1\206\35\150\1\53"+
    "\1\150\2\0\4\53\1\150\21\53\2\150\1\53\1\150"+
    "\13\53\1\0\3\207\2\0\2\152\1\210\3\0\5\152"+
    "\1\0\1\152\1\0\6\152\1\207\24\0\2\156\4\0"+
    "\5\156\1\0\1\156\1\0\6\156\17\0\1\60\3\0"+
    "\3\60\1\211\2\0\10\60\1\211\7\60\2\0\1\60"+
    "\1\0\13\60\2\161\2\0\45\161\1\60\3\0\4\60"+
    "\2\0\4\60\1\165\13\60\2\0\1\60\1\0\14\60"+
    "\3\0\4\60\2\0\14\60\1\212\3\60\2\0\1\60"+
    "\1\0\14\60\3\0\4\60\2\0\14\60\1\213\2\60"+
    "\1\213\2\0\1\60\1\0\13\60\7\0\1\214\12\0"+
    "\1\214\35\0\1\215\12\0\1\215\26\0\2\133\2\0"+
    "\45\133\1\161\2\176\1\116\4\161\1\177\1\162\1\161"+
    "\1\163\16\161\1\176\3\161\1\177\1\161\1\177\10\161"+
    "\1\131\1\176\2\116\4\131\1\177\21\131\1\176\1\161"+
    "\1\131\1\161\1\201\1\131\1\201\10\131\1\16\3\116"+
    "\4\16\1\117\1\120\15\16\1\203\2\16\1\216\1\217"+
    "\1\16\1\0\1\121\1\16\1\121\11\16\3\116\4\16"+
    "\1\117\1\120\20\16\1\216\1\217\1\16\1\0\1\121"+
    "\1\16\1\121\10\16\2\150\2\0\45\150\1\0\3\207"+
    "\4\0\1\210\21\0\1\207\16\0\1\60\3\0\4\60"+
    "\2\0\15\60\1\213\2\60\1\220\1\217\1\60\1\0"+
    "\14\60\3\0\4\60\2\0\20\60\1\220\1\217\1\60"+
    "\1\0\13\60\1\0\3\116\4\0\1\117\21\0\1\216"+
    "\1\217\2\0\1\117\1\0\1\117\11\0\2\217\4\0"+
    "\1\217\12\0\2\217\6\0\1\217\1\0\1\217\1\221"+
    "\45\0\1\220\1\217\15\0";

  private static int [] zzUnpackTrans() {
    int [] result = new int[3977];
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
    "\15\0\5\1\1\11\7\1\2\11\1\1\1\11\16\1"+
    "\1\11\20\1\1\11\1\1\11\11\1\1\2\11\2\1"+
    "\1\0\1\11\1\0\2\1\1\0\1\1\2\0\2\1"+
    "\3\0\6\1\1\0\1\11\1\0\2\1\2\0\2\1"+
    "\3\11\1\1\1\11\1\1\1\0\1\11\1\0\4\1"+
    "\2\0\3\1\2\0\1\1\1\0\4\1\1\11\2\0"+
    "\1\11\5\1\3\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[145];
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
      
    private int depth = 0;
    private boolean leftAbsSign = true;
    
    public ParadoxScriptLexer(Project propect) {
        this((java.io.Reader)null);
        this.project = project;
    }
    
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
          case 50: break;
          case 2: 
            { return WHITE_SPACE;
            } 
            // fall through
          case 51: break;
          case 3: 
            { return COMMENT;
            } 
            // fall through
          case 52: break;
          case 4: 
            { return BAD_CHARACTER;
            } 
            // fall through
          case 53: break;
          case 5: 
            { yybegin(WAITING_PROPERTY_END); return INT_TOKEN;
            } 
            // fall through
          case 54: break;
          case 6: 
            { depth++; yybegin(nextState()); return LEFT_BRACE;
            } 
            // fall through
          case 55: break;
          case 7: 
            { depth--; yybegin(nextState()); return RIGHT_BRACE;
            } 
            // fall through
          case 56: break;
          case 8: 
            { return END_OF_LINE_COMMENT;
            } 
            // fall through
          case 57: break;
          case 9: 
            { yybegin(WAITING_VARIABLE_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 58: break;
          case 10: 
            { yybegin(WAITING_VARIABLE_END); return STRING_TOKEN;
            } 
            // fall through
          case 59: break;
          case 11: 
            { yybegin(WAITING_VARIABLE_END); return INT_TOKEN;
            } 
            // fall through
          case 60: break;
          case 12: 
            { yybegin(nextState()); return WHITE_SPACE;
            } 
            // fall through
          case 61: break;
          case 13: 
            { yybegin(WATIING_PROPERTY_SEPARATOR); return PROPERTY_KEY_ID;
            } 
            // fall through
          case 62: break;
          case 14: 
            { yybegin(WAITING_PROPERTY_VALUE); return EQUAL_SIGN;
            } 
            // fall through
          case 63: break;
          case 15: 
            { yybegin(WAITING_PROPERTY_VALUE); return GT_SIGN;
            } 
            // fall through
          case 64: break;
          case 16: 
            { yybegin(WAITING_PROPERTY_VALUE); return LT_SIGN;
            } 
            // fall through
          case 65: break;
          case 17: 
            { yybegin(WAITING_PROPERTY_KEY); return WHITE_SPACE;
            } 
            // fall through
          case 66: break;
          case 18: 
            { yybegin(WAITING_INLINE_MATH_OP); return INLINE_MATH_VARIABLE_REFERENCE_ID;
            } 
            // fall through
          case 67: break;
          case 19: 
            { yybegin(WAITING_INLINE_MATH_OP); return NUMBER_TOKEN;
            } 
            // fall through
          case 68: break;
          case 20: 
            { return LP_SIGN;
            } 
            // fall through
          case 69: break;
          case 21: 
            { if(leftAbsSign){
      leftAbsSign=false; 
      return LABS_SIGN;
    }else{
      leftAbsSign=true;
      return RABS_SIGN;
    }
            } 
            // fall through
          case 70: break;
          case 22: 
            { return RP_SIGN;
            } 
            // fall through
          case 71: break;
          case 23: 
            { yybegin(WAITING_INLINE_MATH_PARAMETER); return PARAMETER_START;
            } 
            // fall through
          case 72: break;
          case 24: 
            { leftAbsSign=true; yybegin(WAITING_PROPERTY_END); return INLINE_MATH_END;
            } 
            // fall through
          case 73: break;
          case 25: 
            { yybegin(WAITING_INLINE_MATH); return MINUS_SIGN;
            } 
            // fall through
          case 74: break;
          case 26: 
            { yybegin(WAITING_INLINE_MATH); return PLUS_SIGN;
            } 
            // fall through
          case 75: break;
          case 27: 
            { yybegin(WAITING_INLINE_MATH); return TIMES_SIGN;
            } 
            // fall through
          case 76: break;
          case 28: 
            { yybegin(WAITING_INLINE_MATH); return DIV_SIGN;
            } 
            // fall through
          case 77: break;
          case 29: 
            { yybegin(WAITING_INLINE_MATH); return MOD_SIGN;
            } 
            // fall through
          case 78: break;
          case 30: 
            { return PARAMETER_ID;
            } 
            // fall through
          case 79: break;
          case 31: 
            { yybegin(WAITING_INLINE_MATH_PARAMETER_DEFAULT_VALUE); return PIPE;
            } 
            // fall through
          case 80: break;
          case 32: 
            { yybegin(WAITING_INLINE_MATH_OP); return PARAMETER_END;
            } 
            // fall through
          case 81: break;
          case 33: 
            { return NUMBER_TOKEN;
            } 
            // fall through
          case 82: break;
          case 34: 
            { yypushback(yylength()); yybegin(WAITING_PROPERTY);
            } 
            // fall through
          case 83: break;
          case 35: 
            { yybegin(WAITING_VARIABLE_EQUAL_SIGN); return VARIABLE_NAME_ID;
            } 
            // fall through
          case 84: break;
          case 36: 
            { yybegin(WAITING_PROPERTY_END); return QUOTED_STRING_TOKEN;
            } 
            // fall through
          case 85: break;
          case 37: 
            { yybegin(WAITING_PROPERTY_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 86: break;
          case 38: 
            { yybegin(WAITING_VARIABLE_END); return QUOTED_STRING_TOKEN;
            } 
            // fall through
          case 87: break;
          case 39: 
            { yybegin(WAITING_VARIABLE_END); return BOOLEAN_TOKEN;
            } 
            // fall through
          case 88: break;
          case 40: 
            { //如果匹配到的文本以等号结尾，则将空白之前的部分解析为VARIABLE_NAME_ID，否则将其解析为VARIABLE_REFERENCE_ID
	CharSequence text = yytext();
	  int length = text.length();
	  if(text.charAt(length -1) == '='){
	  //计算需要回退的长度
	  int i;
	  for (i = 1; i < length ; i++) {
	    char c = text.charAt(length-i-1);
		if(!Character.isWhitespace(c)) break;
	  }
	  yypushback(i);
	  yybegin(WAITING_VARIABLE_EQUAL_SIGN);
	  return VARIABLE_NAME_ID;
	} else {
	  yybegin(WAITING_PROPERTY_END);
      return VARIABLE_REFERENCE_ID;
	}
            } 
            // fall through
          case 89: break;
          case 41: 
            { yybegin(WAITING_PROPERTY_VALUE); return GE_SIGN;
            } 
            // fall through
          case 90: break;
          case 42: 
            { yybegin(WAITING_PROPERTY_VALUE); return LE_SIGN;
            } 
            // fall through
          case 91: break;
          case 43: 
            { yybegin(WAITING_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            } 
            // fall through
          case 92: break;
          case 44: 
            { yybegin(WAITING_PROPERTY_END); return VARIABLE_REFERENCE_ID;
            } 
            // fall through
          case 93: break;
          case 45: 
            { yybegin(WAITING_INLINE_MATH); return INLINE_MATH_START;
            } 
            // fall through
          case 94: break;
          case 46: 
            { yybegin(WAITING_PROPERTY_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 95: break;
          case 47: 
            { yybegin(WAITING_VARIABLE_END); return FLOAT_TOKEN;
            } 
            // fall through
          case 96: break;
          case 48: 
            { yybegin(WATIING_PROPERTY_SEPARATOR); return QUOTED_PROPERTY_KEY_ID;
            } 
            // fall through
          case 97: break;
          case 49: 
            { yybegin(WAITING_PROPERTY_END); return COLOR_TOKEN;
            } 
            // fall through
          case 98: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
