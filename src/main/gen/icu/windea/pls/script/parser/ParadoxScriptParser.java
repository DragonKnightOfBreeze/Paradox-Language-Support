// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import static icu.windea.pls.script.parser.ParadoxScriptParserUtil.*;
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
    boolean r;
    if (t == INLINE_MATH_ROOT) {
      r = inline_math_root(b, l + 1);
    }
    else {
      r = root(b, l + 1);
    }
    return r;
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(INLINE_MATH_BINARY_EXPRESSION, INLINE_MATH_EXPRESSION, INLINE_MATH_FACTOR, INLINE_MATH_GROUPING_EXPRESSION,
      INLINE_MATH_NUMBER, INLINE_MATH_PARAMETER, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE, INLINE_MATH_UNARY_EXPRESSION),
    create_token_set_(BLOCK, BOOLEAN, COLOR, FLOAT,
      INLINE_MATH, INT, SCRIPTED_VARIABLE_REFERENCE, STRING,
      VALUE),
  };

  /* ********************************************************** */
  // LEFT_BRACE block_items? RIGHT_BRACE
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!nextTokenIs(b, LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, BLOCK, null);
    r = consumeToken(b, LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, block_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_BRACE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // block_items?
  private static boolean block_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_1")) return false;
    block_items(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // comment | scripted_variable | property | value | normal_conditional_block
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = comment(b, l + 1);
    if (!r) r = scripted_variable(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    if (!r) r = normal_conditional_block(b, l + 1);
    exit_section_(b, l, m, r, false, ParadoxScriptParser::block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  //   )
  static boolean block_item_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !block_item_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, COLOR_TOKEN);
    if (!r) r = consumeToken(b, INLINE_MATH_START);
    if (!r) r = consumeToken(b, PARAMETER_START);
    if (!r) r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // block_item+
  static boolean block_items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_items")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = block_item(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "block_items", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // BOOLEAN_TOKEN
  public static boolean boolean_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_$")) return false;
    if (!nextTokenIs(b, "<boolean>", BOOLEAN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, BOOLEAN, "<boolean>");
    r = consumeToken(b, BOOLEAN_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COLOR_TOKEN
  public static boolean color(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "color")) return false;
    if (!nextTokenIs(b, "<color>", COLOR_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLOR, "<color>");
    r = consumeToken(b, COLOR_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMENT
  static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    if (!nextTokenIs(b, "<comment>", COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<comment>");
    r = consumeToken(b, COMMENT);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // NOT_SIGN? conditional_parameter
  public static boolean conditional_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_expression")) return false;
    if (!nextTokenIs(b, "<conditional expression>", CONDITION_PARAMETER_TOKEN, NOT_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_EXPRESSION, "<conditional expression>");
    r = conditional_expression_0(b, l + 1);
    r = r && conditional_parameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // NOT_SIGN?
  private static boolean conditional_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_expression_0")) return false;
    consumeToken(b, NOT_SIGN);
    return true;
  }

  /* ********************************************************** */
  // NESTED_LEFT_BRACKET conditional_expression NESTED_RIGHT_BRACKET
  static boolean conditional_header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_header")) return false;
    if (!nextTokenIs(b, NESTED_LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, NESTED_LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, conditional_expression(b, l + 1));
    r = p && consumeToken(b, NESTED_RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CONDITION_PARAMETER_TOKEN
  public static boolean conditional_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "conditional_parameter")) return false;
    if (!nextTokenIs(b, "<conditional parameter>", CONDITION_PARAMETER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CONDITIONAL_PARAMETER, "<conditional parameter>");
    r = consumeToken(b, CONDITION_PARAMETER_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // FLOAT_TOKEN
  public static boolean float_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "float_$")) return false;
    if (!nextTokenIs(b, "<float>", FLOAT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, FLOAT, "<float>");
    r = consumeToken(b, FLOAT_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET <<processInlineConditionalBlock>> conditional_header inline_conditional_block_item? RIGHT_BRACKET
  public static boolean inline_conditional_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block")) return false;
    if (!nextTokenIs(b, "<inline conditional block>", LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_CONDITIONAL_BLOCK, "<inline conditional block>");
    r = consumeToken(b, LEFT_BRACKET);
    r = r && processInlineConditionalBlock(b, l + 1);
    p = r; // pin = 2
    r = r && report_error_(b, conditional_header(b, l + 1));
    r = p && report_error_(b, inline_conditional_block_3(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // inline_conditional_block_item?
  private static boolean inline_conditional_block_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_3")) return false;
    inline_conditional_block_item(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // inline_conditional_block_item_content
  static boolean inline_conditional_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = inline_conditional_block_item_content(b, l + 1);
    exit_section_(b, l, m, r, false, ParadoxScriptParser::inline_normal_conditional_block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // inline_conditional_block_item_part ( <<processPart>> inline_conditional_block_item_part )*
  static boolean inline_conditional_block_item_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item_content")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_conditional_block_item_part(b, l + 1);
    r = r && inline_conditional_block_item_content_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> inline_conditional_block_item_part )*
  private static boolean inline_conditional_block_item_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item_content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_conditional_block_item_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_conditional_block_item_content_1", c)) break;
    }
    return true;
  }

  // <<processPart>> inline_conditional_block_item_part
  private static boolean inline_conditional_block_item_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && inline_conditional_block_item_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // inline_conditional_block_item_token | normal_parameter | inline_conditional_block
  static boolean inline_conditional_block_item_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item_part")) return false;
    boolean r;
    r = inline_conditional_block_item_token(b, l + 1);
    if (!r) r = normal_parameter(b, l + 1);
    if (!r) r = inline_conditional_block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN | STRING_TOKEN | SCRIPTED_VARIABLE_NAME_TOKEN | SCRIPTED_VARIABLE_REFERENCE_TOKEN
  static boolean inline_conditional_block_item_token(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_conditional_block_item_token")) return false;
    boolean r;
    r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, SCRIPTED_VARIABLE_NAME_TOKEN);
    if (!r) r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
    return r;
  }

  /* ********************************************************** */
  // INLINE_MATH_START INLINE_MATH_TOKEN INLINE_MATH_END
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    if (!nextTokenIs(b, "<inline math>", INLINE_MATH_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH, "<inline math>");
    r = consumeTokens(b, 1, INLINE_MATH_START, INLINE_MATH_TOKEN, INLINE_MATH_END);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // LABS_SIGN inline_math_expr RABS_SIGN
  static boolean inline_math_abs_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_abs_expr")) return false;
    if (!nextTokenIs(b, LABS_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LABS_SIGN);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_expr(b, l + 1));
    r = p && consumeToken(b, RABS_SIGN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // inline_math_add_op inline_math_expr_factor
  public static boolean inline_math_add_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_add_expr")) return false;
    if (!nextTokenIs(b, "<inline math add expr>", MINUS_SIGN, PLUS_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, INLINE_MATH_BINARY_EXPRESSION, "<inline math add expr>");
    r = inline_math_add_op(b, l + 1);
    p = r; // pin = 1
    r = r && inline_math_expr_factor(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PLUS_SIGN | MINUS_SIGN
  static boolean inline_math_add_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_add_op")) return false;
    if (!nextTokenIs(b, "", MINUS_SIGN, PLUS_SIGN)) return false;
    boolean r;
    r = consumeToken(b, PLUS_SIGN);
    if (!r) r = consumeToken(b, MINUS_SIGN);
    return r;
  }

  /* ********************************************************** */
  // inline_math_add_expr | inline_math_mul_expr
  public static boolean inline_math_binary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_binary_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_BINARY_EXPRESSION, "<inline math binary expression>");
    r = inline_math_add_expr(b, l + 1);
    if (!r) r = inline_math_mul_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_expr_term
  static boolean inline_math_expr(PsiBuilder b, int l) {
    return inline_math_expr_term(b, l + 1);
  }

  /* ********************************************************** */
  // inline_math_expr_unary inline_math_mul_expr*
  static boolean inline_math_expr_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_factor")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_expr_unary(b, l + 1);
    r = r && inline_math_expr_factor_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // inline_math_mul_expr*
  private static boolean inline_math_expr_factor_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_factor_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_mul_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_expr_factor_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // inline_math_grouping_expression | inline_math_factor
  static boolean inline_math_expr_primary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_primary")) return false;
    boolean r;
    r = inline_math_grouping_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_math_expr_factor inline_math_add_expr*
  static boolean inline_math_expr_term(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_term")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_expr_factor(b, l + 1);
    r = r && inline_math_expr_term_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // inline_math_add_expr*
  private static boolean inline_math_expr_term_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_term_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_add_expr(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_expr_term_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // inline_math_unary_expression | inline_math_expr_primary
  static boolean inline_math_expr_unary(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expr_unary")) return false;
    boolean r;
    r = inline_math_unary_expression(b, l + 1);
    if (!r) r = inline_math_expr_primary(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_math_binary_expression | inline_math_unary_expression | inline_math_grouping_expression | inline_math_factor
  public static boolean inline_math_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expression")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_EXPRESSION, "<inline math expression>");
    r = inline_math_binary_expression(b, l + 1);
    if (!r) r = inline_math_unary_expression(b, l + 1);
    if (!r) r = inline_math_grouping_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_number | inline_math_scripted_variable_reference | inline_math_parameter
  public static boolean inline_math_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_factor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_FACTOR, "<inline math factor>");
    r = inline_math_number(b, l + 1);
    if (!r) r = inline_math_scripted_variable_reference(b, l + 1);
    if (!r) r = inline_math_parameter(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_par_expr | inline_math_abs_expr
  public static boolean inline_math_grouping_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_grouping_expression")) return false;
    if (!nextTokenIs(b, "<inline math grouping expression>", LABS_SIGN, LP_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_GROUPING_EXPRESSION, "<inline math grouping expression>");
    r = inline_math_par_expr(b, l + 1);
    if (!r) r = inline_math_abs_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // inline_math_mul_op inline_math_expr_unary
  public static boolean inline_math_mul_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_mul_expr")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _LEFT_, INLINE_MATH_BINARY_EXPRESSION, "<inline math mul expr>");
    r = inline_math_mul_op(b, l + 1);
    p = r; // pin = 1
    r = r && inline_math_expr_unary(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // TIMES_SIGN | DIV_SIGN | MOD_SIGN
  static boolean inline_math_mul_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_mul_op")) return false;
    boolean r;
    r = consumeToken(b, TIMES_SIGN);
    if (!r) r = consumeToken(b, DIV_SIGN);
    if (!r) r = consumeToken(b, MOD_SIGN);
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
  // LP_SIGN inline_math_expr RP_SIGN
  static boolean inline_math_par_expr(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_par_expr")) return false;
    if (!nextTokenIs(b, LP_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, LP_SIGN);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_expr(b, l + 1));
    r = p && consumeToken(b, RP_SIGN) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PARAMETER_START parameter_name parameter_argument_part? PARAMETER_END
  public static boolean inline_math_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter")) return false;
    if (!nextTokenIs(b, "<inline math parameter>", PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_PARAMETER, "<inline math parameter>");
    r = consumeToken(b, PARAMETER_START);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_name(b, l + 1));
    r = p && report_error_(b, inline_math_parameter_2(b, l + 1)) && r;
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // parameter_argument_part?
  private static boolean inline_math_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter_2")) return false;
    parameter_argument_part(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // inline_math_expr
  public static boolean inline_math_root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_root")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_ROOT, "<inline math root>");
    r = inline_math_expr(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // AT? inline_math_scripted_variable_reference_content
  public static boolean inline_math_scripted_variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_SCRIPTED_VARIABLE_REFERENCE, "<inline math scripted variable reference>");
    r = inline_math_scripted_variable_reference_0(b, l + 1);
    r = r && inline_math_scripted_variable_reference_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // AT?
  private static boolean inline_math_scripted_variable_reference_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_0")) return false;
    consumeToken(b, AT);
    return true;
  }

  /* ********************************************************** */
  // inline_math_scripted_variable_reference_part ( <<processPart>> inline_math_scripted_variable_reference_part )*
  static boolean inline_math_scripted_variable_reference_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_content")) return false;
    if (!nextTokenIs(b, "", PARAMETER_START, SCRIPTED_VARIABLE_REFERENCE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_scripted_variable_reference_part(b, l + 1);
    r = r && inline_math_scripted_variable_reference_content_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> inline_math_scripted_variable_reference_part )*
  private static boolean inline_math_scripted_variable_reference_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_scripted_variable_reference_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_scripted_variable_reference_content_1", c)) break;
    }
    return true;
  }

  // <<processPart>> inline_math_scripted_variable_reference_part
  private static boolean inline_math_scripted_variable_reference_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && inline_math_scripted_variable_reference_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCRIPTED_VARIABLE_REFERENCE_TOKEN | inline_math_parameter
  static boolean inline_math_scripted_variable_reference_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_scripted_variable_reference_part")) return false;
    if (!nextTokenIs(b, "", PARAMETER_START, SCRIPTED_VARIABLE_REFERENCE_TOKEN)) return false;
    boolean r;
    r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
    if (!r) r = inline_math_parameter(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // inline_math_unary_op inline_math_unary_factor
  public static boolean inline_math_unary_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_unary_expression")) return false;
    if (!nextTokenIs(b, "<inline math unary expression>", MINUS_SIGN, PLUS_SIGN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _COLLAPSE_, INLINE_MATH_UNARY_EXPRESSION, "<inline math unary expression>");
    r = inline_math_unary_op(b, l + 1);
    p = r; // pin = 1
    r = r && inline_math_unary_factor(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // inline_math_grouping_expression | inline_math_factor
  static boolean inline_math_unary_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_unary_factor")) return false;
    boolean r;
    r = inline_math_grouping_expression(b, l + 1);
    if (!r) r = inline_math_factor(b, l + 1);
    return r;
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
  // !( COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  //   )
  static boolean inline_normal_conditional_block_item_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_normal_conditional_block_item_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !inline_normal_conditional_block_item_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean inline_normal_conditional_block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_normal_conditional_block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, COLOR_TOKEN);
    if (!r) r = consumeToken(b, INLINE_MATH_START);
    if (!r) r = consumeToken(b, PARAMETER_START);
    if (!r) r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // INT_TOKEN
  public static boolean int_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "int_$")) return false;
    if (!nextTokenIs(b, "<int>", INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INT, "<int>");
    r = consumeToken(b, INT_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // LEFT_BRACKET conditional_header normal_conditional_block_items? RIGHT_BRACKET
  public static boolean normal_conditional_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block")) return false;
    if (!nextTokenIs(b, "<conditional block>", LEFT_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_CONDITIONAL_BLOCK, "<conditional block>");
    r = consumeToken(b, LEFT_BRACKET);
    p = r; // pin = 1
    r = r && report_error_(b, conditional_header(b, l + 1));
    r = p && report_error_(b, normal_conditional_block_2(b, l + 1)) && r;
    r = p && consumeToken(b, RIGHT_BRACKET) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // normal_conditional_block_items?
  private static boolean normal_conditional_block_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block_2")) return false;
    normal_conditional_block_items(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // comment/* | scripted_variable*/ | property | value | normal_conditional_block
  static boolean normal_conditional_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    if (!r) r = normal_conditional_block(b, l + 1);
    exit_section_(b, l, m, r, false, ParadoxScriptParser::normal_conditional_block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  //   )
  static boolean normal_conditional_block_item_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block_item_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !normal_conditional_block_item_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean normal_conditional_block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, COLOR_TOKEN);
    if (!r) r = consumeToken(b, INLINE_MATH_START);
    if (!r) r = consumeToken(b, PARAMETER_START);
    if (!r) r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // normal_conditional_block_item+
  static boolean normal_conditional_block_items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_conditional_block_items")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = normal_conditional_block_item(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!normal_conditional_block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "normal_conditional_block_items", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PARAMETER_START parameter_name parameter_argument_part? PARAMETER_END
  public static boolean normal_parameter(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_parameter")) return false;
    if (!nextTokenIs(b, "<parameter>", PARAMETER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, NORMAL_PARAMETER, "<parameter>");
    r = consumeToken(b, PARAMETER_START);
    p = r; // pin = 1
    r = r && report_error_(b, parameter_name(b, l + 1));
    r = p && report_error_(b, normal_parameter_2(b, l + 1)) && r;
    r = p && consumeToken(b, PARAMETER_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // parameter_argument_part?
  private static boolean normal_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "normal_parameter_2")) return false;
    parameter_argument_part(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // ARGUMENT_TOKEN
  public static boolean parameter_argument(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_argument")) return false;
    if (!nextTokenIs(b, "<parameter argument>", ARGUMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PARAMETER_ARGUMENT, "<parameter argument>");
    r = consumeToken(b, ARGUMENT_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PIPE parameter_argument?
  static boolean parameter_argument_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_argument_part")) return false;
    if (!nextTokenIs(b, PIPE)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && parameter_argument_part_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // parameter_argument?
  private static boolean parameter_argument_part_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "parameter_argument_part_1")) return false;
    parameter_argument(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // PARAMETER_TOKEN
  static boolean parameter_name(PsiBuilder b, int l) {
    return consumeToken(b, PARAMETER_TOKEN);
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
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // property_key_content
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
    r = property_key_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // property_key_part <<processLhsContent>> ( <<processPart>> property_key_part )*
  static boolean property_key_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_content")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = property_key_part(b, l + 1);
    r = r && processLhsContent(b, l + 1);
    r = r && property_key_content_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> property_key_part )*
  private static boolean property_key_content_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_content_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_key_content_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_key_content_2", c)) break;
    }
    return true;
  }

  // <<processPart>> property_key_part
  private static boolean property_key_content_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_content_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && property_key_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN | normal_parameter | inline_conditional_block
  static boolean property_key_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key_part")) return false;
    boolean r;
    r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = normal_parameter(b, l + 1);
    if (!r) r = inline_conditional_block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN | NOT_EQUAL_SIGN | LE_SIGN | GE_SIGN | LT_SIGN | GT_SIGN | SAFE_ASSIGN_SIGN | SAFE_CALL_ASSIGN_SIGN
  static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    if (!r) r = consumeToken(b, LE_SIGN);
    if (!r) r = consumeToken(b, GE_SIGN);
    if (!r) r = consumeToken(b, LT_SIGN);
    if (!r) r = consumeToken(b, GT_SIGN);
    if (!r) r = consumeToken(b, SAFE_ASSIGN_SIGN);
    if (!r) r = consumeToken(b, SAFE_CALL_ASSIGN_SIGN);
    return r;
  }

  /* ********************************************************** */
  // value
  static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<property value>");
    r = value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // root_block?
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    root_block(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // root_block_items
  public static boolean root_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROOT_BLOCK, "<root block>");
    r = root_block_items(b, l + 1);
    register_hook_(b, WS_BINDERS, GREEDY_LEFT_BINDER, GREEDY_RIGHT_BINDER);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // comment | scripted_variable | property | value | normal_conditional_block
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = comment(b, l + 1);
    if (!r) r = scripted_variable(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    if (!r) r = normal_conditional_block(b, l + 1);
    exit_section_(b, l, m, r, false, ParadoxScriptParser::root_block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  //   )
  static boolean root_block_item_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !root_block_item_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | AT | COLOR_TOKEN | INLINE_MATH_START
  //   | PARAMETER_START | LEFT_BRACKET | RIGHT_BRACKET
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean root_block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, AT);
    if (!r) r = consumeToken(b, COLOR_TOKEN);
    if (!r) r = consumeToken(b, INLINE_MATH_START);
    if (!r) r = consumeToken(b, PARAMETER_START);
    if (!r) r = consumeToken(b, LEFT_BRACKET);
    if (!r) r = consumeToken(b, RIGHT_BRACKET);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // root_block_item+
  static boolean root_block_items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_items")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_block_item(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!root_block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_block_items", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_name scripted_variable_separator scripted_variable_value
  public static boolean scripted_variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable")) return false;
    if (!nextTokenIs(b, "<scripted variable>", AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE, "<scripted variable>");
    r = scripted_variable_name(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, scripted_variable_separator(b, l + 1));
    r = p && scripted_variable_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // AT <<processLhsContent>> scripted_variable_name_content
  public static boolean scripted_variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name")) return false;
    if (!nextTokenIs(b, "<scripted variable name>", AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE_NAME, "<scripted variable name>");
    r = consumeToken(b, AT);
    r = r && processLhsContent(b, l + 1);
    p = r; // pin = 2
    r = r && scripted_variable_name_content(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // scripted_variable_name_part ( <<processPart>> scripted_variable_name_part )*
  static boolean scripted_variable_name_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_content")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_name_part(b, l + 1);
    r = r && scripted_variable_name_content_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> scripted_variable_name_part )*
  private static boolean scripted_variable_name_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!scripted_variable_name_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scripted_variable_name_content_1", c)) break;
    }
    return true;
  }

  // <<processPart>> scripted_variable_name_part
  private static boolean scripted_variable_name_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && scripted_variable_name_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCRIPTED_VARIABLE_NAME_TOKEN | normal_parameter | inline_conditional_block
  static boolean scripted_variable_name_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_name_part")) return false;
    boolean r;
    r = consumeToken(b, SCRIPTED_VARIABLE_NAME_TOKEN);
    if (!r) r = normal_parameter(b, l + 1);
    if (!r) r = inline_conditional_block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // AT scripted_variable_reference_content
  public static boolean scripted_variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference")) return false;
    if (!nextTokenIs(b, "<scripted variable reference>", AT)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SCRIPTED_VARIABLE_REFERENCE, "<scripted variable reference>");
    r = consumeToken(b, AT);
    p = r; // pin = 1
    r = r && scripted_variable_reference_content(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // scripted_variable_reference_part ( <<processPart>> scripted_variable_reference_part )*
  static boolean scripted_variable_reference_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_content")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = scripted_variable_reference_part(b, l + 1);
    r = r && scripted_variable_reference_content_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> scripted_variable_reference_part )*
  private static boolean scripted_variable_reference_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!scripted_variable_reference_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "scripted_variable_reference_content_1", c)) break;
    }
    return true;
  }

  // <<processPart>> scripted_variable_reference_part
  private static boolean scripted_variable_reference_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && scripted_variable_reference_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // SCRIPTED_VARIABLE_REFERENCE_TOKEN | normal_parameter | inline_conditional_block
  static boolean scripted_variable_reference_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "scripted_variable_reference_part")) return false;
    boolean r;
    r = consumeToken(b, SCRIPTED_VARIABLE_REFERENCE_TOKEN);
    if (!r) r = normal_parameter(b, l + 1);
    if (!r) r = inline_conditional_block(b, l + 1);
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
    Marker m = enter_section_(b, l, _NONE_, null, "<scripted variable value>");
    r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // string_content
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STRING, "<string>");
    r = string_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // string_part ( <<processPart>> string_part )*
  static boolean string_content(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_content")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = string_part(b, l + 1);
    r = r && string_content_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ( <<processPart>> string_part )*
  private static boolean string_content_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_content_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!string_content_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "string_content_1", c)) break;
    }
    return true;
  }

  // <<processPart>> string_part
  private static boolean string_content_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_content_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = processPart(b, l + 1);
    r = r && string_part(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN | normal_parameter | inline_conditional_block
  static boolean string_part(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_part")) return false;
    boolean r;
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = normal_parameter(b, l + 1);
    if (!r) r = inline_conditional_block(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // scripted_variable_reference | boolean | int | float | string | block | color | inline_math
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = scripted_variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = block(b, l + 1);
    if (!r) r = color(b, l + 1);
    if (!r) r = inline_math(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
