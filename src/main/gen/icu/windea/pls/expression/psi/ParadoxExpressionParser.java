// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*;
import static icu.windea.pls.expression.psi.ParadoxExpressionParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ParadoxExpressionParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, EXTENDS_SETS_);
    Marker m = enter_section_(b, 0, _COLLAPSE_, null);
    r = parse_root_(t, b);
    exit_section_(b, 0, m, t, r, true, TRUE_CONDITION);
  }

  protected boolean parse_root_(IElementType t, PsiBuilder b) {
    return parse_root_(t, b, 0);
  }

  static boolean parse_root_(IElementType t, PsiBuilder b, int l) {
    return root(b, l + 1);
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(DUMMY_SCOPE_FIELD, SCOPE_FIELD, SCOPE_LINK, SCOPE_LINK_FROM_DATA,
      SYSTEM_SCOPE),
  };

  /* ********************************************************** */
  // "identifier_token"
  public static boolean dummy_scope_field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "dummy_scope_field")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DUMMY_SCOPE_FIELD, "<dummy scope field>");
    r = consumeToken(b, "identifier_token");
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // scope_field_expression
  static boolean root(PsiBuilder b, int l) {
    return scope_field_expression(b, l + 1);
  }

  /* ********************************************************** */
  // system_scope | scope_link | scope_link_from_data | dummy_scope_field
  public static boolean scope_field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_field")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, SCOPE_FIELD, "<scope field>");
    r = system_scope(b, l + 1);
    if (!r) r = scope_link(b, l + 1);
    if (!r) r = scope_link_from_data(b, l + 1);
    if (!r) r = dummy_scope_field(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // scope_field ('.' scope_field) *
  public static boolean scope_field_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_field_expression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCOPE_FIELD_EXPRESSION, "<scope field expression>");
    r = scope_field(b, l + 1);
    p = r; // pin = 1
    r = r && scope_field_expression_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // ('.' scope_field) *
  private static boolean scope_field_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_field_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!scope_field_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scope_field_expression_1", c)) break;
    }
    return true;
  }

  // '.' scope_field
  private static boolean scope_field_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_field_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOT);
    r = r && scope_field(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // <<parseScopeLink>>
  public static boolean scope_link(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_link")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, SCOPE_LINK, "<scope link>");
    r = parseScopeLink(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<parseScopeLinkFromData>>
  public static boolean scope_link_from_data(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scope_link_from_data")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, SCOPE_LINK_FROM_DATA, "<scope link from data>");
    r = parseScopeLinkFromData(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<parseSystemScope>>
  public static boolean system_scope(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "system_scope")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, SYSTEM_SCOPE, "<system scope>");
    r = parseSystemScope(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
