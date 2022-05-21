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
    create_token_set_(BLOCK, BOOLEAN, COLOR, FLOAT,
      INLINE_MATH, INT, NUMBER, ROOT_BLOCK,
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
  // END_OF_LINE_COMMENT | COMMENT | variable | property | value
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = variable(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
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
  // INLINE_MATH_START inline_math_expression INLINE_MATH_END
  public static boolean inline_math(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH, "<inline math>");
    r = consumeToken(b, INLINE_MATH_START);
    p = r; // pin = 1
    r = r && report_error_(b, inline_math_expression(b, l + 1));
    r = p && consumeToken(b, INLINE_MATH_END) && r;
    exit_section_(b, l, m, r, p, inline_math_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // inline_math_op_factor (inline_math_op inline_math_op_factor) *
  static boolean inline_math_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expression")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_op_factor(b, l + 1);
    r = r && inline_math_expression_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (inline_math_op inline_math_op_factor) *
  private static boolean inline_math_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!inline_math_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "inline_math_expression_1", c)) break;
    }
    return true;
  }

  // inline_math_op inline_math_op_factor
  private static boolean inline_math_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_expression_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_op(b, l + 1);
    r = r && inline_math_op_factor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // inline_math_number | inline_math_variable_reference | inline_math_parameter
  public static boolean inline_math_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_factor")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, INLINE_MATH_FACTOR, "<inline math factor>");
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
  // PLUS_SIGN | MINUS_SIGN | TIMES_SIGN | DIV_SIGN | MOD_SIGN
  static boolean inline_math_op(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_op")) return false;
    boolean r;
    r = consumeToken(b, PLUS_SIGN);
    if (!r) r = consumeToken(b, MINUS_SIGN);
    if (!r) r = consumeToken(b, TIMES_SIGN);
    if (!r) r = consumeToken(b, DIV_SIGN);
    if (!r) r = consumeToken(b, MOD_SIGN);
    return r;
  }

  /* ********************************************************** */
  // (inline_math_factor) | (LABS_SIGN inline_math_expression RABS_SIGN) | (LP_SIGN inline_math_expression RP_SIGN)
  static boolean inline_math_op_factor(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_op_factor")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_op_factor_0(b, l + 1);
    if (!r) r = inline_math_op_factor_1(b, l + 1);
    if (!r) r = inline_math_op_factor_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // (inline_math_factor)
  private static boolean inline_math_op_factor_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_op_factor_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = inline_math_factor(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // LABS_SIGN inline_math_expression RABS_SIGN
  private static boolean inline_math_op_factor_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_op_factor_1")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LABS_SIGN);
    r = r && inline_math_expression(b, l + 1);
    r = r && consumeToken(b, RABS_SIGN);
    exit_section_(b, m, null, r);
    return r;
  }

  // LP_SIGN inline_math_expression RP_SIGN
  private static boolean inline_math_op_factor_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_op_factor_2")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, LP_SIGN);
    r = r && inline_math_expression(b, l + 1);
    r = r && consumeToken(b, RP_SIGN);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // PARAMETER_START PARAMETER_ID [ PIPE NUMBER_TOKEN] PARAMETER_END
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

  // [ PIPE NUMBER_TOKEN]
  private static boolean inline_math_parameter_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "inline_math_parameter_2")) return false;
    parseTokens(b, 0, PIPE, NUMBER_TOKEN);
    return true;
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
  // PROPERTY_KEY_ID | QUOTED_PROPERTY_KEY_ID
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    if (!nextTokenIs(b, "<property key>", PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
    r = consumeToken(b, PROPERTY_KEY_ID);
    if (!r) r = consumeToken(b, QUOTED_PROPERTY_KEY_ID);
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
  // root_block
  static boolean root(PsiBuilder b, int l) {
    return root_block(b, l + 1);
  }

  /* ********************************************************** */
  // root_block_item *
  public static boolean root_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block")) return false;
    Marker m = enter_section_(b, l, _COLLAPSE_, ROOT_BLOCK, "<root block>");
    while (true) {
      int c = current_position_(b);
      if (!root_block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // END_OF_LINE_COMMENT | COMMENT | variable | property | value
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = variable(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN | QUOTED_STRING_TOKEN
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!nextTokenIs(b, "<string>", QUOTED_STRING_TOKEN, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING, "<string>");
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, QUOTED_STRING_TOKEN);
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
  // VARIABLE_NAME_ID
  public static boolean variable_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_name")) return false;
    if (!nextTokenIs(b, VARIABLE_NAME_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VARIABLE_NAME_ID);
    exit_section_(b, m, VARIABLE_NAME, r);
    return r;
  }

  /* ********************************************************** */
  // !(BOOLEAN_TOKEN | INLINE_MATH_START | COLOR_TOKEN | COMMENT | END_OF_LINE_COMMENT | FLOAT_TOKEN | INT_TOKEN | LEFT_BRACE | PROPERTY_KEY_ID | QUOTED_PROPERTY_KEY_ID | QUOTED_STRING_TOKEN | RIGHT_BRACE | STRING_LIKE_TOKEN | STRING_TOKEN | VARIABLE_NAME_ID | VARIABLE_REFERENCE_ID)
  static boolean variable_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !variable_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // BOOLEAN_TOKEN | INLINE_MATH_START | COLOR_TOKEN | COMMENT | END_OF_LINE_COMMENT | FLOAT_TOKEN | INT_TOKEN | LEFT_BRACE | PROPERTY_KEY_ID | QUOTED_PROPERTY_KEY_ID | QUOTED_STRING_TOKEN | RIGHT_BRACE | STRING_LIKE_TOKEN | STRING_TOKEN | VARIABLE_NAME_ID | VARIABLE_REFERENCE_ID
  private static boolean variable_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_recover_0")) return false;
    boolean r;
    r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INLINE_MATH_START);
    if (!r) r = consumeToken(b, COLOR_TOKEN);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, PROPERTY_KEY_ID);
    if (!r) r = consumeToken(b, QUOTED_PROPERTY_KEY_ID);
    if (!r) r = consumeToken(b, QUOTED_STRING_TOKEN);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    if (!r) r = consumeToken(b, STRING_LIKE_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, VARIABLE_NAME_ID);
    if (!r) r = consumeToken(b, VARIABLE_REFERENCE_ID);
    return r;
  }

  /* ********************************************************** */
  // VARIABLE_REFERENCE_ID
  public static boolean variable_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_reference")) return false;
    if (!nextTokenIs(b, VARIABLE_REFERENCE_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, VARIABLE_REFERENCE_ID);
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

  static final Parser block_auto_recover_ = (b, l) -> !nextTokenIsFast(b, BOOLEAN_TOKEN, COLOR_TOKEN,
    COMMENT, END_OF_LINE_COMMENT, FLOAT_TOKEN, INLINE_MATH_START, INT_TOKEN, LEFT_BRACE,
    PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID, QUOTED_STRING_TOKEN, RIGHT_BRACE, STRING_TOKEN, VARIABLE_NAME_ID, VARIABLE_REFERENCE_ID);
  static final Parser inline_math_auto_recover_ = block_auto_recover_;
  static final Parser property_auto_recover_ = block_auto_recover_;
  static final Parser variable_auto_recover_ = block_auto_recover_;
}
