// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import static icu.windea.pls.cwt.psi.CwtParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CwtOptionParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return option_comment(b, l + 1);
  }

  /* ********************************************************** */
  // option_key option_separator option_value
  public static boolean option(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option")) return false;
    if (!nextTokenIs(b, OPTION_KEY_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPTION, null);
    r = option_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, option_separator(b, l + 1));
    r = p && option_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // OPTION_COMMENT_START option_comment_item ?
  static boolean option_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment")) return false;
    if (!nextTokenIs(b, OPTION_COMMENT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, OPTION_COMMENT_START);
    p = r; // pin = 1
    r = r && option_comment_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // option_comment_item ?
  private static boolean option_comment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_1")) return false;
    option_comment_item(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // option | option_value
  static boolean option_comment_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item")) return false;
    boolean r;
    r = option(b, l + 1);
    if (!r) r = option_value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // OPTION_KEY_TOKEN
  public static boolean option_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_key")) return false;
    if (!nextTokenIs(b, OPTION_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPTION_KEY_TOKEN);
    exit_section_(b, m, OPTION_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN | NOT_EQUAL_SIGN
  static boolean option_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_separator")) return false;
    if (!nextTokenIs(b, "", EQUAL_SIGN, NOT_EQUAL_SIGN)) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    return r;
  }

  /* ********************************************************** */
  // <<parseOptionValue>>
  static boolean option_value(PsiBuilder b, int l) {
    return parseOptionValue(b, l + 1);
  }

}
