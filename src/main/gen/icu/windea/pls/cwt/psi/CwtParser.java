// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.cwt.psi.CwtTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;

import com.intellij.lang.parser.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class CwtParser implements PsiParser, LightPsiParser {

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
    create_token_set_(CwtTypes.BLOCK, CwtTypes.BOOLEAN, CwtTypes.FLOAT, CwtTypes.INT,
      CwtTypes.NUMBER, CwtTypes.ROOT_BLOCK, CwtTypes.STRING, CwtTypes.VALUE),
  };

  /* ********************************************************** */
  // "{" block_item * "}"
  public static boolean block(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.LEFT_BRACE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.BLOCK, null);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.LEFT_BRACE);
    p = r; // pin = 1
    r = r && report_error_(b, block_1(b, l + 1));
    r = p && GeneratedParserUtilBase.consumeToken(b, CwtTypes.RIGHT_BRACE) && r;
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
  // comment | property | option | value
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    r = comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = option(b, l + 1);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // BOOLEAN_TOKEN
  public static boolean boolean_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "boolean_$")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.BOOLEAN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.BOOLEAN_TOKEN);
    exit_section_(b, m, CwtTypes.BOOLEAN, r);
    return r;
  }

  /* ********************************************************** */
  // documentation_comment | option_comment | COMMENT
  static boolean comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "comment")) return false;
    boolean r;
    r = documentation_comment(b, l + 1);
    if (!r) r = option_comment(b, l + 1);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.COMMENT);
    return r;
  }

  /* ********************************************************** */
  // "###" documentation_text ?
  public static boolean documentation_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentation_comment")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.DOCUMENTATION_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.DOCUMENTATION_COMMENT, null);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.DOCUMENTATION_START);
    p = r; // pin = 1
    r = r && documentation_comment_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // documentation_text ?
  private static boolean documentation_comment_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentation_comment_1")) return false;
    documentation_text(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // DOCUMENTATION_TOKEN
  public static boolean documentation_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "documentation_text")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.DOCUMENTATION_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.DOCUMENTATION_TOKEN);
    exit_section_(b, m, CwtTypes.DOCUMENTATION_TEXT, r);
    return r;
  }

  /* ********************************************************** */
  // FLOAT_TOKEN
  public static boolean float_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "float_$")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.FLOAT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.FLOAT_TOKEN);
    exit_section_(b, m, CwtTypes.FLOAT, r);
    return r;
  }

  /* ********************************************************** */
  // INT_TOKEN
  public static boolean int_$(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "int_$")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.INT_TOKEN);
    exit_section_(b, m, CwtTypes.INT, r);
    return r;
  }

  /* ********************************************************** */
  // int | float
  public static boolean number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "number")) return false;
    if (!nextTokenIs(b, "<number>", CwtTypes.FLOAT_TOKEN, CwtTypes.INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, CwtTypes.NUMBER, "<number>");
    r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // option_key option_separator value
  public static boolean option(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.OPTION_KEY_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.OPTION, null);
    r = option_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, option_separator(b, l + 1));
    r = p && value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // "##" option_comment_item
  public static boolean option_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.OPTION_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.OPTION_COMMENT, null);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.OPTION_START);
    p = r; // pin = 1
    r = r && option_comment_item(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // option | value
  static boolean option_comment_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item")) return false;
    boolean r;
    r = option(b, l + 1);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // OPTION_KEY_TOKEN
  public static boolean option_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_key")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.OPTION_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.OPTION_KEY_TOKEN);
    exit_section_(b, m, CwtTypes.OPTION_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // "=" | "<>"
  public static boolean option_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_separator")) return false;
    if (!nextTokenIs(b, "<option separator>", CwtTypes.EQUAL_SIGN, CwtTypes.NOT_EQUAL_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.OPTION_SEPARATOR, "<option separator>");
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.EQUAL_SIGN);
    if (!r) r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.NOT_EQUAL_SIGN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // property_key property_separator value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.PROPERTY_KEY_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, CwtTypes.PROPERTY, null);
    r = property_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, property_separator(b, l + 1));
    r = p && value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.PROPERTY_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.PROPERTY_KEY_TOKEN);
    exit_section_(b, m, CwtTypes.PROPERTY_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // "="
  public static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.EQUAL_SIGN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.EQUAL_SIGN);
    exit_section_(b, m, CwtTypes.PROPERTY_SEPARATOR, r);
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
    Marker m = enter_section_(b, l, _COLLAPSE_, CwtTypes.ROOT_BLOCK, "<root block>");
    while (true) {
      int c = current_position_(b);
      if (!root_block_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_block", c)) break;
    }
    exit_section_(b, l, m, true, false, null);
    return true;
  }

  /* ********************************************************** */
  // comment | property | value
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    r = comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!GeneratedParserUtilBase.nextTokenIs(b, CwtTypes.STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = GeneratedParserUtilBase.consumeToken(b, CwtTypes.STRING_TOKEN);
    exit_section_(b, m, CwtTypes.STRING, r);
    return r;
  }

  /* ********************************************************** */
  // boolean | number | string | block
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, CwtTypes.VALUE, "<value>");
    r = boolean_$(b, l + 1);
    if (!r) r = number(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
