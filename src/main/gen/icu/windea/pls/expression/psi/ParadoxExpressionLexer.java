// Generated by JFlex 1.9.1 http://jflex.de/  (tweaked for IntelliJ platform)
// source: ParadoxExpressionLexer.flex

package icu.windea.pls.expression.psi;


import com.intellij.lexer.*;
import com.intellij.psi.tree.IElementType;

import static com.intellij.psi.TokenType.*;
import static icu.windea.pls.core.StdlibExtensionsKt.*;
import static icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*;


public class ParadoxExpressionLexer implements FlexLexer {

  /** This character denotes the end of file */
  public static final int YYEOF = -1;

  /** initial size of the lookahead buffer */
  private static final int ZZ_BUFFERSIZE = 16384;

  /** lexical states */
  public static final int YYINITIAL = 0;
  public static final int WAITING_EXPRESSION_TOKEN = 2;

  /**
   * ZZ_LEXSTATE[l] is the state in the DFA for the lexical state l
   * ZZ_LEXSTATE[l+1] is the state in the DFA for the lexical state l
   *                  at the beginning of a line
   * l is of the form l = 2*k, k a non negative integer
   */
  private static final int ZZ_LEXSTATE[] = {
     0,  0,  1, 1
  };

  /**
   * Top-level table for translating characters to character classes
   */
  private static final int [] ZZ_CMAP_TOP = zzUnpackcmap_top();

