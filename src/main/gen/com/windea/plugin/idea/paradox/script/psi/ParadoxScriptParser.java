// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;
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
    create_token_set_(BLOCK, BOOLEAN, CODE, COLOR,
      NUMBER, ROOT_BLOCK, STRING, STRING_VALUE,
      VALUE, VARIABLE_REFERENCE),
  };

  /* ********************************************************** */
  // "{" block_item * "}"
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
  // END_OF_LINE_COMMENT | COMMENT | property | value
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
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
  // CODE_START code_text CODE_END
  public static boolean code(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "code")) return false;
    if (!nextTokenIs(b, CODE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CODE, null);
    r = consumeToken(b, CODE_START);
    p = r; // pin = 1
    r = r && report_error_(b, code_text(b, l + 1));
    r = p && consumeToken(b, CODE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // CODE_TEXT_TOKEN
  static boolean code_text(PsiBuilder b, int l) {
    return consumeToken(b, CODE_TEXT_TOKEN);
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
  // NUMBER_TOKEN
  public static boolean number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number")) return false;
    if (!nextTokenIs(b, NUMBER_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, NUMBER_TOKEN);
    exit_section_(b, m, NUMBER, r);
    return r;
  }

  /* ********************************************************** */
  // property_key property_separator property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, "<property>", PROPERTY_KEY_ID, QUOTED_PROPERTY_KEY_ID)) return false;
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
  // "=" | "<" | ">" | "<=" | ">=" | "<>"
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
  // ( END_OF_LINE_COMMENT | COMMENT | variable | property | value) *
  public static boolean root_block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block")) return false;
    Marker m = enter_section_(b, l, _COLLAPSE_, ROOT_BLOCK, "<root block>");
    while (true) {
      int c = current_position_(b);
      if (!root_block_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  // END_OF_LINE_COMMENT | COMMENT | variable | property | value
  private static boolean root_block_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_0")) return false;
    boolean r;
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = variable(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
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
  // color | code | string
  public static boolean string_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, STRING_VALUE, "<string value>");
    r = color(b, l + 1);
    if (!r) r = code(b, l + 1);
    if (!r) r = string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variable_reference | boolean | number | string_value | block
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = variable_reference(b, l + 1);
    if (!r) r = boolean_$(b, l + 1);
    if (!r) r = number(b, l + 1);
    if (!r) r = string_value(b, l + 1);
    if (!r) r = block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // variable_name variable_separator variable_value
  public static boolean variable(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable")) return false;
    if (!nextTokenIs(b, VARIABLE_NAME_ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE, null);
    r = variable_name(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, variable_separator(b, l + 1));
    r = p && variable_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
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
  // "="
  static boolean variable_separator(PsiBuilder b, int l) {
    return consumeToken(b, EQUAL_SIGN);
  }

  /* ********************************************************** */
  // boolean | number | string
  public static boolean variable_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "variable_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, VARIABLE_VALUE, "<variable value>");
    r = boolean_$(b, l + 1);
    if (!r) r = number(b, l + 1);
    if (!r) r = string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
