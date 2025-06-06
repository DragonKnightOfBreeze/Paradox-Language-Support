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
    boolean r;
    if (t == OPTION_COMMENT_ROOT) {
      r = option_comment_root(b, l + 1);
    }
    else {
      r = root(b, l + 1);
    }
    return r;
  }

  public static final TokenSet[] EXTENDS_SETS_ = new TokenSet[] {
    create_token_set_(BLOCK, BOOLEAN, FLOAT, INT,
      STRING, VALUE),
  };

  /* ********************************************************** */
  // LEFT_BRACE block_item * RIGHT_BRACE
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
  // general_comment | property | option | value
  static boolean block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = general_comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = option(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, l, m, r, false, block_item_auto_recover_);
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
  // COMMENT
  static boolean comment(PsiBuilder b, int l) {
    return consumeToken(b, COMMENT);
  }

  /* ********************************************************** */
  // DOC_COMMENT_TOKEN
  public static boolean doc_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "doc_comment")) return false;
    if (!nextTokenIs(b, DOC_COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, DOC_COMMENT_TOKEN);
    exit_section_(b, m, DOC_COMMENT, r);
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
    if (!nextTokenIs(b, INT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, INT_TOKEN);
    exit_section_(b, m, INT, r);
    return r;
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
  // OPTION_COMMENT_TOKEN
  public static boolean option_comment(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment")) return false;
    if (!nextTokenIs(b, OPTION_COMMENT_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPTION_COMMENT_TOKEN);
    exit_section_(b, m, OPTION_COMMENT, r);
    return r;
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
  // OPTION_COMMENT_START option_comment_item ? comment ?
  public static boolean option_comment_root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_root")) return false;
    if (!nextTokenIs(b, OPTION_COMMENT_START)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, OPTION_COMMENT_START);
    r = r && option_comment_root_1(b, l + 1);
    r = r && option_comment_root_2(b, l + 1);
    exit_section_(b, m, OPTION_COMMENT_ROOT, r);
    return r;
  }

  // option_comment_item ?
  private static boolean option_comment_root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_root_1")) return false;
    option_comment_item(b, l + 1);
    return true;
  }

  // comment ?
  private static boolean option_comment_root_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "option_comment_root_2")) return false;
    comment(b, l + 1);
    return true;
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
  // value
  static boolean option_value(PsiBuilder b, int l) {
    return value(b, l + 1);
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
  // PROPERTY_KEY_TOKEN
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    if (!nextTokenIs(b, PROPERTY_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PROPERTY_KEY_TOKEN);
    exit_section_(b, m, PROPERTY_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // EQUAL_SIGN | NOT_EQUAL_SIGN
  static boolean property_separator(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_separator")) return false;
    if (!nextTokenIs(b, "", EQUAL_SIGN, NOT_EQUAL_SIGN)) return false;
    boolean r;
    r = consumeToken(b, EQUAL_SIGN);
    if (!r) r = consumeToken(b, NOT_EQUAL_SIGN);
    return r;
  }

  /* ********************************************************** */
  // value
  static boolean property_value(PsiBuilder b, int l) {
    return value(b, l + 1);
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
  // general_comment | property | value
  static boolean root_block_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_block_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = general_comment(b, l + 1);
    if (!r) r = property(b, l + 1);
    if (!r) r = value(b, l + 1);
    exit_section_(b, l, m, r, false, root_block_item_auto_recover_);
    return r;
  }

  /* ********************************************************** */
  // STRING_TOKEN
  public static boolean string(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "string")) return false;
    if (!nextTokenIs(b, STRING_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, STRING_TOKEN);
    exit_section_(b, m, STRING, r);
    return r;
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

  static final Parser block_item_auto_recover_ = (b, l) -> !nextTokenIsFast(b, BOOLEAN_TOKEN, COMMENT,
    DOC_COMMENT_TOKEN, FLOAT_TOKEN, INT_TOKEN, LEFT_BRACE, OPTION_COMMENT_TOKEN, OPTION_KEY_TOKEN,
    PROPERTY_KEY_TOKEN, RIGHT_BRACE, STRING_TOKEN);
  static final Parser property_auto_recover_ = block_item_auto_recover_;
  static final Parser root_block_item_auto_recover_ = (b, l) -> !nextTokenIsFast(b, BOOLEAN_TOKEN, COMMENT,
    DOC_COMMENT_TOKEN, FLOAT_TOKEN, INT_TOKEN, LEFT_BRACE, OPTION_COMMENT_TOKEN, PROPERTY_KEY_TOKEN, STRING_TOKEN);
}