  private static final String ZZ_CMAP_TOP_PACKED_0 =
    "\1\0\1\u0100\1\u0200\1\u0300\1\u0400\1\u0500\1\u0600\1\u0700"+
    "\1\u0800\1\u0900\1\u0a00\1\u0b00\1\u0c00\1\u0d00\1\u0e00\1\u0f00"+
    "\1\u1000\1\u0100\1\u1100\1\u1200\1\u1300\1\u0100\1\u1400\1\u1500"+
    "\1\u1600\1\u1700\1\u1800\1\u1900\1\u1a00\1\u1b00\1\u0100\1\u1c00"+
    "\1\u1d00\1\u1e00\2\u1f00\1\u2000\7\u1f00\1\u2100\1\u2200\1\u2300"+
    "\1\u1f00\1\u2400\1\u2500\2\u1f00\31\u0100\1\u2600\121\u0100\1\u2700"+
    "\4\u0100\1\u2800\1\u0100\1\u2900\1\u2a00\1\u2b00\1\u2c00\1\u2d00"+
    "\1\u2e00\53\u0100\1\u2f00\10\u3000\31\u1f00\1\u0100\1\u3100\1\u3200"+
    "\1\u0100\1\u3300\1\u3400\1\u3500\1\u3600\1\u3700\1\u3800\1\u3900"+
    "\1\u3a00\1\u3b00\1\u0100\1\u3c00\1\u3d00\1\u3e00\1\u3f00\1\u4000"+
    "\1\u4100\1\u4200\1\u1f00\1\u4300\1\u4400\1\u4500\1\u4600\1\u4700"+
    "\1\u4800\1\u4900\1\u4a00\1\u4b00\1\u4c00\1\u4d00\1\u4e00\1\u1f00"+
    "\1\u4f00\1\u5000\1\u5100\1\u1f00\3\u0100\1\u5200\1\u5300\1\u5400"+
    "\12\u1f00\4\u0100\1\u5500\17\u1f00\2\u0100\1\u5600\41\u1f00\2\u0100"+
    "\1\u5700\1\u5800\2\u1f00\1\u5900\1\u5a00\27\u0100\1\u5b00\2\u0100"+
    "\1\u5c00\45\u1f00\1\u0100\1\u5d00\1\u5e00\11\u1f00\1\u5f00\24\u1f00"+
    "\1\u6000\1\u6100\1\u1f00\1\u6200\1\u6300\1\u6400\1\u6500\2\u1f00"+
    "\1\u6600\5\u1f00\1\u6700\1\u6800\1\u6900\5\u1f00\1\u6a00\1\u6b00"+
    "\4\u1f00\1\u6c00\2\u1f00\1\u6d00\16\u1f00\246\u0100\1\u6e00\20\u0100"+
    "\1\u6f00\1\u7000\25\u0100\1\u7100\34\u0100\1\u7200\14\u1f00\2\u0100"+
    "\1\u7300\u0b06\u1f00\1\u2700\u02fe\u1f00";

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
    "\11\0\1\1\1\2\2\3\1\2\22\0\1\1\17\0"+
    "\12\4\1\5\6\0\32\4\4\0\1\4\1\0\32\4"+
    "\12\0\1\3\32\0\1\1\11\0\1\4\12\0\1\4"+
    "\4\0\1\4\5\0\27\4\1\0\37\4\1\0\u01ca\4"+
    "\4\0\14\4\16\0\5\4\7\0\1\4\1\0\1\4"+
    "\21\0\165\4\1\0\2\4\2\0\4\4\1\0\1\4"+
    "\6\0\1\4\1\0\3\4\1\0\1\4\1\0\24\4"+
    "\1\0\123\4\1\0\213\4\1\0\255\4\1\0\46\4"+
    "\2\0\1\4\6\0\51\4\10\0\55\4\1\0\1\4"+
    "\1\0\2\4\1\0\2\4\1\0\1\4\10\0\33\4"+
    "\4\0\4\4\35\0\13\4\5\0\112\4\4\0\146\4"+
    "\1\0\10\4\2\0\12\4\1\0\23\4\2\0\1\4"+
    "\20\0\73\4\2\0\145\4\16\0\66\4\4\0\1\4"+
    "\2\0\1\4\2\0\56\4\22\0\34\4\4\0\13\4"+
    "\65\0\25\4\1\0\10\4\25\0\17\4\1\0\201\4"+
    "\2\0\12\4\1\0\23\4\1\0\10\4\2\0\2\4"+
    "\2\0\26\4\1\0\7\4\1\0\1\4\3\0\4\4"+
    "\2\0\11\4\2\0\2\4\2\0\4\4\10\0\1\4"+
    "\4\0\2\4\1\0\5\4\2\0\14\4\12\0\1\4"+
    "\1\0\1\4\2\0\3\4\1\0\6\4\4\0\2\4"+
    "\2\0\26\4\1\0\7\4\1\0\2\4\1\0\2\4"+
    "\1\0\2\4\2\0\1\4\1\0\5\4\4\0\2\4"+
    "\2\0\3\4\3\0\1\4\7\0\4\4\1\0\1\4"+
    "\7\0\20\4\13\0\3\4\1\0\11\4\1\0\3\4"+
    "\1\0\26\4\1\0\7\4\1\0\2\4\1\0\5\4"+
    "\2\0\12\4\1\0\3\4\1\0\3\4\2\0\1\4"+
    "\17\0\4\4\2\0\12\4\11\0\7\4\1\0\3\4"+
    "\1\0\10\4\2\0\2\4\2\0\26\4\1\0\7\4"+
    "\1\0\2\4\1\0\5\4\2\0\11\4\2\0\2\4"+
    "\2\0\3\4\10\0\2\4\4\0\2\4\1\0\5\4"+
    "\2\0\12\4\1\0\1\4\20\0\2\4\1\0\6\4"+
    "\3\0\3\4\1\0\4\4\3\0\2\4\1\0\1\4"+
    "\1\0\2\4\3\0\2\4\3\0\3\4\3\0\14\4"+
    "\4\0\5\4\3\0\3\4\1\0\4\4\2\0\1\4"+
    "\6\0\1\4\16\0\12\4\20\0\15\4\1\0\3\4"+
    "\1\0\27\4\1\0\20\4\3\0\10\4\1\0\3\4"+
    "\1\0\4\4\7\0\2\4\1\0\3\4\5\0\4\4"+
    "\2\0\12\4\20\0\4\4\1\0\10\4\1\0\3\4"+
    "\1\0\27\4\1\0\12\4\1\0\5\4\2\0\11\4"+
    "\1\0\3\4\1\0\4\4\7\0\2\4\7\0\1\4"+
    "\1\0\4\4\2\0\12\4\1\0\2\4\15\0\4\4"+
    "\1\0\10\4\1\0\3\4\1\0\63\4\1\0\3\4"+
    "\1\0\5\4\5\0\4\4\7\0\5\4\2\0\12\4"+
    "\12\0\6\4\2\0\2\4\1\0\22\4\3\0\30\4"+
    "\1\0\11\4\1\0\1\4\2\0\7\4\3\0\1\4"+
    "\4\0\6\4\1\0\1\4\1\0\10\4\6\0\12\4"+
    "\2\0\2\4\15\0\72\4\5\0\17\4\1\0\12\4"+
    "\47\0\2\4\1\0\1\4\1\0\5\4\1\0\30\4"+
    "\1\0\1\4\1\0\27\4\2\0\5\4\1\0\1\4"+
    "\1\0\6\4\2\0\12\4\2\0\4\4\40\0\1\4"+
    "\27\0\2\4\6\0\12\4\13\0\1\4\1\0\1\4"+
    "\1\0\1\4\4\0\12\4\1\0\44\4\4\0\24\4"+
    "\1\0\22\4\1\0\44\4\11\0\1\4\71\0\112\4"+
    "\6\0\116\4\2\0\46\4\1\0\1\4\5\0\1\4"+
    "\2\0\53\4\1\0\115\4\1\0\4\4\2\0\7\4"+
    "\1\0\1\4\1\0\4\4\2\0\51\4\1\0\4\4"+
    "\2\0\41\4\1\0\4\4\2\0\7\4\1\0\1\4"+
    "\1\0\4\4\2\0\17\4\1\0\71\4\1\0\4\4"+
    "\2\0\103\4\2\0\3\4\40\0\20\4\20\0\126\4"+
    "\2\0\6\4\3\0\u016c\4\2\0\21\4\1\1\32\4"+
    "\5\0\113\4\3\0\13\4\7\0\15\4\1\0\7\4"+
    "\13\0\25\4\13\0\24\4\14\0\15\4\1\0\3\4"+
    "\1\0\2\4\14\0\124\4\3\0\1\4\4\0\2\4"+
    "\2\0\12\4\41\0\3\4\2\0\12\4\6\0\131\4"+
    "\7\0\53\4\5\0\106\4\12\0\37\4\1\0\14\4"+
    "\4\0\14\4\12\0\50\4\2\0\5\4\13\0\54\4"+
    "\4\0\32\4\6\0\12\4\46\0\34\4\4\0\77\4"+
    "\1\0\35\4\2\0\13\4\6\0\12\4\15\0\1\4"+
    "\10\0\17\4\101\0\114\4\4\0\12\4\21\0\11\4"+
    "\14\0\164\4\14\0\70\4\10\0\12\4\3\0\61\4"+
    "\2\0\11\4\7\0\53\4\2\0\3\4\20\0\3\4"+
    "\1\0\47\4\5\0\372\4\1\0\33\4\2\0\6\4"+
    "\2\0\46\4\2\0\6\4\2\0\10\4\1\0\1\4"+
    "\1\0\1\4\1\0\1\4\1\0\37\4\2\0\65\4"+
    "\1\0\7\4\1\0\1\4\3\0\3\4\1\0\7\4"+
    "\3\0\4\4\2\0\6\4\4\0\15\4\5\0\3\4"+
    "\1\0\7\4\3\0\13\1\35\0\2\3\5\0\1\1"+
    "\17\0\2\4\23\0\1\4\12\0\1\1\21\0\1\4"+
    "\15\0\1\4\20\0\15\4\63\0\41\4\21\0\1\4"+
    "\4\0\1\4\2\0\12\4\1\0\1\4\3\0\5\4"+
    "\6\0\1\4\1\0\1\4\1\0\1\4\1\0\4\4"+
    "\1\0\13\4\2\0\4\4\5\0\5\4\4\0\1\4"+
    "\21\0\51\4\u022d\0\64\4\26\0\57\4\1\0\57\4"+
    "\1\0\205\4\6\0\11\4\14\0\46\4\1\0\1\4"+
    "\5\0\1\4\2\0\70\4\7\0\1\4\17\0\30\4"+
    "\11\0\7\4\1\0\7\4\1\0\7\4\1\0\7\4"+
    "\1\0\7\4\1\0\7\4\1\0\7\4\1\0\7\4"+
    "\1\0\40\4\57\0\1\4\320\0\1\1\4\0\3\4"+
    "\31\0\17\4\1\0\5\4\2\0\5\4\4\0\126\4"+
    "\2\0\2\4\2\0\3\4\1\0\132\4\1\0\4\4"+
    "\5\0\53\4\1\0\136\4\21\0\33\4\65\0\306\4"+
    "\112\0\360\4\20\0\215\4\103\0\56\4\2\0\15\4"+
    "\3\0\34\4\24\0\63\4\1\0\12\4\1\0\163\4"+
    "\45\0\11\4\2\0\147\4\2\0\65\4\2\0\5\4"+
    "\60\0\61\4\30\0\64\4\14\0\106\4\12\0\12\4"+
    "\6\0\30\4\3\0\1\4\1\0\61\4\2\0\44\4"+
    "\14\0\35\4\3\0\101\4\16\0\13\4\6\0\37\4"+
    "\1\0\67\4\11\0\16\4\2\0\12\4\6\0\27\4"+
    "\3\0\111\4\30\0\3\4\2\0\20\4\2\0\5\4"+
    "\12\0\6\4\2\0\6\4\2\0\6\4\11\0\7\4"+
    "\1\0\7\4\1\0\53\4\1\0\14\4\10\0\173\4"+
    "\1\0\2\4\2\0\12\4\6\0\244\4\14\0\27\4"+
    "\4\0\61\4\4\0\u0100\6\156\4\2\0\152\4\46\0"+
    "\7\4\14\0\5\4\5\0\14\4\1\0\15\4\1\0"+
    "\5\4\1\0\1\4\1\0\2\4\1\0\2\4\1\0"+
    "\154\4\41\0\153\4\22\0\100\4\2\0\66\4\50\0"+
    "\14\4\4\0\20\4\20\0\20\4\3\0\2\4\30\0"+
    "\3\4\40\0\5\4\1\0\207\4\23\0\12\4\7\0"+
    "\32\4\4\0\1\4\1\0\32\4\13\0\131\4\3\0"+
    "\6\4\2\0\6\4\2\0\6\4\2\0\3\4\43\0"+
    "\14\4\1\0\32\4\1\0\23\4\1\0\2\4\1\0"+
    "\17\4\2\0\16\4\42\0\173\4\105\0\65\4\210\0"+
    "\1\4\202\0\35\4\3\0\61\4\17\0\1\4\37\0"+
    "\40\4\15\0\36\4\5\0\53\4\5\0\36\4\2\0"+
    "\44\4\4\0\10\4\1\0\5\4\52\0\236\4\2\0"+
    "\12\4\6\0\44\4\4\0\44\4\4\0\50\4\10\0"+
    "\64\4\234\0\67\4\11\0\26\4\12\0\10\4\230\0"+
    "\6\4\2\0\1\4\1\0\54\4\1\0\2\4\3\0"+
    "\1\4\2\0\27\4\12\0\27\4\11\0\37\4\101\0"+
    "\23\4\1\0\2\4\12\0\26\4\12\0\32\4\106\0"+
    "\70\4\6\0\2\4\100\0\4\4\1\0\2\4\5\0"+
    "\10\4\1\0\3\4\1\0\35\4\2\0\3\4\4\0"+
    "\1\4\40\0\35\4\3\0\35\4\43\0\10\4\1\0"+
    "\36\4\31\0\66\4\12\0\26\4\12\0\23\4\15\0"+
    "\22\4\156\0\111\4\67\0\63\4\15\0\63\4\15\0"+
    "\50\4\10\0\12\4\306\0\35\4\12\0\1\4\10\0"+
    "\41\4\217\0\27\4\11\0\107\4\37\0\12\4\17\0"+
    "\74\4\25\0\31\4\7\0\12\4\6\0\65\4\1\0"+
    "\12\4\4\0\3\4\11\0\44\4\2\0\1\4\11\0"+
    "\105\4\4\0\4\4\3\0\13\4\1\0\1\4\43\0"+
    "\22\4\1\0\45\4\6\0\1\4\101\0\7\4\1\0"+
    "\1\4\1\0\4\4\1\0\17\4\1\0\12\4\7\0"+
    "\73\4\5\0\12\4\6\0\4\4\1\0\10\4\2\0"+
    "\2\4\2\0\26\4\1\0\7\4\1\0\2\4\1\0"+
    "\5\4\1\0\12\4\2\0\2\4\2\0\3\4\2\0"+
    "\1\4\6\0\1\4\5\0\7\4\2\0\7\4\3\0"+
    "\5\4\213\0\113\4\5\0\12\4\4\0\2\4\40\0"+
    "\106\4\1\0\1\4\10\0\12\4\246\0\66\4\2\0"+
    "\11\4\27\0\6\4\42\0\101\4\3\0\1\4\13\0"+
    "\12\4\46\0\71\4\7\0\12\4\66\0\33\4\2\0"+
    "\17\4\4\0\12\4\306\0\73\4\145\0\112\4\25\0"+
    "\1\4\240\0\10\4\2\0\56\4\2\0\10\4\1\0"+
    "\2\4\33\0\77\4\10\0\1\4\10\0\112\4\3\0"+
    "\1\4\42\0\71\4\7\0\11\4\1\0\55\4\1\0"+
    "\11\4\17\0\12\4\30\0\36\4\2\0\26\4\1\0"+
    "\16\4\111\0\7\4\1\0\2\4\1\0\54\4\3\0"+
    "\1\4\1\0\2\4\1\0\11\4\10\0\12\4\6\0"+
    "\6\4\1\0\2\4\1\0\45\4\1\0\2\4\1\0"+
    "\6\4\7\0\12\4\u0136\0\27\4\11\0\232\4\146\0"+
    "\157\4\21\0\304\4\274\0\57\4\321\0\107\4\271\0"+
    "\71\4\7\0\37\4\1\0\12\4\146\0\36\4\2\0"+
    "\5\4\13\0\67\4\11\0\4\4\14\0\12\4\11\0"+
    "\25\4\5\0\23\4\260\0\100\4\200\0\113\4\4\0"+
    "\71\4\7\0\21\4\100\0\2\4\1\0\1\4\34\0"+
    "\370\4\10\0\363\4\15\0\37\4\61\0\3\4\21\0"+
    "\4\4\10\0\u018c\4\4\0\153\4\5\0\15\4\3\0"+
    "\11\4\7\0\12\4\3\0\2\4\306\0\5\4\3\0"+
    "\6\4\10\0\10\4\2\0\7\4\36\0\4\4\224\0"+
    "\3\4\273\0\125\4\1\0\107\4\1\0\2\4\2\0"+
    "\1\4\2\0\2\4\2\0\4\4\1\0\14\4\1\0"+
    "\1\4\1\0\7\4\1\0\101\4\1\0\4\4\2\0"+
    "\10\4\1\0\7\4\1\0\34\4\1\0\4\4\1\0"+
    "\5\4\1\0\1\4\3\0\7\4\1\0\u0154\4\2\0"+
    "\31\4\1\0\31\4\1\0\37\4\1\0\31\4\1\0"+
    "\37\4\1\0\31\4\1\0\37\4\1\0\31\4\1\0"+
    "\37\4\1\0\31\4\1\0\10\4\2\0\151\4\4\0"+
    "\62\4\10\0\1\4\16\0\1\4\26\0\5\4\1\0"+
    "\17\4\120\0\7\4\1\0\21\4\2\0\7\4\1\0"+
    "\2\4\1\0\5\4\325\0\55\4\3\0\16\4\2\0"+
    "\12\4\4\0\1\4\u0171\0\72\4\6\0\305\4\13\0"+
    "\7\4\51\0\114\4\4\0\12\4\246\0\4\4\1\0"+
    "\33\4\1\0\2\4\1\0\1\4\2\0\1\4\1\0"+
    "\12\4\1\0\4\4\1\0\1\4\1\0\1\4\6\0"+
    "\1\4\4\0\1\4\1\0\1\4\1\0\1\4\1\0"+
    "\3\4\1\0\2\4\1\0\1\4\2\0\1\4\1\0"+
    "\1\4\1\0\1\4\1\0\1\4\1\0\1\4\1\0"+
    "\2\4\1\0\1\4\2\0\4\4\1\0\7\4\1\0"+
    "\4\4\1\0\4\4\1\0\1\4\1\0\12\4\1\0"+
    "\21\4\5\0\3\4\1\0\5\4\1\0\21\4\164\0"+
    "\32\4\6\0\32\4\6\0\32\4\166\0\327\4\51\0"+
    "\65\4\13\0\336\4\2\0\u0182\4\16\0\u0131\4\37\0"+
    "\36\4\342\0";

