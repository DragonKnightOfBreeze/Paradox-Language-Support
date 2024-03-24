// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: ParadoxScriptLexer.flex

package icu.windea.pls.script.psi;

import com.intellij.lexer.*;
import com.intellij.psi.tree.*;

import java.util.*;
import java.util.concurrent.atomic.*;

import static com.intellij.psi.TokenType.BAD_CHARACTER;
import static com.intellij.psi.TokenType.WHITE_SPACE;
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
    "\1\14\1\0\1\15\1\16\1\17\12\20\2\0\1\21"+
    "\1\22\1\23\1\24\1\25\32\26\1\27\1\30\1\31"+
    "\1\0\1\26\1\0\1\26\1\32\2\26\1\33\1\26"+
    "\1\34\1\35\5\26\1\36\1\37\2\26\1\40\1\41"+
    "\2\26\1\42\2\26\1\43\1\26\1\44\1\45\1\46"+
    "\7\0\1\3\32\0\1\47\u01bf\0\12\50\206\0\12\50"+
    "\306\0\12\50\234\0\12\50\166\0\12\50\140\0\12\50"+
    "\166\0\12\50\106\0\12\50\u0116\0\12\50\106\0\12\50"+
    "\346\0\1\47\u015f\0\12\50\46\0\12\50\u012c\0\12\50"+
    "\200\0\12\50\246\0\12\50\6\0\12\50\266\0\12\50"+
    "\126\0\12\50\206\0\12\50\6\0\12\50\246\0\13\47"+
    "\35\0\2\3\5\0\1\47\57\0\1\47\240\0\1\47"+
    "\u01cf\0\12\50\46\0\12\50\306\0\12\50\26\0\12\50"+
    "\126\0\12\50\u0196\0\12\50\6\0\u0100\51\240\0\12\50"+
    "\206\0\12\50\u012c\0\12\50\200\0\12\50\74\0\12\50"+
    "\220\0\12\50\166\0\12\50\146\0\12\50\206\0\12\50"+
    "\106\0\12\50\266\0\12\50\u0164\0\62\50\100\0\12\50"+
    "\266\0";

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
    "\1\6\1\7\1\1\1\10\1\11\1\12\4\1\1\13"+
    "\1\14\2\15\1\16\1\5\1\7\1\15\1\17\1\12"+
    "\1\20\1\21\1\22\1\23\1\17\1\12\4\24\1\25"+
    "\1\16\2\24\1\17\1\26\2\27\1\5\1\7\1\27"+
    "\1\16\1\2\1\30\1\31\1\32\1\33\1\17\1\12"+
    "\1\34\1\35\1\36\1\37\1\40\3\41\1\42\1\43"+
    "\1\44\1\45\1\46\1\47\1\50\1\51\1\52\1\53"+
    "\1\54\1\55\1\56\1\57\1\16\1\60\1\61\1\62"+
    "\1\63\1\64\1\0\1\65\1\1\1\65\1\66\1\1"+
    "\1\65\1\0\1\67\1\70\1\66\1\71\1\72\1\73"+
    "\1\0\1\1\1\74\2\1\1\0\1\15\1\66\1\24"+
    "\1\0\1\75\1\76\1\24\1\27\1\66\1\0\1\30"+
    "\1\0\1\31\1\32\1\0\1\77\1\100\1\0\1\1"+
    "\1\65\1\1\1\0\1\1\2\0\1\101";

  private static int [] zzUnpackAction() {
    int [] result = new int[151];
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
    "\0\0\0\52\0\124\0\176\0\250\0\322\0\374\0\u0126"+
    "\0\u0150\0\u017a\0\u01a4\0\u01ce\0\u01f8\0\u0222\0\u024c\0\u0276"+
    "\0\u02a0\0\u02ca\0\u02f4\0\u031e\0\u0348\0\u0372\0\u039c\0\u03c6"+
    "\0\u03f0\0\u041a\0\u03f0\0\u0444\0\u046e\0\u0498\0\u04c2\0\u04ec"+
    "\0\u031e\0\u031e\0\u0516\0\u0540\0\u056a\0\u0594\0\u046e\0\u046e"+
    "\0\u05be\0\u05e8\0\u046e\0\u0612\0\u063c\0\u0666\0\u05be\0\u05be"+
    "\0\u046e\0\u046e\0\u0690\0\u046e\0\u046e\0\u046e\0\u06ba\0\u06e4"+
    "\0\u070e\0\u0738\0\u070e\0\u04ec\0\u0762\0\u078c\0\u031e\0\u04ec"+
    "\0\u07b6\0\u07e0\0\u080a\0\u0834\0\u085e\0\u0888\0\u0888\0\u08b2"+
    "\0\u08dc\0\u0906\0\u0930\0\u0930\0\u0930\0\u095a\0\u046e\0\u046e"+
    "\0\u0984\0\u046e\0\u09ae\0\u09d8\0\u0a02\0\u09d8\0\u046e\0\u046e"+
    "\0\u0a2c\0\u046e\0\u031e\0\u046e\0\u0a56\0\u046e\0\u046e\0\u046e"+
    "\0\u046e\0\u046e\0\u046e\0\u0a80\0\u046e\0\u0aaa\0\u0ad4\0\u046e"+
    "\0\u046e\0\u0afe\0\u031e\0\u0afe\0\u046e\0\u046e\0\u0b28\0\u039c"+
    "\0\u0b52\0\u041a\0\u046e\0\u031e\0\u046e\0\u046e\0\u046e\0\u0b7c"+
    "\0\u0ba6\0\u031e\0\u0bd0\0\u0bfa\0\u0c24\0\u046e\0\u05be\0\u046e"+
    "\0\u0c4e\0\u0738\0\u06ba\0\u0c78\0\u046e\0\u07b6\0\u0888\0\u046e"+
    "\0\u0ca2\0\u046e\0\u046e\0\u0ccc\0\u0a02\0\u0a80\0\u0a80\0\u0cf6"+
    "\0\u0cf6\0\u046e\0\u0d20\0\u0d4a\0\u0d74\0\u0d9e\0\u046e";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[151];
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
    "\1\24\1\33\1\34\1\35\1\36\1\37\1\40\1\24"+
    "\1\41\1\24\1\42\3\24\1\43\1\44\1\24\1\45"+
    "\2\24\1\46\1\47\1\24\1\50\1\25\2\24\1\51"+
    "\3\25\1\52\1\53\1\30\12\51\1\54\1\35\1\55"+
    "\1\56\1\53\1\51\1\57\1\51\1\60\12\51\1\61"+
    "\1\51\1\50\1\25\2\51\1\53\3\25\2\53\1\30"+
    "\1\62\10\53\1\63\1\53\1\64\3\53\1\63\1\65"+
    "\1\53\1\66\12\63\1\61\1\53\1\50\1\25\2\53"+
    "\1\67\3\25\1\67\1\70\1\30\1\53\4\67\2\71"+
    "\1\72\1\67\1\73\1\67\1\53\2\67\1\74\1\67"+
    "\1\65\1\67\1\66\4\67\1\75\4\67\1\76\1\61"+
    "\1\67\1\50\1\25\2\67\1\24\3\25\1\26\1\27"+
    "\1\30\5\24\2\31\1\32\1\24\1\33\1\34\1\35"+
    "\1\36\1\37\1\40\1\24\1\77\1\24\1\42\3\24"+
    "\1\43\1\44\1\24\1\45\2\24\1\46\1\61\1\24"+
    "\1\50\1\25\3\24\3\25\1\24\1\27\1\30\5\24"+
    "\2\31\1\32\1\24\1\33\1\24\1\53\2\24\1\100"+
    "\1\24\1\77\1\24\1\42\3\24\1\43\1\44\1\24"+
    "\1\45\2\24\1\46\1\61\1\24\1\50\1\25\2\24"+
    "\1\101\3\25\1\102\1\53\1\30\1\62\11\101\1\103"+
    "\1\35\1\104\1\105\1\53\1\101\1\65\1\101\1\66"+
    "\12\101\1\61\1\101\1\50\1\25\2\101\1\53\1\106"+
    "\2\107\1\53\1\110\1\53\1\62\37\53\1\106\2\53"+
    "\1\111\3\25\1\111\1\53\1\30\1\62\12\111\1\53"+
    "\2\111\1\53\1\111\1\65\1\111\1\66\12\111\1\61"+
    "\1\111\1\50\1\25\2\111\5\53\1\112\1\53\1\62"+
    "\42\53\1\113\3\25\1\113\1\53\1\30\13\113\1\53"+
    "\2\113\1\53\1\113\1\114\1\113\1\115\12\113\1\61"+
    "\1\113\1\50\1\25\2\113\1\53\3\25\2\53\1\30"+
    "\1\62\10\53\1\116\5\53\1\116\1\65\1\53\1\66"+
    "\12\116\1\61\1\53\1\50\1\25\3\53\3\117\2\53"+
    "\1\117\1\120\16\53\1\121\1\65\1\53\1\66\12\121"+
    "\1\61\1\122\1\50\1\117\2\53\1\123\3\117\2\123"+
    "\1\117\1\120\4\123\2\124\1\125\1\123\1\126\1\123"+
    "\1\53\4\123\1\65\1\123\1\66\12\123\1\61\1\123"+
    "\1\50\1\117\2\123\1\53\3\117\2\53\1\117\1\120"+
    "\17\53\1\65\1\53\1\66\12\53\1\61\1\53\1\50"+
    "\1\117\3\53\3\25\2\53\1\30\20\53\1\127\1\53"+
    "\1\66\12\53\1\61\1\53\1\50\1\25\3\53\3\25"+
    "\1\130\1\53\1\30\17\53\1\131\2\53\1\132\12\131"+
    "\1\61\1\53\1\50\1\25\2\53\1\24\3\25\1\24"+
    "\1\27\1\30\5\24\2\31\1\32\1\24\1\33\1\24"+
    "\1\53\2\24\1\74\1\24\1\133\1\24\1\42\3\24"+
    "\1\43\1\44\1\24\1\45\2\24\1\46\1\134\1\24"+
    "\1\50\1\25\2\24\1\53\3\135\2\53\1\30\1\62"+
    "\1\136\1\137\1\140\1\141\1\142\1\143\1\144\1\145"+
    "\1\146\5\53\1\147\2\53\1\150\12\147\1\53\1\151"+
    "\1\53\1\135\2\53\1\24\3\152\1\153\1\154\1\0"+
    "\12\24\1\153\1\155\1\153\20\24\1\0\1\24\1\0"+
    "\1\152\2\24\1\0\3\25\43\0\1\25\2\0\1\24"+
    "\3\152\1\153\1\154\1\0\12\24\1\153\1\156\1\153"+
    "\20\24\1\0\1\24\1\0\1\152\2\24\2\27\1\157"+
    "\1\27\1\160\1\154\13\27\3\160\4\27\1\161\21\27"+
    "\2\30\1\0\47\30\1\24\3\152\1\153\1\154\1\0"+
    "\7\24\1\32\1\24\1\33\1\153\1\155\1\153\20\24"+
    "\1\0\1\24\1\0\1\152\3\24\3\152\1\153\1\154"+
    "\1\0\11\24\1\162\1\153\1\155\1\153\20\24\1\0"+
    "\1\24\1\0\1\152\3\24\3\152\1\153\1\154\1\0"+
    "\12\24\1\153\1\163\1\164\20\24\1\0\1\24\1\0"+
    "\1\152\2\24\52\0\1\24\3\152\1\153\1\154\1\0"+
    "\12\24\1\153\1\165\1\153\20\24\1\0\1\24\1\0"+
    "\1\152\3\24\3\152\1\153\1\154\1\0\12\24\1\153"+
    "\1\166\1\153\20\24\1\0\1\24\1\0\1\152\2\24"+
    "\27\0\1\167\1\170\21\0\1\24\3\152\1\153\1\154"+
    "\1\0\12\24\1\153\1\155\1\153\15\24\1\171\2\24"+
    "\1\0\1\24\1\0\1\152\3\24\3\152\1\153\1\154"+
    "\1\0\12\24\1\153\1\155\1\153\13\24\1\172\4\24"+
    "\1\0\1\24\1\0\1\152\3\24\3\152\1\153\1\154"+
    "\1\0\12\24\1\153\1\155\1\153\10\24\1\173\7\24"+
    "\1\0\1\24\1\0\1\152\3\24\3\152\1\153\1\154"+
    "\1\0\12\24\1\153\1\155\1\153\7\24\1\174\10\24"+
    "\1\0\1\24\1\0\1\152\2\24\1\51\3\175\1\51"+
    "\2\0\13\51\1\176\2\51\1\0\16\51\1\0\1\51"+
    "\1\0\1\175\3\51\3\175\1\51\2\0\13\51\1\156"+
    "\2\51\1\0\16\51\1\0\1\51\1\0\1\175\3\51"+
    "\3\175\1\51\2\0\13\51\1\163\1\177\1\51\1\0"+
    "\16\51\1\0\1\51\1\0\1\175\3\51\3\175\1\51"+
    "\2\0\13\51\1\165\2\51\1\0\16\51\1\0\1\51"+
    "\1\0\1\175\3\51\3\175\1\51\2\0\13\51\1\166"+
    "\2\51\1\0\16\51\1\0\1\51\1\0\1\175\2\51"+
    "\20\0\1\63\5\0\1\63\3\0\12\63\6\0\1\67"+
    "\3\0\1\67\1\200\2\0\12\67\1\0\4\67\1\0"+
    "\1\67\1\0\12\67\1\0\1\67\2\0\2\67\5\70"+
    "\1\200\22\70\1\201\21\70\1\67\3\0\1\67\1\200"+
    "\2\0\6\67\1\72\1\67\1\73\1\67\1\0\4\67"+
    "\1\0\1\67\1\0\12\67\1\0\1\67\2\0\3\67"+
    "\3\0\1\67\1\200\2\0\10\67\1\202\1\67\1\0"+
    "\4\67\1\0\1\67\1\0\12\67\1\0\1\67\2\0"+
    "\3\67\3\0\1\67\1\200\2\0\12\67\1\0\4\67"+
    "\1\0\1\67\1\0\5\67\1\203\4\67\1\0\1\67"+
    "\2\0\3\67\3\0\1\67\1\200\2\0\12\67\1\0"+
    "\4\67\1\0\1\67\1\0\1\67\1\204\10\67\1\0"+
    "\1\67\2\0\2\67\1\101\3\0\1\101\1\205\2\0"+
    "\12\101\1\0\4\101\1\0\1\101\1\0\12\101\1\0"+
    "\1\101\2\0\3\101\3\0\1\101\1\205\2\0\12\101"+
    "\1\156\4\101\1\0\1\101\1\0\12\101\1\0\1\101"+
    "\2\0\3\101\3\0\1\101\1\205\2\0\12\101\1\163"+
    "\1\206\3\101\1\0\1\101\1\0\12\101\1\0\1\101"+
    "\2\0\3\101\3\0\1\101\1\205\2\0\12\101\1\165"+
    "\4\101\1\0\1\101\1\0\12\101\1\0\1\101\2\0"+
    "\3\101\3\0\1\101\1\205\2\0\12\101\1\166\4\101"+
    "\1\0\1\101\1\0\12\101\1\0\1\101\2\0\2\101"+
    "\1\0\1\207\2\107\43\0\1\207\2\0\2\110\1\0"+
    "\2\110\1\210\22\110\1\211\21\110\1\111\3\0\1\111"+
    "\1\212\2\0\12\111\1\0\4\111\1\0\1\111\1\0"+
    "\12\111\1\0\1\111\2\0\2\111\5\112\1\213\22\112"+
    "\1\214\21\112\1\113\3\0\1\113\2\0\13\113\1\0"+
    "\2\113\1\0\16\113\1\0\1\113\2\0\2\113\20\0"+
    "\1\116\5\0\1\116\3\0\12\116\26\0\1\121\5\0"+
    "\1\121\3\0\12\121\6\0\1\123\3\0\2\123\2\0"+
    "\12\123\1\0\4\123\1\0\1\123\1\0\12\123\1\0"+
    "\1\123\2\0\3\123\3\0\2\123\2\0\6\123\1\125"+
    "\1\123\1\126\1\123\1\0\4\123\1\0\1\123\1\0"+
    "\12\123\1\0\1\123\2\0\3\123\3\0\2\123\2\0"+
    "\10\123\1\215\1\123\1\0\4\123\1\0\1\123\1\0"+
    "\12\123\1\0\1\123\2\0\2\123\20\0\1\131\5\0"+
    "\1\131\3\0\12\131\7\0\3\135\43\0\1\135\22\0"+
    "\1\216\47\0\1\217\1\0\1\146\5\0\1\147\3\0"+
    "\12\147\26\0\1\147\5\0\1\147\3\0\12\147\7\0"+
    "\3\152\1\155\14\0\3\155\23\0\1\152\2\0\1\220"+
    "\3\157\1\221\1\222\13\220\3\221\4\220\1\223\16\220"+
    "\1\157\2\220\2\27\2\220\45\27\1\220\27\0\1\167"+
    "\22\0\1\24\3\152\1\153\1\154\1\0\12\24\1\153"+
    "\1\155\1\153\16\24\1\224\1\24\1\0\1\24\1\0"+
    "\1\152\3\24\3\152\1\153\1\154\1\0\12\24\1\153"+
    "\1\155\1\153\6\24\1\224\11\24\1\0\1\24\1\0"+
    "\1\152\3\24\3\152\1\153\1\154\1\0\12\24\1\153"+
    "\1\155\1\153\15\24\1\172\2\24\1\0\1\24\1\0"+
    "\1\152\2\24\1\0\3\175\16\0\1\176\24\0\1\175"+
    "\2\0\52\70\1\67\3\0\1\67\1\200\2\0\12\67"+
    "\1\0\4\67\1\0\1\67\1\0\7\67\1\203\2\67"+
    "\1\0\1\67\2\0\2\67\2\110\2\0\45\110\1\0"+
    "\52\112\5\220\1\222\22\220\1\223\73\220\1\24\1\225"+
    "\2\152\1\153\1\154\1\0\12\24\1\153\1\155\1\153"+
    "\20\24\1\226\1\24\1\0\1\152\2\24\1\0\1\225"+
    "\2\152\1\155\14\0\3\155\20\0\1\226\2\0\1\152"+
    "\3\0\1\226\1\0\1\226\12\0\1\226\1\0\1\226"+
    "\25\0\1\227\2\226\1\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[3528];
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
    "\23\0\11\1\1\11\11\1\2\11\2\1\1\11\5\1"+
    "\2\11\1\1\3\11\30\1\2\11\1\1\1\11\4\1"+
    "\2\11\1\1\1\11\1\1\1\11\1\1\6\11\1\1"+
    "\1\11\2\1\2\11\1\0\2\1\2\11\2\1\1\0"+
    "\1\1\1\11\1\1\3\11\1\0\4\1\1\0\1\11"+
    "\1\1\1\11\1\0\3\1\1\11\1\1\1\0\1\11"+
    "\1\0\2\11\1\0\2\1\1\0\2\1\1\11\1\0"+
    "\1\1\2\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[151];
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
            if(stateRef == templateStateRef) {
                state = stack.isEmpty() ? YYINITIAL : stack.peekLast();
            } 
            yybegin(state);
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
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_STRING);
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_STRING);
        }
            }
          // fall through
          case 66: break;
          case 2:
            { exitState(templateStateRef); return WHITE_SPACE;
            }
          // fall through
          case 67: break;
          case 3:
            { return COMMENT;
            }
          // fall through
          case 68: break;
          case 4:
            { enterState(templateStateRef, yystate());  return INT_TOKEN;
            }
          // fall through
          case 69: break;
          case 5:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LT_SIGN;
            }
          // fall through
          case 70: break;
          case 6:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 71: break;
          case 7:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GT_SIGN;
            }
          // fall through
          case 72: break;
          case 8:
            { yybegin(IN_SCRIPTED_VARIABLE); return AT;
            }
          // fall through
          case 73: break;
          case 9:
            { enterState(stack, YYINITIAL); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 74: break;
          case 10:
            { exitState(stack, YYINITIAL); recoverState(templateStateRef); return RIGHT_BRACKET;
            }
          // fall through
          case 75: break;
          case 11:
            { enterState(stack, YYINITIAL); return LEFT_BRACE;
            }
          // fall through
          case 76: break;
          case 12:
            { exitState(stack, YYINITIAL); return RIGHT_BRACE;
            }
          // fall through
          case 77: break;
          case 13:
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
          case 78: break;
          case 14:
            { return BAD_CHARACTER;
            }
          // fall through
          case 79: break;
          case 15:
            { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 80: break;
          case 16:
            { enterState(stack, stack.isEmpty() ? YYINITIAL : IN_PROPERTY_OR_VALUE); return LEFT_BRACE;
            }
          // fall through
          case 81: break;
          case 17:
            { enterState(parameterStateRef, yystate()); yybegin(IN_PARAMETER); return PARAMETER_START;
            }
          // fall through
          case 82: break;
          case 18:
            { return SCRIPTED_VARIABLE_NAME_TOKEN;
            }
          // fall through
          case 83: break;
          case 19:
            { exitState(templateStateRef); yybegin(IN_SCRIPTED_VARIABLE_VALUE); return EQUAL_SIGN;
            }
          // fall through
          case 84: break;
          case 20:
            { enterState(templateStateRef, yystate()); return STRING_TOKEN;
            }
          // fall through
          case 85: break;
          case 21:
            { enterState(templateStateRef, yystate()); return INT_TOKEN;
            }
          // fall through
          case 86: break;
          case 22:
            { yybegin(IN_SCRIPTED_VARIABLE_REFERENCE); return AT;
            }
          // fall through
          case 87: break;
          case 23:
            { return PROPERTY_KEY_TOKEN;
            }
          // fall through
          case 88: break;
          case 24:
            { boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return PROPERTY_KEY_TOKEN;
            }
          // fall through
          case 89: break;
          case 25:
            { return STRING_TOKEN;
            }
          // fall through
          case 90: break;
          case 26:
            { boolean rightQuoted = yycharat(yylength() -1) == '"';
        if(rightQuoted) {
            exitState(templateStateRef);
        }
        return STRING_TOKEN;
            }
          // fall through
          case 91: break;
          case 27:
            { yypushback(yylength());
        enterState(templateStateRef, yystate());
        yybegin(IN_SCRIPTED_VARIABLE_REFERENCE_NAME);
            }
          // fall through
          case 92: break;
          case 28:
            { return SCRIPTED_VARIABLE_REFERENCE_TOKEN;
            }
          // fall through
          case 93: break;
          case 29:
            { yypushback(yylength()); exitState(parameterStateRef);
            }
          // fall through
          case 94: break;
          case 30:
            { exitState(parameterStateRef); return PARAMETER_END;
            }
          // fall through
          case 95: break;
          case 31:
            { return PARAMETER_TOKEN;
            }
          // fall through
          case 96: break;
          case 32:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE); return PIPE;
            }
          // fall through
          case 97: break;
          case 33:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE_END); return SNIPPET_TOKEN;
            }
          // fall through
          case 98: break;
          case 34:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE_END); return INT_TOKEN;
            }
          // fall through
          case 99: break;
          case 35:
            { yybegin(IN_PARAMETER_CONDITION_EXPRESSION); return NESTED_LEFT_BRACKET;
            }
          // fall through
          case 100: break;
          case 36:
            { return NOT_SIGN;
            }
          // fall through
          case 101: break;
          case 37:
            { return CONDITION_PARAMETER_TOKEN;
            }
          // fall through
          case 102: break;
          case 38:
            { yybegin(IN_PARAMETER_CONDITION_BODY); return NESTED_RIGHT_BRACKET;
            }
          // fall through
          case 103: break;
          case 39:
            { enterState(stack, IN_PARAMETER_CONDITION_BODY); yybegin(IN_PARAMETER_CONDITION); return LEFT_BRACKET;
            }
          // fall through
          case 104: break;
          case 40:
            { enterState(stack, IN_PARAMETER_CONDITION_BODY); return LEFT_BRACE;
            }
          // fall through
          case 105: break;
          case 41:
            { return WHITE_SPACE;
            }
          // fall through
          case 106: break;
          case 42:
            { yybegin(IN_INLINE_MATH); return MOD_SIGN;
            }
          // fall through
          case 107: break;
          case 43:
            { return LP_SIGN;
            }
          // fall through
          case 108: break;
          case 44:
            { return RP_SIGN;
            }
          // fall through
          case 109: break;
          case 45:
            { yybegin(IN_INLINE_MATH); return TIMES_SIGN;
            }
          // fall through
          case 110: break;
          case 46:
            { yybegin(IN_INLINE_MATH); return PLUS_SIGN;
            }
          // fall through
          case 111: break;
          case 47:
            { yybegin(IN_INLINE_MATH); return MINUS_SIGN;
            }
          // fall through
          case 112: break;
          case 48:
            { yybegin(IN_INLINE_MATH); return DIV_SIGN;
            }
          // fall through
          case 113: break;
          case 49:
            { return INT_NUMBER_TOKEN;
            }
          // fall through
          case 114: break;
          case 50:
            { return INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN;
            }
          // fall through
          case 115: break;
          case 51:
            { leftAbsSign = true; exitState(stack, YYINITIAL); return INLINE_MATH_END;
            }
          // fall through
          case 116: break;
          case 52:
            { if(leftAbsSign) {
            leftAbsSign=false; 
            return LABS_SIGN;
        } else {
            leftAbsSign=true;
            return RABS_SIGN;
        }
            }
          // fall through
          case 117: break;
          case 53:
            { boolean leftQuoted = yycharat(0) == '"';
        if(leftQuoted) {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_QUOTED_KEY);
        } else {
            yypushback(yylength());
            enterState(templateStateRef, yystate());
            yybegin(IN_KEY);
        }
            }
          // fall through
          case 118: break;
          case 54:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return NOT_EQUAL_SIGN;
            }
          // fall through
          case 119: break;
          case 55:
            { enterState(templateStateRef, yystate());  return FLOAT_TOKEN;
            }
          // fall through
          case 120: break;
          case 56:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return LE_SIGN;
            }
          // fall through
          case 121: break;
          case 57:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return GE_SIGN;
            }
          // fall through
          case 122: break;
          case 58:
            { exitState(templateStateRef); yybegin(IN_PROPERTY_VALUE); return QUESTION_EQUAL_SIGN;
            }
          // fall through
          case 123: break;
          case 59:
            { enterState(stack, yystate()); yybegin(IN_INLINE_MATH); return INLINE_MATH_START;
            }
          // fall through
          case 124: break;
          case 60:
            { enterState(templateStateRef, yystate());  return BOOLEAN_TOKEN;
            }
          // fall through
          case 125: break;
          case 61:
            { enterState(templateStateRef, yystate()); return FLOAT_TOKEN;
            }
          // fall through
          case 126: break;
          case 62:
            { enterState(templateStateRef, yystate()); return BOOLEAN_TOKEN;
            }
          // fall through
          case 127: break;
          case 63:
            { yybegin(IN_PARAMETER_DEFAULT_VALUE_END);; return FLOAT_TOKEN;
            }
          // fall through
          case 128: break;
          case 64:
            { return FLOAT_NUMBER_TOKEN;
            }
          // fall through
          case 129: break;
          case 65:
            { enterState(templateStateRef, yystate());  return COLOR_TOKEN;
            }
          // fall through
          case 130: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
