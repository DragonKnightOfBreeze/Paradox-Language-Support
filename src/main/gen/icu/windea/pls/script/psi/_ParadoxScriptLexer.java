// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: ParadoxScriptLexer.flex

package icu.windea.pls.script.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;
import java.util.*;
import java.util.concurrent.atomic.*;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;


public class _ParadoxScriptLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int IN_SCRIPTED_VARIABLE = 2;
  public static final int IN_SCRIPTED_VARIABLE_NAME = 4;
  public static final int IN_SCRIPTED_VARIABLE_VALUE = 6;
  public static final int IN_PROPERTY_OR_VALUE = 8;
  public static final int IN_PROPERTY_VALUE = 10;
  public static final int IN_KEY = 12;
  public static final int IN_QUOTED_KEY = 14;
  public static final int IN_STRING = 16;
  public static final int IN_QUOTED_STRING = 18;
  public static final int IN_SCRIPTED_VARIABLE_REFERENCE = 20;
  public static final int IN_SCRIPTED_VARIABLE_REFERENCE_NAME = 22;
  public static final int IN_PARAMETER = 24;
  public static final int IN_PARAMETER_DEFAULT_VALUE = 26;
  public static final int IN_PARAMETER_DEFAULT_VALUE_END = 28;
  public static final int IN_PARAMETER_CONDITION = 30;
  public static final int IN_PARAMETER_CONDITION_EXPRESSION = 32;
  public static final int IN_PARAMETER_CONDITION_BODY = 34;
  public static final int IN_INLINE_MATH = 36;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1,  1,  2,  2,  3,  3,  4,  4,  5,  5,  6,  6,  7,  7,
     8,  8,  9,  9, 10, 10, 11, 11, 12, 12, 13, 13, 14, 14, 15, 15,
    16, 16, 17, 17, 18, 18
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\5\u0100\1\u0200\1\u0300\1\u0100\5\u0400\1\u0500\1\u0600"+
    "\1\u0700\5\u0100\1\u0800\1\u0900\1\u0a00\1\u0b00\1\u0c00\1\u0d00"+
    "\1\u0e00\3\u0100\1\u0f00\17\u0100\1\u1000\165\u0100\1\u0600\1\u0100"+
    "\1\u1100\1\u1200\1\u1300\1\u1400\54\u0100\10\u1500\37\u0100\1\u0a00"+
    "\4\u0100\1\u1600\10\u0100\1\u1700\2\u0100\1\u1800\1\u1900\1\u1400"+
    "\1\u0100\1\u0500\1\u0100\1\u1a00\1\u1700\1\u0900\3\u0100\1\u1300"+
    "\1\u1b00\114\u0100\1\u1c00\1\u1300\153\u0100\1\u1d00\11\u0100\1\u1e00"+
    "\1\u1400\6\u0100\1\u1300\u0f16\u0100";

  private static int [] zzUnpackcmap_top() {
    int [] result = new int[4352];
    int offset = 0;
    offset = zzUnpackcmap_top(ZZ_CMAP_TOP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_top(String packed, int offset, int [] result) {
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
   * Second-level tables for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_BLOCKS = zzUnpackcmap_blocks();

  private static final String ZZ_CMAP_BLOCKS_PACKED_0 =
    "\11\0\1\1\1\2\2\3\1\2\22\0\1\1\1\4"+
    "\1\5\1\6\1\7\1\10\2\0\1\11\1\12\1\13"+
    "\1\14\1\0\1\15\1\16\1\17\1\20\2\21\1\22"+
    "\2\21\1\23\3\21\2\0\1\24\1\25\1\26\1\27"+
    "\1\30\32\31\1\32\1\33\1\34\1\0\1\31\1\0"+
    "\1\31\1\35\2\31\1\36\1\31\1\37\1\40\5\31"+
    "\1\41\1\42\2\31\1\43\1\44\2\31\1\45\2\31"+
    "\1\46\1\31\1\47\1\50\1\51\7\0\1\3\32\0"+
    "\1\52\u01bf\0\12\53\206\0\12\53\306\0\12\53\234\0"+
    "\12\53\166\0\12\53\140\0\12\53\166\0\12\53\106\0"+
    "\12\53\u0116\0\12\53\106\0\12\53\346\0\1\52\u015f\0"+
    "\12\53\46\0\12\53\u012c\0\12\53\200\0\12\53\246\0"+
    "\12\53\6\0\12\53\266\0\12\53\126\0\12\53\206\0"+
    "\12\53\6\0\12\53\246\0\13\52\35\0\2\3\5\0"+
    "\1\52\57\0\1\52\240\0\1\52\u01cf\0\12\53\46\0"+
    "\12\53\306\0\12\53\26\0\12\53\126\0\12\53\u0196\0"+
    "\12\53\6\0\u0100\54\240\0\12\53\206\0\12\53\u012c\0"+
    "\12\53\200\0\12\53\74\0\12\53\220\0\12\53\166\0"+
    "\12\53\146\0\12\53\206\0\12\53\106\0\12\53\266\0"+
    "\12\53\u0164\0\62\53\100\0\12\53\266\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[7936];
    int offset = 0;
    offset = zzUnpackcmap_blocks(ZZ_CMAP_BLOCKS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackcmap_blocks(String packed, int offset, int [] result) {
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
   * Translates DFA states to action switch labels.
   */
  private static final int [] ZZ_ACTION = zzUnpackAction();

  private static final String ZZ_ACTION_PACKED_0 =
    "\23\0\1\1\1\2\2\1\1\3\2\1\1\4\1\5"+
    "\1\6\1\7\1\10\1\11\1\12\1\13\4\1\1\14"+
    "\1\15\2\16\1\17\1\16\1\20\1\13\1\21\1\22"+
    "\1\23\1\24\1\20\4\25\1\26\1\16\2\25\1\20"+
    "\1\27\2\30\2\10\1\2\1\31\1\32\1\33\1\34"+
    "\1\20\1\35\1\36\1\37\1\40\1\41\1\42\1\43"+
    "\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53"+
    "\1\54\1\55\1\56\1\57\1\10\1\60\1\61\1\62"+
    "\1\63\1\64\1\0\1\65\1\1\1\65\1\66\1\1"+
    "\1\65\1\0\1\67\1\70\1\71\1\72\1\73\1\0"+
    "\3\1\1\74\2\1\1\0\1\17\1\25\1\0\1\75"+
    "\1\76\1\25\1\30\1\0\1\31\1\0\1\32\1\33"+
    "\1\0\1\77\1\0\1\1\1\65\1\0\2\1\1\0"+
    "\1\1\1\0\1\1\1\100";

  private static int [] zzUnpackAction() {
    int [] result = new int[142];
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
    "\0\0\0\55\0\132\0\207\0\264\0\341\0\u010e\0\u013b"+
    "\0\u0168\0\u0195\0\u01c2\0\u01ef\0\u021c\0\u0249\0\u0276\0\u02a3"+
    "\0\u02d0\0\u02fd\0\u032a\0\u0357\0\u0384\0\u03b1\0\u03de\0\u040b"+
    "\0\u0438\0\u0465\0\u0438\0\u0492\0\u04bf\0\u04ec\0\u0519\0\u0546"+
    "\0\u0573\0\u0357\0\u05a0\0\u05cd\0\u05fa\0\u0627\0\u04bf\0\u04bf"+
    "\0\u04bf\0\u0654\0\u0681\0\u0519\0\u0681\0\u04bf\0\u04bf\0\u04bf"+
    "\0\u06ae\0\u04bf\0\u04bf\0\u06db\0\u0708\0\u0735\0\u0762\0\u0735"+
    "\0\u0546\0\u078f\0\u07bc\0\u0573\0\u0546\0\u07e9\0\u0816\0\u04bf"+
    "\0\u0843\0\u0843\0\u0870\0\u089d\0\u08ca\0\u08f7\0\u08f7\0\u0924"+
    "\0\u04bf\0\u04bf\0\u0951\0\u04bf\0\u097e\0\u04bf\0\u09ab\0\u04bf"+
    "\0\u09d8\0\u04bf\0\u0573\0\u04bf\0\u04bf\0\u04bf\0\u04bf\0\u04bf"+
    "\0\u04bf\0\u04bf\0\u0a05\0\u04bf\0\u0a32\0\u0a5f\0\u04bf\0\u04bf"+
    "\0\u0a8c\0\u0357\0\u0a8c\0\u04bf\0\u04bf\0\u0ab9\0\u03de\0\u0ae6"+
    "\0\u0465\0\u04bf\0\u04bf\0\u04bf\0\u04bf\0\u0b13\0\u0573\0\u04bf"+
    "\0\u0b40\0\u0357\0\u0b6d\0\u0b9a\0\u0bc7\0\u04bf\0\u04bf\0\u0bf4"+
    "\0\u0762\0\u06db\0\u0c21\0\u04bf\0\u0843\0\u04bf\0\u0c4e\0\u04bf"+
    "\0\u04bf\0\u0c7b\0\u0a05\0\u0a05\0\u0ca8\0\u0ca8\0\u0cd5\0\u0d02"+
    "\0\u0d2f\0\u0d5c\0\u0d89\0\u0db6\0\u0de3\0\u04bf";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[142];
    int offset = 0;
    offset = zzUnpackRowMap(ZZ_ROWMAP_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpackRowMap(String packed, int offset, int [] result) {
    int i = 0;  /* index in packed string  */
    int j = offset;  /* index in unpacked array */
    int l = packed.length() - 1;
    while (i < l) {
      int high = packed.charAt(i++) << 16;
      result[j++] = high | packed.charAt(i++);
    }
    return j;
  }

  /**
   * The transition table of the DFA
   */
  private static final int [] ZZ_TRANS = zzUnpacktrans();

  private static final String ZZ_TRANS_PACKED_0 =
    "\1\24\3\25\1\26\1\27\1\30\5\24\2\31\1\32"+
    "\1\24\4\33\1\34\1\35\1\36\1\37\1\40\1\24"+
    "\1\41\1\24\1\42\3\24\1\43\1\44\1\24\1\45"+
    "\2\24\1\46\1\47\1\24\1\50\1\25\2\24\1\51"+
    "\3\25\1\52\1\51\1\30\1\53\14\51\1\34\1\35"+
    "\1\36\1\54\1\51\1\53\1\55\1\51\1\56\12\53"+
    "\1\57\1\51\1\50\1\25\3\51\3\25\2\51\1\30"+
    "\1\60\10\51\4\61\1\51\1\62\3\51\1\61\1\63"+
    "\1\51\1\56\12\61\1\57\1\51\1\50\1\25\2\51"+
    "\1\64\3\25\1\64\1\65\1\30\1\51\4\64\2\66"+
    "\1\67\1\64\4\70\4\51\1\71\1\64\1\63\1\64"+
    "\1\56\4\64\1\72\4\64\1\73\1\57\1\64\1\50"+
    "\1\25\2\64\1\24\3\25\1\26\1\27\1\30\5\24"+
    "\2\31\1\32\1\24\4\33\1\34\1\35\1\36\1\54"+
    "\1\40\1\24\1\74\1\24\1\42\3\24\1\43\1\44"+
    "\1\24\1\45\2\24\1\46\1\57\1\24\1\50\1\25"+
    "\3\24\3\25\1\24\1\27\1\30\5\24\2\31\1\32"+
    "\1\24\4\33\4\51\1\75\1\24\1\74\1\24\1\42"+
    "\3\24\1\43\1\44\1\24\1\45\2\24\1\46\1\57"+
    "\1\24\1\50\1\25\2\24\1\76\3\25\1\77\1\100"+
    "\1\30\1\60\14\76\1\34\1\35\1\36\1\37\1\100"+
    "\1\76\1\63\1\76\1\56\12\76\1\57\1\76\1\50"+
    "\1\25\2\76\1\100\1\101\2\102\1\100\1\103\1\100"+
    "\1\60\42\100\1\101\2\100\1\104\3\25\1\104\1\100"+
    "\1\30\1\60\14\104\5\100\1\104\1\63\1\104\1\56"+
    "\12\104\1\57\1\104\1\50\1\25\2\104\5\100\1\105"+
    "\1\100\1\60\45\100\1\51\3\25\2\51\1\30\1\106"+
    "\21\51\1\106\1\107\1\51\1\56\12\106\1\57\1\51"+
    "\1\50\1\25\3\51\3\25\2\51\1\30\1\60\10\51"+
    "\4\110\5\51\1\110\1\63\1\51\1\56\12\110\1\57"+
    "\1\51\1\50\1\25\2\51\1\100\3\111\2\100\1\111"+
    "\1\112\21\100\1\113\1\63\1\100\1\56\12\113\1\57"+
    "\1\114\1\50\1\111\2\100\1\115\3\111\2\115\1\111"+
    "\1\112\14\115\4\100\2\115\1\63\1\115\1\56\12\115"+
    "\1\57\1\115\1\50\1\111\2\115\1\100\3\111\2\100"+
    "\1\111\1\112\22\100\1\63\1\100\1\56\12\100\1\57"+
    "\1\100\1\50\1\111\2\100\1\51\3\25\2\51\1\30"+
    "\23\51\1\116\1\51\1\56\12\51\1\57\1\51\1\50"+
    "\1\25\2\51\1\100\3\117\1\120\1\100\1\30\22\100"+
    "\1\121\2\100\1\122\12\121\1\57\1\100\1\50\1\117"+
    "\2\100\1\24\3\25\1\24\1\27\1\30\5\24\2\31"+
    "\1\32\1\24\4\33\4\51\1\71\1\24\1\123\1\24"+
    "\1\42\3\24\1\43\1\44\1\24\1\45\2\24\1\46"+
    "\1\124\1\24\1\50\1\25\2\24\1\100\3\117\2\100"+
    "\1\30\1\60\1\125\1\126\1\127\1\130\1\131\1\132"+
    "\1\133\1\134\4\135\5\100\1\136\2\100\1\137\12\136"+
    "\1\57\1\140\1\50\1\117\2\100\1\24\3\141\1\142"+
    "\1\143\1\0\15\24\4\144\17\24\1\0\1\24\1\0"+
    "\1\141\2\24\1\0\3\25\46\0\1\25\2\0\1\24"+
    "\3\141\1\142\1\143\1\0\15\24\1\144\1\145\2\144"+
    "\17\24\1\0\1\24\1\0\1\141\2\24\2\27\1\146"+
    "\1\27\1\147\1\143\16\27\4\147\3\27\1\150\21\27"+
    "\2\30\1\0\52\30\1\24\3\141\1\142\1\143\1\0"+
    "\7\24\1\32\1\24\4\33\4\144\17\24\1\0\1\24"+
    "\1\0\1\141\3\24\3\141\1\142\1\143\1\0\11\24"+
    "\4\151\4\144\17\24\1\0\1\24\1\0\1\141\2\24"+
    "\25\0\1\152\1\145\130\0\1\153\54\0\1\154\61\0"+
    "\1\155\1\156\21\0\1\157\3\0\1\157\1\160\1\0"+
    "\15\157\4\0\17\157\1\0\1\157\2\0\2\157\1\24"+
    "\3\141\1\142\1\143\1\0\15\24\4\144\14\24\1\161"+
    "\2\24\1\0\1\24\1\0\1\141\3\24\3\141\1\142"+
    "\1\143\1\0\15\24\4\144\12\24\1\162\4\24\1\0"+
    "\1\24\1\0\1\141\3\24\3\141\1\142\1\143\1\0"+
    "\15\24\4\144\7\24\1\163\7\24\1\0\1\24\1\0"+
    "\1\141\3\24\3\141\1\142\1\143\1\0\15\24\4\144"+
    "\6\24\1\164\10\24\1\0\1\24\1\0\1\141\2\24"+
    "\25\0\1\145\27\0\1\53\3\165\1\53\2\0\16\53"+
    "\1\166\2\53\1\0\16\53\1\0\1\53\1\0\1\165"+
    "\2\53\20\0\4\61\5\0\1\61\3\0\12\61\6\0"+
    "\1\64\3\0\1\64\1\167\2\0\14\64\4\0\2\64"+
    "\1\0\1\64\1\0\12\64\1\0\1\64\2\0\2\64"+
    "\5\65\1\167\25\65\1\170\21\65\1\64\3\0\1\64"+
    "\1\167\2\0\6\64\1\67\1\64\4\70\4\0\2\64"+
    "\1\0\1\64\1\0\12\64\1\0\1\64\2\0\3\64"+
    "\3\0\1\64\1\167\2\0\10\64\4\171\4\0\2\64"+
    "\1\0\1\64\1\0\12\64\1\0\1\64\2\0\3\64"+
    "\3\0\1\64\1\167\2\0\14\64\4\0\2\64\1\0"+
    "\1\64\1\0\5\64\1\172\4\64\1\0\1\64\2\0"+
    "\3\64\3\0\1\64\1\167\2\0\14\64\4\0\2\64"+
    "\1\0\1\64\1\0\1\64\1\173\10\64\1\0\1\64"+
    "\2\0\2\64\1\76\3\0\1\76\1\174\2\0\14\76"+
    "\4\0\2\76\1\0\1\76\1\0\12\76\1\0\1\76"+
    "\2\0\3\76\3\0\1\76\1\174\2\0\14\76\1\0"+
    "\1\145\2\0\2\76\1\0\1\76\1\0\12\76\1\0"+
    "\1\76\2\0\2\76\1\0\1\175\2\102\46\0\1\175"+
    "\2\0\2\103\1\0\2\103\1\176\1\103\1\0\23\103"+
    "\1\177\21\103\1\104\3\0\1\104\1\200\2\0\14\104"+
    "\4\0\2\104\1\0\1\104\1\0\12\104\1\0\1\104"+
    "\2\0\2\104\5\105\1\201\1\105\1\0\23\105\1\202"+
    "\21\105\1\106\3\0\1\106\2\0\16\106\1\0\2\106"+
    "\1\0\16\106\1\0\1\106\2\0\2\106\20\0\4\110"+
    "\5\0\1\110\3\0\12\110\26\0\4\113\5\0\1\113"+
    "\3\0\12\113\6\0\1\115\3\0\2\115\2\0\14\115"+
    "\4\0\2\115\1\0\1\115\1\0\12\115\1\0\1\115"+
    "\2\0\2\115\1\0\3\117\46\0\1\117\22\0\4\121"+
    "\5\0\1\121\3\0\12\121\26\0\4\203\47\0\1\204"+
    "\1\0\4\135\5\0\1\136\3\0\12\136\26\0\4\136"+
    "\5\0\1\136\3\0\12\136\7\0\3\141\1\144\17\0"+
    "\4\144\22\0\1\141\2\0\1\205\3\146\1\206\1\160"+
    "\16\205\4\206\3\205\1\207\16\205\1\146\2\205\2\27"+
    "\2\205\50\27\1\205\32\0\1\155\22\0\1\24\3\141"+
    "\1\142\1\143\1\0\15\24\4\144\15\24\1\210\1\24"+
    "\1\0\1\24\1\0\1\141\3\24\3\141\1\142\1\143"+
    "\1\0\15\24\4\144\5\24\1\211\11\24\1\0\1\24"+
    "\1\0\1\141\3\24\3\141\1\142\1\143\1\0\15\24"+
    "\4\144\14\24\1\162\2\24\1\0\1\24\1\0\1\141"+
    "\2\24\1\0\3\165\21\0\1\166\24\0\1\165\2\0"+
    "\55\65\1\64\3\0\1\64\1\167\2\0\14\64\4\0"+
    "\2\64\1\0\1\64\1\0\7\64\1\172\2\64\1\0"+
    "\1\64\2\0\2\64\55\103\55\105\5\205\1\160\25\205"+
    "\1\207\76\205\1\24\1\212\2\141\1\142\1\143\1\0"+
    "\13\24\1\213\1\24\4\144\17\24\1\214\1\24\1\0"+
    "\1\141\3\24\1\212\2\141\1\142\1\143\1\0\15\24"+
    "\4\144\17\24\1\214\1\24\1\0\1\141\2\24\1\0"+
    "\1\212\2\141\1\144\17\0\4\144\17\0\1\214\2\0"+
    "\1\141\2\0\1\24\3\141\1\142\1\143\1\0\14\24"+
    "\1\215\4\144\17\24\1\0\1\24\1\0\1\141\2\24"+
    "\1\0\1\214\1\0\1\214\12\0\1\214\1\0\4\214"+
    "\25\0\1\216\2\214\1\0\1\24\3\141\1\142\1\143"+
    "\1\0\11\24\1\211\3\24\4\144\17\24\1\0\1\24"+
    "\1\0\1\141\2\24";

  private static int [] zzUnpacktrans() {
    int [] result = new int[3600];
    int offset = 0;
    offset = zzUnpacktrans(ZZ_TRANS_PACKED_0, offset, result);
    return result;
  }

  private static int zzUnpacktrans(String packed, int offset, int [] result) {
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
   * ZZ_ATTRIBUTE[aState] contains the attributes of state {@code aState}
   */
  private static final int [] ZZ_ATTRIBUTE = zzUnpackAttribute();

  private static final String ZZ_ATTRIBUTE_PACKED_0 =
    "\23\0\11\1\1\11\11\1\3\11\4\1\3\11\1\1"+
    "\2\11\14\1\1\11\10\1\2\11\1\1\1\11\1\1"+
    "\1\11\1\1\1\11\1\1\1\11\1\1\7\11\1\1"+
    "\1\11\2\1\2\11\1\0\2\1\2\11\2\1\1\0"+
    "\1\1\4\11\1\0\1\1\1\11\4\1\1\0\2\11"+
    "\1\0\3\1\1\11\1\0\1\11\1\0\2\11\1\0"+
    "\1\1\1\0\2\1\1\0\2\1\1\0\1\1\1\0"+
    "\1\1\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[142];
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

  /** zzAtEOF == true <=> the scanner is at the EOF */
  private boolean zzAtEOF;

  /** Number of newlines encountered up to the start of the matched text. */
  @SuppressWarnings("unused")
  private int yyline;

  /** Number of characters from the last newline up to the start of the matched text. */
  @SuppressWarnings("unused")
  protected int yycolumn;

  /** Number of characters up to the start of the matched text. */
  @SuppressWarnings("unused")
  private long yychar;

  /** Whether the scanner is currently at the beginning of a line. */
  @SuppressWarnings("unused")
  private boolean zzAtBOL = true;

  /** Whether the user-EOF-code has already been executed. */
  @SuppressWarnings("unused")
  private boolean zzEOFDone;

  /* user code: */
    private boolean leftAbsSign = true;
    private final Deque<Integer> stack = new ArrayDeque<>();
    private final AtomicInteger templateStateRef = new AtomicInteger(-1);
    private final AtomicInteger parameterStateRef = new AtomicInteger(-1);

    public _ParadoxScriptLexer() {
        this((java.io.Reader)null);
    }

    private void enterState(Deque<Integer> stack, int state) {
        stack.offerLast(state);
        yybegin(state);
    }

    private void exitState(Deque<Integer> stack, int defaultState) {
        Integer state = stack.pollLast();
        if(state != null) {
            yybegin(state);
        } else {
            yybegin(defaultState);
        }
    }

    private void enterState(AtomicInteger stateRef, int state) {
        if(stateRef.get() == -1) {
            stateRef.set(state);
        }
    }

    private void exitState(AtomicInteger stateRef) {
        int state = stateRef.getAndSet(-1);
        if(state != -1) {
            if(stateRef == templateStateRef && state != IN_INLINE_MATH) {
                state = stack.isEmpty() ? YYINITIAL : stack.peekLast();
            }
            yybegin(state);
        }
    }

    private boolean exitStateForErrorToken(AtomicInteger stateRef) {
        int state = stateRef.getAndSet(-1);
        if(state != -1) {
            if(stateRef == templateStateRef && state != IN_INLINE_MATH) {
                state = stack.isEmpty() ? YYINITIAL : stack.peekLast();
            }
            yybegin(state);
        }
        if(state != -1) {
            yypushback(yylength());
            return true;
        } else {
            return false;
        }
    }

    private void recoverState(AtomicInteger stateRef) {
        int state = stateRef.get();
        if(state != -1) {
            yybegin(state);
        }
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public _ParadoxScriptLexer(java.io.Reader in) {
    this.zzReader = in;
  }


  /** Returns the maximum size of the scanner buffer, which limits the size of tokens. */
  private int zzMaxBufferLen() {
    return Integer.MAX_VALUE;
  }

  /**  Whether the scanner buffer can grow to accommodate a larger token. */
  private boolean zzCanGrow() {
    return true;
  }

  /**
   * Translates raw input code points to DFA table row
   */
  private static int zzCMap(int input) {
    int offset = input & 255;
    return offset == input ? ZZ_CMAP_BLOCKS[offset] : ZZ_CMAP_BLOCKS[ZZ_CMAP_TOP[input >> 8] | offset];
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
  public IElementType advance() throws java.io.IOException
  {
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
            zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
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
              zzInput = Character.codePointAt(zzBufferL, zzCurrentPosL);
              zzCurrentPosL += Character.charCount(zzInput);
            }
          }
          int zzNext = zzTransL[ zzRowMapL[zzState] + zzCMap(zzInput) ];
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
            { boolean leftQuoted = yycharat(0) == '"';
        if(leftQuoted) {
            yypushback(yylength() - 1);
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_STRING);
            return STRING_TOKEN;
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_STRING);
        }
            }
          // fall through
          case 65: break;
          case 2:
            { exitState(templateStateRef); return WHITE_SPACE;
            }
          // fall through
          case 66: break;
          case 3:
            { return COMMENT;
            }
          // fall through
          case 67: break;
          case 4:
            { enterState(templateStateRef, yystate());  return INT_TOKEN;
            }
          // fall through
          case 68: break;
          case 5:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN;
            }
          // fall through
          case 69: break;
          case 6:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 70: break;
          case 7:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN;
            }
          // fall through
          case 71: break;
          case 8:
            { return BAD_CHARACTER;
            }
          // fall through
          case 72: break;
          case 9:
            { yybegin(IN_SCRIPTED_VARIABLE); return AT;
            }
          // fall through
          case 73: break;
          case 10:
            { enterState(stack, YYINITIAL); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 74: break;
          case 11:
            { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET;
            }
          // fall through
          case 75: break;
          case 12:
            { enterState(stack, YYINITIAL); return LEFT_BRACE;
            }
          // fall through
          case 76: break;
          case 13:
            { exitState(stack, YYINITIAL); return RIGHT_BRACE;
            }
          // fall through
          case 77: break;
          case 14:
            { boolean r = exitStateForErrorToken(templateStateRef);
        if(!r) return BAD_CHARACTER;
            }
          // fall through
          case 78: break;
          case 15:
            { //如果匹配到的文本以等号结尾，则作为scriptedVariable进行解析，否则作为scriptedVariableReference进行解析
        if(yycharat(yylength() -1) == '='){
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_SCRIPTED_VARIABLE_NAME);
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME);
        }
            }
          // fall through
          case 79: break;
          case 16:
            { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 80: break;
          case 17:
            { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE;
            }
          // fall through
          case 81: break;
          case 18:
            { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START;
            }
          // fall through
          case 82: break;
          case 19:
            { return SCRIPTED_VARIABLE_NAME_TOKEN;
            }
          // fall through
          case 83: break;
          case 20:
            { exitState(templateStateRef); yybegin(IN_SCRIPTED_VARIABLE_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 84: break;
          case 21:
            { enterState(templateStateRef, yystate()); return STRING_TOKEN;
            }
          // fall through
          case 85: break;
          case 22:
            { enterState(templateStateRef, yystate()); return INT_TOKEN;
            }
          // fall through
          case 86: break;
          case 23:
            { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT;
            }
          // fall through
          case 87: break;
          case 24:
            { return PROPERTY_KEY_TOKEN;
            }
          // fall through
          case 88: break;
          case 25:
            { boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return PROPERTY_KEY_TOKEN;
            }
          // fall through
          case 89: break;
          case 26:
            { return STRING_TOKEN;
            }
          // fall through
          case 90: break;
          case 27:
            { boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return STRING_TOKEN;
            }
          // fall through
          case 91: break;
          case 28:
            { yypushback(yylength());
        enterState(templateStateRef, yystate());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME);
            }
          // fall through
          case 92: break;
          case 29:
            { return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
            }
          // fall through
          case 93: break;
          case 30:
            { yypushback(yylength()); exitState(parameterStateRef);
            }
          // fall through
          case 94: break;
          case 31:
            { exitState(parameterStateRef); return PARAMETER_END;
            }
          // fall through
          case 95: break;
          case 32:
            { return PARAMETER_TOKEN;
            }
          // fall through
          case 96: break;
          case 33:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE); return PIPE;
            }
          // fall through
          case 97: break;
          case 34:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE_END); return PARAMETER_VALUE_TOKEN;
            }
          // fall through
          case 98: break;
          case 35:
            { yybegin(IN_PARAMETER_CONDITION_EXPRESSION); return NESTED_LEFT_BRACKET;
            }
          // fall through
          case 99: break;
          case 36:
            { return WHITE_SPACE;
            }
          // fall through
          case 100: break;
          case 37:
            { return NOT_SIGN;
            }
          // fall through
          case 101: break;
          case 38:
            { return CONDITION_PARAMETER_TOKEN;
            }
          // fall through
          case 102: break;
          case 39:
            { yybegin(IN_PARAMETER_CONDITION_BODY); return NESTED_RIGHT_BRACKET;
            }
          // fall through
          case 103: break;
          case 40:
            { enterState(stack, IN_PARAMETER_CONDITION_BODY); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 104: break;
          case 41:
            { enterState(stack, IN_PARAMETER_CONDITION_BODY); return LEFT_BRACE;
            }
          // fall through
          case 105: break;
          case 42:
            { return MOD_SIGN;
            }
          // fall through
          case 106: break;
          case 43:
            { return LP_SIGN;
            }
          // fall through
          case 107: break;
          case 44:
            { return RP_SIGN;
            }
          // fall through
          case 108: break;
          case 45:
            { return TIMES_SIGN;
            }
          // fall through
          case 109: break;
          case 46:
            { return PLUS_SIGN;
            }
          // fall through
          case 110: break;
          case 47:
            { return MINUS_SIGN;
            }
          // fall through
          case 111: break;
          case 48:
            { return DIV_SIGN;
            }
          // fall through
          case 112: break;
          case 49:
            { return INT_NUMBER_TOKEN;
            }
          // fall through
          case 113: break;
          case 50:
            { return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN;
            }
          // fall through
          case 114: break;
          case 51:
            { exitState(stack, YYINITIAL); return INLINE_MATH_END;
            }
          // fall through
          case 115: break;
          case 52:
            { if(leftAbsSign) {
            leftAbsSign = false;
            return LABS_SIGN;
        } else {
            leftAbsSign = true;
            return RABS_SIGN;
        }
            }
          // fall through
          case 116: break;
          case 53:
            { boolean leftQuoted = yycharat(0) == '"';
        if(leftQuoted) {
            yypushback(yylength() - 1);
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_KEY);
            return PROPERTY_KEY_TOKEN;
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_KEY);
        }
            }
          // fall through
          case 117: break;
          case 54:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            }
          // fall through
          case 118: break;
          case 55:
            { enterState(templateStateRef, yystate());  return FLOAT_TOKEN;
            }
          // fall through
          case 119: break;
          case 56:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN;
            }
          // fall through
          case 120: break;
          case 57:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN;
            }
          // fall through
          case 121: break;
          case 58:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return SAFE_EQUAL_SIGN;
            }
          // fall through
          case 122: break;
          case 59:
            { enterState(stack, yystate());
        enterState(templateStateRef, yystate());
        leftAbsSign = true;
        yybegin(IN_INLINE_MATH);
        return INLINE_MATH_START;
            }
          // fall through
          case 123: break;
          case 60:
            { enterState(templateStateRef, yystate());  return BOOLEAN_TOKEN;
            }
          // fall through
          case 124: break;
          case 61:
            { enterState(templateStateRef, yystate()); return FLOAT_TOKEN;
            }
          // fall through
          case 125: break;
          case 62:
            { enterState(templateStateRef, yystate()); return BOOLEAN_TOKEN;
            }
          // fall through
          case 126: break;
          case 63:
            { return FLOAT_NUMBER_TOKEN;
            }
          // fall through
          case 127: break;
          case 64:
            { enterState(templateStateRef, yystate());  return COLOR_TOKEN;
            }
          // fall through
          case 128: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
