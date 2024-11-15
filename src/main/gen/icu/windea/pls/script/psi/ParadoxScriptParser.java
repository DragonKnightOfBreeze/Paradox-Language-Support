// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import static icu.windea.pls.script.psi.ParadoxScriptParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.WhitespacesBinders.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ParadoxScriptParser implements PsiParser, LightPsiParser {

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
    create_token_set_(INLINE_MATH_FACTOR, INLINE_MATH_NUMBER, INLINE_MATH_PARAMETER, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE),
    create_token_set_(INLINE_MATH_ABS_EXPRESSION, INLINE_MATH_BI_EXPRESSION, INLINE_MATH_EXPRESSION, INLINE_MATH_PAR_EXPRESSION,
      INLINE_MATH_UNARY_EXPRESSION),
    create_token_set_(BLOCK, BOOLEAN, COLOR, FLOAT,
      INLINE_MATH, INT, SCRIPTED_VARIABLE_REFERENCE, STRING,
      VALUE),
  };

  /* ********************************************************** */
  // LEFT_BRACE block_item * RIGHT_BRACE
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, "<block>");
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, block_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, block_auto_recover_);
    return r || p;
  }

  // block_item *
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMENT | block_value | property | scripted_variable | parameter_condition
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMENT);
    if (!r) r = block_value(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = scripted_variable(b, l + 1);
    if (!r) r = parameter_condition(b, l + 1);
    exit_section_(b, l, m, r, false, block_item_auto_recover_);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
  static boolean block_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BOOLEAN_TOKEN
  public static boolean boolean_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_$")) return false;
    if (!nextTokenIs(b, BOOLEAN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, BOOLEAN_TOKEN);
    exit_section_(b, m, BOOLEAN, r);
    return r;
  }

  /* ********************************************************** */
  // COLOR_TOKEN
  public static boolean color(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "color")) return false;
    if (!nextTokenIs(b, COLOR_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLOR_TOKEN);
    exit_section_(b, m, COLOR, r);
    return r;
  }

  /* ********************************************************** */
  // FLOAT_TOKEN
  public static boolean float_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "float_$")) return false;
    if (!nextTokenIs(b, FLOAT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, FLOAT_TOKEN);
    exit_section_(b, m, FLOAT, r);
    return r;
  }

  /* ********************************************************** */
  // INLINE_MATH_START inline_math_expr INLINE_MATH_END
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH, "<inline math>");
    r = consumeToken(b, INLINE_MATH_START);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_expr(b, l + 1));
    r = p && consumeToken(b, INLINE_MATH_END) && r;
    exit_section_(b, l, m, r, p, inline_math_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // inline_math_expr
  static boolean inline_math_abs_expr(PsiBuilder b, int l) {
    return inline_math_expr(b, l + 1);
  }

  /* ********************************************************** */
  // LABS_SIGN inline_math_abs_expr RABS_SIGN
  public static boolean inline_math_abs_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_abs_expression")) return false;
    if (!nextTokenIs(b, LABS_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_ABS_EXPRESSION, null);
    r = consumeToken(b, LABS_SIGN);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_abs_expr(b, l + 1));
    r = p && consumeToken(b, RABS_SIGN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // inline_math_bi_op inline_math_bi_right_factor
  public static boolean inline_math_bi_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_bi_expression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, INLINE_MATH_BI_EXPRESSION, "<inline math bi expression>");
    r = inline_math_bi_op(b, l + 1);
    p = r; // pin = 1
    r = r && inline_math_bi_right_factor(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PLUS_SIGN | MINUS_SIGN | TIMES_SIGN | DIV_SIGN | MOD_SIGN
  static boolean inline_math_bi_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_bi_op")) return false;
    boolean r;
    r = consumeToken(b, PLUS_SIGN);
    if (!r) r = consumeToken(b, MINUS_SIGN);
    if (!r) r = consumeToken(b, TIMES_SIGN);
    if (!r) r = consumeToken(b, DIV_SIGN);
    if (!r) r = consumeToken(b, MOD_SIGN);
    return r;
  }

  /* ********************************************************** */
  // inline_math_unary_expression | inline_math_abs_expression | inline_math_par_expression | inline_math_factor
  static boolean inline_math_bi_right_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_bi_right_factor")) return false;
    boolean r;
    r = inline_math_unary_expression(b, l + 1);
    if (!r) r = inline_math_abs_expression(b, l + 1);
    if (!r) r = inline_math_par_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // (inline_math_unary_expression | inline_math_abs_expression | inline_math_par_expression | inline_math_factor) inline_math_bi_expression *
  static boolean inline_math_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_expr_0(b, l + 1);
    r = r && inline_math_expr_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // inline_math_unary_expression | inline_math_abs_expression | inline_math_par_expression | inline_math_factor
  private static boolean inline_math_expr_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_0")) return false;
    boolean r;
    r = inline_math_unary_expression(b, l + 1);
    if (!r) r = inline_math_abs_expression(b, l + 1);
    if (!r) r = inline_math_par_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    return r;
  }

  // inline_math_bi_expression *
  private static boolean inline_math_expr_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_bi_expression(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_expr_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // inline_math_unary_expression | inline_math_abs_expression | inline_math_par_expression | inline_math_bi_expression
  public static boolean inline_math_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_EXPRESSION, "<inline math expression>");
    r = inline_math_unary_expression(b, l + 1);
    if (!r) r = inline_math_abs_expression(b, l + 1);
    if (!r) r = inline_math_par_expression(b, l + 1);
    if (!r) r = inline_math_bi_expression(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_number | inline_math_parameter | inline_math_scripted_variable_reference
  public static boolean inline_math_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_factor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_FACTOR, "<inline math factor>");
    r = inline_math_number(b, l + 1);
    if (!r) r = inline_math_parameter(b, l + 1);
    if (!r) r = inline_math_scripted_variable_reference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // INT_NUMBER_TOKEN | FLOAT_NUMBER_TOKEN
  public static boolean inline_math_number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_number")) return false;
    if (!nextTokenIs(b, "<inline math number>", FLOAT_NUMBER_TOKEN, INT_NUMBER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_NUMBER, "<inline math number>");
    r = consumeToken(b, INT_NUMBER_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_NUMBER_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_expr
  static boolean inline_math_par_expr(PsiBuilder b, int l) {
    return inline_math_expr(b, l + 1);
  }

  /* ********************************************************** */
  // LP_SIGN inline_math_par_expr RP_SIGN
  public static boolean inline_math_par_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_par_expression")) return false;
    if (!nextTokenIs(b, LP_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_PAR_EXPRESSION, null);
    r = consumeToken(b, LP_SIGN);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_par_expr(b, l + 1));
    r = p && consumeToken(b, RP_SIGN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PARAMETER_START PARAMETER_TOKEN [PIPE parameter_default_value] PARAMETER_END
  public static boolean inline_math_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter")) return false;
    if (!nextTokenIs(b, PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_PARAMETER, null);
    r = consumeTokens(b, 2, PARAMETER_START, PARAMETER_TOKEN);
    p = r; // pin = 2
    r = r && report_error_(b, inline_math_parameter_2(b, l + 1));
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PIPE parameter_default_value]
  private static boolean inline_math_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter_2")) return false;
    inline_math_parameter_2_0(b, l + 1);
    return true;
  }

  // PIPE parameter_default_value
  private static boolean inline_math_parameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && parameter_default_value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // inline_math_scripted_variable_reference_part <<checkRightTemplate>> (<<processTemplate>> inline_math_scripted_variable_reference_part) *
  public static boolean inline_math_scripted_variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE, "<inline math scripted variable reference>");
    r = inline_math_scripted_variable_reference_part(b, l + 1);
    r = r && checkRightTemplate(b, l + 1);
    r = r && inline_math_scripted_variable_reference_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<processTemplate>> inline_math_scripted_variable_reference_part) *
  private static boolean inline_math_scripted_variable_reference_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_scripted_variable_reference_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_scripted_variable_reference_2", c)) break;
    }
    return true;
  }

  // <<processTemplate>> inline_math_scripted_variable_reference_part
  private static boolean inline_math_scripted_variable_reference_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && inline_math_scripted_variable_reference_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN | inline_math_parameter | inline_parameter_condition
  static boolean inline_math_scripted_variable_reference_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_part")) return false;
    boolean r;
    r = consumeToken(b, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE_TOKEN);
    if (!r) r = inline_math_parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_math_abs_expression | inline_math_par_expression | inline_math_factor
  static boolean inline_math_unary_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_unary_expr")) return false;
    boolean r;
    r = inline_math_abs_expression(b, l + 1);
    if (!r) r = inline_math_par_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_math_unary_op inline_math_unary_expr
  public static boolean inline_math_unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_unary_expression")) return false;
    if (!nextTokenIs(b, "<inline math unary expression>", MINUS_SIGN, PLUS_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_UNARY_EXPRESSION, "<inline math unary expression>");
    r = inline_math_unary_op(b, l + 1);
    p = r; // pin = 1
    r = r && inline_math_unary_expr(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PLUS_SIGN | MINUS_SIGN
  static boolean inline_math_unary_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_unary_op")) return false;
    if (!nextTokenIs(b, "", MINUS_SIGN, PLUS_SIGN)) return false;
    boolean r;
    r = consumeToken(b, PLUS_SIGN);
    if (!r) r = consumeToken(b, MINUS_SIGN);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET <<processInlineParameterCondition>> parameter_condition_expr inline_parameter_condition_item RIGHT_BRACKET
  public static boolean inline_parameter_condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition")) return false;
    if (!nextTokenIs(b, LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_PARAMETER_CONDITION, null);
    r = consumeToken(b, LEFT_BRACKET);
    r = r && processInlineParameterCondition(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, parameter_condition_expr(b, l + 1));
    r = p && report_error_(b, inline_parameter_condition_item(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // <<processSnippet>> inline_parameter_condition_snippet <<postProcessSnippet>>
  static boolean inline_parameter_condition_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processSnippet(b, l + 1);
    r = r && inline_parameter_condition_snippet(b, l + 1);
    r = r && postProcessSnippet(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SNIPPET_TOKEN | parameter | inline_parameter_condition
  static boolean inline_parameter_condition_item_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition_item_part")) return false;
    boolean r;
    r = consumeToken(b, SNIPPET_TOKEN);
    if (!r) r = parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_parameter_condition_item_part (<<processTemplate>> inline_parameter_condition_item_part) *
  static boolean inline_parameter_condition_snippet(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition_snippet")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_parameter_condition_item_part(b, l + 1);
    r = r && inline_parameter_condition_snippet_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (<<processTemplate>> inline_parameter_condition_item_part) *
  private static boolean inline_parameter_condition_snippet_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition_snippet_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_parameter_condition_snippet_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_parameter_condition_snippet_1", c)) break;
    }
    return true;
  }

  // <<processTemplate>> inline_parameter_condition_item_part
  private static boolean inline_parameter_condition_snippet_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_parameter_condition_snippet_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && inline_parameter_condition_item_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INT_TOKEN
  public static boolean int_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "int_$")) return false;
    if (!nextTokenIs(b, INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INT_TOKEN);
    exit_section_(b, m, INT, r);
    return r;
  }

  /* ********************************************************** */
  // PARAMETER_START PARAMETER_TOKEN [PIPE parameter_default_value] PARAMETER_END
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, null);
    r = consumeTokens(b, 2, PARAMETER_START, PARAMETER_TOKEN);
    p = r; // pin = 2
    r = r && report_error_(b, parameter_2(b, l + 1));
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PIPE parameter_default_value]
  private static boolean parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2")) return false;
    parameter_2_0(b, l + 1);
    return true;
  }

  // PIPE parameter_default_value
  private static boolean parameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && parameter_default_value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET parameter_condition_expr parameter_condition_item * RIGHT_BRACKET
  public static boolean parameter_condition(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_CONDITION, "<parameter condition>");
    r = consumeToken(b, LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_condition_expr(b, l + 1));
    r = p && report_error_(b, parameter_condition_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, parameter_condition_auto_recover_);
    return r || p;
  }

  // parameter_condition_item *
  private static boolean parameter_condition_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!parameter_condition_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "parameter_condition_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // NESTED_LEFT_BRACKET parameter_condition_expression NESTED_RIGHT_BRACKET
  static boolean parameter_condition_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_expr")) return false;
    if (!nextTokenIs(b, NESTED_LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, NESTED_LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_condition_expression(b, l + 1));
    r = p && consumeToken(b, NESTED_RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // NOT_SIGN ? parameter_condition_parameter
  public static boolean parameter_condition_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_expression")) return false;
    if (!nextTokenIs(b, "<parameter condition expression>", CONDITION_PARAMETER_TOKEN, NOT_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_CONDITION_EXPRESSION, "<parameter condition expression>");
    r = parameter_condition_expression_0(b, l + 1);
    r = r && parameter_condition_parameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT_SIGN ?
  private static boolean parameter_condition_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_expression_0")) return false;
    consumeToken(b, NOT_SIGN);
    return true;
  }

  /* ********************************************************** */
  // COMMENT | parameter_condition_value | property | parameter_condition
  static boolean parameter_condition_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMENT);
    if (!r) r = parameter_condition_value(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = parameter_condition(b, l + 1);
    exit_section_(b, l, m, r, false, parameter_condition_item_auto_recover_);
    return r;
  }

  /* ********************************************************** */
  // CONDITION_PARAMETER_TOKEN
  public static boolean parameter_condition_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_parameter")) return false;
    if (!nextTokenIs(b, CONDITION_PARAMETER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, CONDITION_PARAMETER_TOKEN);
    exit_section_(b, m, PARAMETER_CONDITION_PARAMETER, r);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
  static boolean parameter_condition_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SNIPPET_TOKEN
  static boolean parameter_default_value(PsiBuilder b, int l) {
    return consumeToken(b, SNIPPET_TOKEN);
  }

  /* ********************************************************** */
  // property_key property_separator property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = property_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, property_separator(b, l + 1));
    r = p && property_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, property_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // property_key_part (<<processTemplate>> property_key_part) *
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
    r = property_key_part(b, l + 1);
    r = r && property_key_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<processTemplate>> property_key_part) *
  private static boolean property_key_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_key_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_key_1", c)) break;
    }
    return true;
  }

  // <<processTemplate>> property_key_part
  private static boolean property_key_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && property_key_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN | parameter | inline_parameter_condition
  static boolean property_key_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_part")) return false;
    boolean r;
    r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN | LT_SIGN | GT_SIGN | LE_SIGN | GE_SIGN | NOT_EQUAL_SIGN | QUESTION_EQUAL_SIGN
  static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, LT_SIGN);
    if (!r) r = consumeToken(b, GT_SIGN);
    if (!r) r = consumeToken(b, LE_SIGN);
    if (!r) r = consumeToken(b, GE_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    if (!r) r = consumeToken(b, QUESTION_EQUAL_SIGN);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
  static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // root_block ?
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    root_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // root_block_item +
  public static boolean root_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROOT_BLOCK, "<root block>");
    r = root_block_item(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!root_block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_block", c)) break;
    }
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMENT | root_block_value | property | scripted_variable | parameter_condition
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMENT);
    if (!r) r = root_block_value(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = scripted_variable(b, l + 1);
    if (!r) r = parameter_condition(b, l + 1);
    exit_section_(b, l, m, r, false, root_block_item_auto_recover_);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
  static boolean root_block_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_name scripted_variable_separator scripted_variable_value
  public static boolean scripted_variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE, "<scripted variable>");
    r = scripted_variable_name(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, scripted_variable_separator(b, l + 1));
    r = p && scripted_variable_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, scripted_variable_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // AT scripted_variable_name_part (<<processTemplate>> scripted_variable_name_part) *
  public static boolean scripted_variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE_NAME, null);
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && report_error_(b, scripted_variable_name_part(b, l + 1));
    r = p && scripted_variable_name_2(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (<<processTemplate>> scripted_variable_name_part) *
  private static boolean scripted_variable_name_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!scripted_variable_name_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scripted_variable_name_2", c)) break;
    }
    return true;
  }

  // <<processTemplate>> scripted_variable_name_part
  private static boolean scripted_variable_name_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && scripted_variable_name_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCRIPTED_VARIABLE_NAME_TOKEN | parameter | inline_parameter_condition
  static boolean scripted_variable_name_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_part")) return false;
    boolean r;
    r = consumeToken(b, SCRIPTED_VARIABLE_NAME_TOKEN);
    if (!r) r = parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // AT scripted_variable_reference_part <<checkRightTemplate>> (<<processTemplate>> scripted_variable_reference_part) *
  public static boolean scripted_variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, AT);
    r = r && scripted_variable_reference_part(b, l + 1);
    r = r && checkRightTemplate(b, l + 1);
    r = r && scripted_variable_reference_3(b, l + 1);
    exit_section_(b, m, SCRIPTED_VARIABLE_REFERENCE, r);
    return r;
  }

  // (<<processTemplate>> scripted_variable_reference_part) *
  private static boolean scripted_variable_reference_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_3")) return false;
    while (true) {
      int c = current_position_(b);
      if (!scripted_variable_reference_3_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scripted_variable_reference_3", c)) break;
    }
    return true;
  }

  // <<processTemplate>> scripted_variable_reference_part
  private static boolean scripted_variable_reference_3_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_3_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && scripted_variable_reference_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCRIPTED_VARIABLE_REFERENCE_TOKEN | parameter | inline_parameter_condition
  static boolean scripted_variable_reference_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_part")) return false;
    boolean r;
    r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
    if (!r) r = parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN
  static boolean scripted_variable_separator(PsiBuilder b, int l) {
    return consumeToken(b, EQUAL_SIGN);
  }

  /* ********************************************************** */
  // boolean | int | float | string | inline_math
  static boolean scripted_variable_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_value")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // string_part <<checkRightTemplate>> (<<processTemplate>> string_part) *
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STRING, "<string>");
    r = string_part(b, l + 1);
    r = r && checkRightTemplate(b, l + 1);
    r = r && string_2(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // (<<processTemplate>> string_part) *
  private static boolean string_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!string_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "string_2", c)) break;
    }
    return true;
  }

  // <<processTemplate>> string_part
  private static boolean string_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processTemplate(b, l + 1);
    r = r && string_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN | parameter | inline_parameter_condition
  static boolean string_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_part")) return false;
    boolean r;
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = parameter(b, l + 1);
    if (!r) r = inline_parameter_condition(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | color | block | inline_math
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  static final Parser block_auto_recover_ = (b, l) -> !nextTokenIsFast(b, AT, BOOLEAN_TOKEN,
    COLOR_TOKEN, COMMENT, FLOAT_TOKEN, INLINE_MATH_START, INT_TOKEN, LEFT_BRACE,
    LEFT_BRACKET, PARAMETER_START, PROPERTY_KEY_TOKEN, RIGHT_BRACE, RIGHT_BRACKET, STRING_TOKEN);
  static final Parser block_item_auto_recover_ = block_auto_recover_;
  static final Parser inline_math_auto_recover_ = block_auto_recover_;
  static final Parser parameter_condition_auto_recover_ = block_auto_recover_;
  static final Parser parameter_condition_item_auto_recover_ = block_auto_recover_;
  static final Parser property_auto_recover_ = block_auto_recover_;
  static final Parser root_block_item_auto_recover_ = (b, l) -> !nextTokenIsFast(b, AT, BOOLEAN_TOKEN,
    COLOR_TOKEN, COMMENT, FLOAT_TOKEN, INLINE_MATH_START, INT_TOKEN, LEFT_BRACE,
    LEFT_BRACKET, PARAMETER_START, PROPERTY_KEY_TOKEN, STRING_TOKEN);
  static final Parser scripted_variable_auto_recover_ = block_auto_recover_;
}
