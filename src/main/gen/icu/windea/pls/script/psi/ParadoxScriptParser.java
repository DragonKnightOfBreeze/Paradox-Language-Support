// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

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
    create_token_set_(INLINE_MATH_FACTOR, INLINE_MATH_NUMBER, INLINE_MATH_PARAMETER, INLINE_MATH_VARIABLE_REFERENCE),
    create_token_set_(INLINE_MATH_ABS_EXPRESSION, INLINE_MATH_BI_EXPRESSION, INLINE_MATH_EXPRESSION, INLINE_MATH_PAR_EXPRESSION,
      INLINE_MATH_UNARY_EXPRESSION),
    create_token_set_(BLOCK, BOOLEAN, COLOR, FLOAT,
      INLINE_MATH, INT, NUMBER, PARAMETER,
      STRING, VALUE, VARIABLE_REFERENCE),
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
  // END_OF_LINE_COMMENT | COMMENT | property | value | parameter_condition | variable
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    if (!r) r = parameter_condition(b, l + 1);
    if (!r) r = variable(b, l + 1);
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
  // inline_math_number | inline_math_variable_reference | inline_math_parameter
  public static boolean inline_math_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_factor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_FACTOR, "<inline math factor>");
    r = inline_math_number(b, l + 1);
    if (!r) r = inline_math_variable_reference(b, l + 1);
    if (!r) r = inline_math_parameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NUMBER_TOKEN
  public static boolean inline_math_number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_number")) return false;
    if (!nextTokenIs(b, NUMBER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER_TOKEN);
    exit_section_(b, m, INLINE_MATH_NUMBER, r);
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
  // PARAMETER_START PARAMETER_ID [PIPE ARG_NUMBER_TOKEN] PARAMETER_END
  public static boolean inline_math_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter")) return false;
    if (!nextTokenIs(b, PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_PARAMETER, null);
    r = consumeTokens(b, 1, PARAMETER_START, PARAMETER_ID);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_parameter_2(b, l + 1));
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PIPE ARG_NUMBER_TOKEN]
  private static boolean inline_math_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter_2")) return false;
    parseTokens(b, 0, PIPE, ARG_NUMBER_TOKEN);
    return true;
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
  // INLINE_MATH_VARIABLE_REFERENCE_ID
  public static boolean inline_math_variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_variable_reference")) return false;
    if (!nextTokenIs(b, INLINE_MATH_VARIABLE_REFERENCE_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INLINE_MATH_VARIABLE_REFERENCE_ID);
    exit_section_(b, m, INLINE_MATH_VARIABLE_REFERENCE, r);
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
  // key_string_template_entry | key_string_entry
  static boolean key_entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_entry")) return false;
    boolean r;
    r = key_string_template_entry(b, l + 1);
    if (!r) r = key_string_entry(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN | QUOTED_PROPERTY_KEY_TOKEN
  static boolean key_string_entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_entry")) return false;
    if (!nextTokenIs(b, "", PROPERTY_KEY_TOKEN, QUOTED_PROPERTY_KEY_TOKEN)) return false;
    boolean r;
    r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, QUOTED_PROPERTY_KEY_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // KEY_STRING_SNIPPET ? (parameter KEY_STRING_SNIPPET ?) +
  public static boolean key_string_template(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_template")) return false;
    if (!nextTokenIs(b, "<key string template>", KEY_STRING_SNIPPET, PARAMETER_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, KEY_STRING_TEMPLATE, "<key string template>");
    r = key_string_template_0(b, l + 1);
    r = r && key_string_template_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // KEY_STRING_SNIPPET ?
  private static boolean key_string_template_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_template_0")) return false;
    consumeToken(b, KEY_STRING_SNIPPET);
    return true;
  }

  // (parameter KEY_STRING_SNIPPET ?) +
  private static boolean key_string_template_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_template_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = key_string_template_1_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!key_string_template_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "key_string_template_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter KEY_STRING_SNIPPET ?
  private static boolean key_string_template_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_template_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter(b, l + 1);
    r = r && key_string_template_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // KEY_STRING_SNIPPET ?
  private static boolean key_string_template_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "key_string_template_1_0_1")) return false;
    consumeToken(b, KEY_STRING_SNIPPET);
    return true;
  }

  /* ********************************************************** */
  // key_string_template
  static boolean key_string_template_entry(PsiBuilder b, int l) {
    return key_string_template(b, l + 1);
  }

  /* ********************************************************** */
  // int | float
  public static boolean number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number")) return false;
    if (!nextTokenIs(b, "<number>", FLOAT_TOKEN, INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, NUMBER, "<number>");
    r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PARAMETER_START PARAMETER_ID [PIPE (ARG_NUMBER_TOKEN | ARG_STRING_TOKEN)] PARAMETER_END
  public static boolean parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter")) return false;
    if (!nextTokenIs(b, PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER, null);
    r = consumeTokens(b, 1, PARAMETER_START, PARAMETER_ID);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_2(b, l + 1));
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PIPE (ARG_NUMBER_TOKEN | ARG_STRING_TOKEN)]
  private static boolean parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2")) return false;
    parameter_2_0(b, l + 1);
    return true;
  }

  // PIPE (ARG_NUMBER_TOKEN | ARG_STRING_TOKEN)
  private static boolean parameter_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && parameter_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ARG_NUMBER_TOKEN | ARG_STRING_TOKEN
  private static boolean parameter_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_2_0_1")) return false;
    boolean r;
    r = consumeToken(b, ARG_NUMBER_TOKEN);
    if (!r) r = consumeToken(b, ARG_STRING_TOKEN);
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
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, NESTED_LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_condition_expression(b, l + 1));
    r = p && consumeToken(b, NESTED_RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, parameter_condition_expr_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // NOT_SIGN ? parameter_condition_parameter
  public static boolean parameter_condition_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_expression")) return false;
    if (!nextTokenIs(b, "<parameter condition expression>", INPUT_PARAMETER_ID, NOT_SIGN)) return false;
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
  // END_OF_LINE_COMMENT | COMMENT | property | value
  static boolean parameter_condition_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // INPUT_PARAMETER_ID
  public static boolean parameter_condition_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_condition_parameter")) return false;
    if (!nextTokenIs(b, INPUT_PARAMETER_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INPUT_PARAMETER_ID);
    exit_section_(b, m, PARAMETER_CONDITION_PARAMETER, r);
    return r;
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
  // key_entry
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
    r = key_entry(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN | LT_SIGN | GT_SIGN | LE_SIGN | GE_SIGN | NOT_EQUAL_SIGN
  static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, LT_SIGN);
    if (!r) r = consumeToken(b, GT_SIGN);
    if (!r) r = consumeToken(b, LE_SIGN);
    if (!r) r = consumeToken(b, GE_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    return r;
  }

  /* ********************************************************** */
  // value
  public static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_VALUE, "<property value>");
    r = value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
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
  // END_OF_LINE_COMMENT | COMMENT | property | value | variable
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    if (!r) r = variable(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // value_entry
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING, "<string>");
    r = value_entry(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variable_reference | boolean | number | string | color | block | inline_math
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = number(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // value_string_template_entry | value_string_entry
  static boolean value_entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_entry")) return false;
    boolean r;
    r = value_string_template_entry(b, l + 1);
    if (!r) r = value_string_entry(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN | QUOTED_STRING_TOKEN
  static boolean value_string_entry(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_entry")) return false;
    if (!nextTokenIs(b, "", QUOTED_STRING_TOKEN, STRING_TOKEN)) return false;
    boolean r;
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, QUOTED_STRING_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // VALUE_STRING_SNIPPET ? (parameter VALUE_STRING_SNIPPET ?) +
  public static boolean value_string_template(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_template")) return false;
    if (!nextTokenIs(b, "<value string template>", PARAMETER_START, VALUE_STRING_SNIPPET)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VALUE_STRING_TEMPLATE, "<value string template>");
    r = value_string_template_0(b, l + 1);
    r = r && value_string_template_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // VALUE_STRING_SNIPPET ?
  private static boolean value_string_template_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_template_0")) return false;
    consumeToken(b, VALUE_STRING_SNIPPET);
    return true;
  }

  // (parameter VALUE_STRING_SNIPPET ?) +
  private static boolean value_string_template_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_template_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = value_string_template_1_0(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!value_string_template_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "value_string_template_1", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter VALUE_STRING_SNIPPET ?
  private static boolean value_string_template_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_template_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = parameter(b, l + 1);
    r = r && value_string_template_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // VALUE_STRING_SNIPPET ?
  private static boolean value_string_template_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value_string_template_1_0_1")) return false;
    consumeToken(b, VALUE_STRING_SNIPPET);
    return true;
  }

  /* ********************************************************** */
  // value_string_template
  static boolean value_string_template_entry(PsiBuilder b, int l) {
    return value_string_template(b, l + 1);
  }

  /* ********************************************************** */
  // variable_name variable_separator variable_value
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE, "<variable>");
    r = variable_name(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, variable_separator(b, l + 1));
    r = p && variable_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, variable_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // AT VARIABLE_NAME_ID
  public static boolean variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_name")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AT, VARIABLE_NAME_ID);
    exit_section_(b, m, VARIABLE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // AT VARIABLE_REFERENCE_ID
  public static boolean variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_reference")) return false;
    if (!nextTokenIs(b, AT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeTokens(b, 0, AT, VARIABLE_REFERENCE_ID);
    exit_section_(b, m, VARIABLE_REFERENCE, r);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN
  static boolean variable_separator(PsiBuilder b, int l) {
    return consumeToken(b, EQUAL_SIGN);
  }

  /* ********************************************************** */
  // number
  public static boolean variable_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_value")) return false;
    if (!nextTokenIs(b, "<variable value>", FLOAT_TOKEN, INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_VALUE, "<variable value>");
    r = number(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  static final Parser block_auto_recover_ = (b, l) -> !nextTokenIsFast(b, AT, BOOLEAN_TOKEN,
    COLOR_TOKEN, COMMENT, END_OF_LINE_COMMENT, FLOAT_TOKEN, INLINE_MATH_START, INT_TOKEN,
    KEY_STRING_SNIPPET, LEFT_BRACE, LEFT_BRACKET, PARAMETER_START, PROPERTY_KEY_TOKEN, QUOTED_PROPERTY_KEY_TOKEN,
    QUOTED_STRING_TOKEN, RIGHT_BRACE, RIGHT_BRACKET, STRING_TOKEN, VALUE_STRING_SNIPPET);
  static final Parser inline_math_auto_recover_ = block_auto_recover_;
  static final Parser parameter_condition_auto_recover_ = block_auto_recover_;
  static final Parser parameter_condition_expr_auto_recover_ = block_auto_recover_;
  static final Parser property_auto_recover_ = block_auto_recover_;
  static final Parser variable_auto_recover_ = block_auto_recover_;
}