  private static int [] zzUnpackcmap_blocks() {
    int [] result = new int[29696];
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
    "\2\0\1\1\2\2\1\1\1\3\1\2\2\0\1\4";

  private static int [] zzUnpackAction() {
    int [] result = new int[11];
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
    "\0\0\0\7\0\16\0\25\0\34\0\43\0\52\0\61"+
    "\0\34\0\43\0\16";

  private static int [] zzUnpackRowMap() {
    int [] result = new int[11];
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
    "\1\3\1\4\1\5\1\4\1\6\2\3\1\7\1\10"+
    "\1\5\1\4\2\7\1\3\10\0\1\4\1\5\1\4"+
    "\4\0\1\11\2\5\7\0\1\12\1\13\1\0\2\7"+
    "\2\0\2\7\1\0\1\7\1\10\1\5\1\4\2\7"+
    "\1\0";

  private static int [] zzUnpacktrans() {
    int [] result = new int[56];
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
    "\2\0\1\11\5\1\2\0\1\11";

  private static int [] zzUnpackAttribute() {
    int [] result = new int[11];
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
    public ParadoxExpressionLexer() {
        this((java.io.Reader)null);
    }


  /**
   * Creates a new scanner
   *
   * @param   in  the java.io.Reader to read input from.
   */
  public ParadoxExpressionLexer(java.io.Reader in) {
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
            { return BAD_CHARACTER;
            }
          // fall through
          case 5: break;
          case 2:
            { return WHITE_SPACE;
            }
          // fall through
          case 6: break;
          case 3:
            { return EXPRESSION_TOKEN;
            }
          // fall through
          case 7: break;
          case 4:
            { yybegin(WAITING_EXPRESSION_TOKEN); return EXPRESSION_PREFIX;
            }
          // fall through
          case 8: break;
          default:
            zzScanError(ZZ_NO_MATCH);
          }
      }
    }
  }


}
