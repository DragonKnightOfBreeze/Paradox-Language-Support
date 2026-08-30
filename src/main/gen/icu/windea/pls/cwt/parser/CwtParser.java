// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import static icu.windea.pls.cwt.parser.CwtParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.WhitespacesBinders.*;

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
    create_token_set_(BLOCK, BOOLEAN, FLOAT, INT,
      STRING, VALUE),
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
  // general_comment | option | property | value
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = general_comment(b, l + 1);
    if (!r) r = option(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, l, m, r, false, CwtParser::block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN | OPTION_KEY_TOKEN
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

  // COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN | OPTION_KEY_TOKEN
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, OPTION_COMMENT_START);
    if (!r) r = consumeToken(b, DOC_COMMENT_TOKEN);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, OPTION_KEY_TOKEN);
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
  // DOC_COMMENT_TOKEN
  public static boolean doc_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doc_comment")) return false;
    if (!nextTokenIs(b, "<doc comment>", DOC_COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, DOC_COMMENT, "<doc comment>");
    r = consumeToken(b, DOC_COMMENT_TOKEN);
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
  // doc_comment | option_comment | comment
  static boolean general_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "general_comment")) return false;
    boolean r;
    r = doc_comment(b, l + 1);
    if (!r) r = option_comment(b, l + 1);
    if (!r) r = comment(b, l + 1);
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
  // option_key option_separator option_value
  public static boolean option(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option")) return false;
    if (!nextTokenIs(b, "<option>", OPTION_KEY_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, OPTION, "<option>");
    r = option_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, option_separator(b, l + 1));
    r = p && option_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // option_comment_root
  public static boolean option_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment")) return false;
    if (!nextTokenIs(b, "<option comment>", OPTION_COMMENT_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTION_COMMENT, "<option comment>");
    r = option_comment_root(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // <<checkEol>> ( option | option_value )
  static boolean option_comment_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = checkEol(b, l + 1);
    r = r && option_comment_item_1(b, l + 1);
    exit_section_(b, l, m, r, false, CwtParser::option_comment_item_recover);
    return r;
  }

  // option | option_value
  private static boolean option_comment_item_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item_1")) return false;
    boolean r;
    r = option(b, l + 1);
    if (!r) r = option_value(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN | OPTION_KEY_TOKEN
  //   | LEFT_BRACE | RIGHT_BRACE
  //   )
  static boolean option_comment_item_recover(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item_recover")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NOT_);
    r = !option_comment_item_recover_0(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN | OPTION_KEY_TOKEN
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean option_comment_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, OPTION_COMMENT_START);
    if (!r) r = consumeToken(b, DOC_COMMENT_TOKEN);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
    if (!r) r = consumeToken(b, OPTION_KEY_TOKEN);
    if (!r) r = consumeToken(b, LEFT_BRACE);
    if (!r) r = consumeToken(b, RIGHT_BRACE);
    return r;
  }

  /* ********************************************************** */
  // option_comment_item+
  static boolean option_comment_items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_items")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = option_comment_item(b, l + 1);
    while (r) {
      int c = current_position_(b);
      if (!option_comment_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "option_comment_items", c)) break;
    }
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // OPTION_COMMENT_START option_comment_items?
  static boolean option_comment_root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_root")) return false;
    if (!nextTokenIs(b, OPTION_COMMENT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, OPTION_COMMENT_START);
    p = r; // pin = 1
    r = r && option_comment_root_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // option_comment_items?
  private static boolean option_comment_root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_root_1")) return false;
    option_comment_items(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // option_key_content
  public static boolean option_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_key")) return false;
    if (!nextTokenIs(b, "<option key>", OPTION_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, OPTION_KEY, "<option key>");
    r = option_key_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // OPTION_KEY_TOKEN
  static boolean option_key_content(PsiBuilder b, int l) {
    return consumeToken(b, OPTION_KEY_TOKEN);
  }

  /* ********************************************************** */
  // EQUAL_SIGN | NOT_EQUAL_SIGN | DOUBLE_EQUAL_SIGN
  static boolean option_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_separator")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    if (!r) r = consumeToken(b, DOUBLE_EQUAL_SIGN);
    return r;
  }

  /* ********************************************************** */
  // value
  static boolean option_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, null, "<option value>");
    r = value(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // property_key property_separator property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, "<property>", PROPERTY_KEY_TOKEN)) return false;
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
    if (!nextTokenIs(b, "<property key>", PROPERTY_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_KEY, "<property key>");
    r = property_key_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_TOKEN
  static boolean property_key_content(PsiBuilder b, int l) {
    return consumeToken(b, PROPERTY_KEY_TOKEN);
  }

  /* ********************************************************** */
  // EQUAL_SIGN | NOT_EQUAL_SIGN | DOUBLE_EQUAL_SIGN
  static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    if (!r) r = consumeToken(b, DOUBLE_EQUAL_SIGN);
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
  // general_comment | property | value
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = general_comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, l, m, r, false, CwtParser::root_block_item_recover);
    return r;
  }

  /* ********************************************************** */
  // !( COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
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

  // COMMENT | OPTION_COMMENT_START | DOC_COMMENT_TOKEN
  //   | BOOLEAN_TOKEN | INT_TOKEN | FLOAT_TOKEN | STRING_TOKEN | PROPERTY_KEY_TOKEN
  //   | LEFT_BRACE | RIGHT_BRACE
  private static boolean root_block_item_recover_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item_recover_0")) return false;
    boolean r;
    r = consumeToken(b, COMMENT);
    if (!r) r = consumeToken(b, OPTION_COMMENT_START);
    if (!r) r = consumeToken(b, DOC_COMMENT_TOKEN);
    if (!r) r = consumeToken(b, BOOLEAN_TOKEN);
    if (!r) r = consumeToken(b, INT_TOKEN);
    if (!r) r = consumeToken(b, FLOAT_TOKEN);
    if (!r) r = consumeToken(b, STRING_TOKEN);
    if (!r) r = consumeToken(b, PROPERTY_KEY_TOKEN);
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
  // string_content
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!nextTokenIs(b, "<string>", STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, STRING, "<string>");
    r = string_content(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN
  static boolean string_content(PsiBuilder b, int l) {
    return consumeToken(b, STRING_TOKEN);
  }

  /* ********************************************************** */
  // boolean | int | float | string | block
  public static boolean value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "value")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, VALUE, "<value>");
    r = boolean_$(b, l + 1);
    if (!r) r = int_$(b, l + 1);
    if (!r) r = float_$(b, l + 1);
    if (!r) r = string(b, l + 1);
    if (!r) r = block(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
