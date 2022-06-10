// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ParadoxLocalisationParser implements PsiParser, LightPsiParser {

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
    create_token_set_(COMMAND_FIELD, COMMAND_IDENTIFIER, COMMAND_SCOPE),
    create_token_set_(COLORFUL_TEXT, COMMAND, ESCAPE, ICON,
      PROPERTY_REFERENCE, RICH_TEXT, STRING),
  };

  /* ********************************************************** */
  // COLORFUL_TEXT_START COLOR_ID colorful_text_item [COLORFUL_TEXT_END]
  public static boolean colorful_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text")) return false;
    if (!nextTokenIs(b, COLORFUL_TEXT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COLORFUL_TEXT, null);
    r = consumeTokens(b, 1, COLORFUL_TEXT_START, COLOR_ID);
    p = r; // pin = 1
    r = r && report_error_(b, colorful_text_item(b, l + 1));
    r = p && colorful_text_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [COLORFUL_TEXT_END]
  private static boolean colorful_text_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text_3")) return false;
    consumeToken(b, COLORFUL_TEXT_END);
    return true;
  }

  /* ********************************************************** */
  // rich_text *
  static boolean colorful_text_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text_item")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rich_text(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "colorful_text_item", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // COMMAND_START command_expression? COMMAND_END
  public static boolean command(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command")) return false;
    if (!nextTokenIs(b, COMMAND_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COMMAND, null);
    r = consumeToken(b, COMMAND_START);
    p = r; // pin = 1
    r = r && report_error_(b, command_1(b, l + 1));
    r = p && consumeToken(b, COMMAND_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // command_expression?
  private static boolean command_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_1")) return false;
    command_expression(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // (command_scope DOT)* command_field
  static boolean command_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = command_expression_0(b, l + 1);
    p = r; // pin = 1
    r = r && command_field(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (command_scope DOT)*
  private static boolean command_expression_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!command_expression_0_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "command_expression_0", c)) break;
    }
    return true;
  }

  // command_scope DOT
  private static boolean command_expression_0_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression_0_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = command_scope(b, l + 1);
    r = r && consumeToken(b, DOT);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // COMMAND_FIELD_ID | property_reference
  public static boolean command_field(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_field")) return false;
    if (!nextTokenIs(b, "<command field>", COMMAND_FIELD_ID, PROPERTY_REFERENCE_START)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COMMAND_FIELD, "<command field>");
    r = consumeToken(b, COMMAND_FIELD_ID);
    if (!r) r = property_reference(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  public static boolean command_identifier(PsiBuilder b, int l) {
    Marker m = enter_section_(b);
    exit_section_(b, m, COMMAND_IDENTIFIER, true);
    return true;
  }

  /* ********************************************************** */
  // COMMAND_SCOPE_ID
  public static boolean command_scope(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_scope")) return false;
    if (!nextTokenIs(b, COMMAND_SCOPE_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMAND_SCOPE_ID);
    exit_section_(b, m, COMMAND_SCOPE, r);
    return r;
  }

  /* ********************************************************** */
  // VALID_ESCAPE_TOKEN | INVALID_ESCAPE_TOKEN | DOUBLE_LEFT_BRACKET
  public static boolean escape(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "escape")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ESCAPE, "<escape>");
    r = consumeToken(b, VALID_ESCAPE_TOKEN);
    if (!r) r = consumeToken(b, INVALID_ESCAPE_TOKEN);
    if (!r) r = consumeToken(b, DOUBLE_LEFT_BRACKET);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ICON_START icon_name [PIPE icon_frame] ICON_END
  public static boolean icon(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon")) return false;
    if (!nextTokenIs(b, ICON_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ICON, null);
    r = consumeToken(b, ICON_START);
    p = r; // pin = 1
    r = r && report_error_(b, icon_name(b, l + 1));
    r = p && report_error_(b, icon_2(b, l + 1)) && r;
    r = p && consumeToken(b, ICON_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [PIPE icon_frame]
  private static boolean icon_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_2")) return false;
    icon_2_0(b, l + 1);
    return true;
  }

  // PIPE icon_frame
  private static boolean icon_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && icon_frame(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // ICON_FRAME | property_reference
  static boolean icon_frame(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_frame")) return false;
    if (!nextTokenIs(b, "", ICON_FRAME, PROPERTY_REFERENCE_START)) return false;
    boolean r;
    r = consumeToken(b, ICON_FRAME);
    if (!r) r = property_reference(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // command | property_reference | ICON_ID
  static boolean icon_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_name")) return false;
    boolean r;
    r = command(b, l + 1);
    if (!r) r = property_reference(b, l + 1);
    if (!r) r = consumeToken(b, ICON_ID);
    return r;
  }

  /* ********************************************************** */
  // LOCALE_ID COLON
  public static boolean locale(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "locale")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCALE, "<locale>");
    r = consumeTokens(b, 1, LOCALE_ID, COLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, locale_auto_recover_);
    return r || p;
  }

  /* ********************************************************** */
  // property_key COLON property_number? property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, "<property>");
    r = property_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && report_error_(b, property_2(b, l + 1)) && r;
    r = p && property_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, property_auto_recover_);
    return r || p;
  }

  // property_number?
  private static boolean property_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_2")) return false;
    property_number(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // COMMENT | property
  static boolean property_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_item")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    exit_section_(b, l, m, r, false, property_item_auto_recover_);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_KEY_ID
  public static boolean property_key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_key")) return false;
    if (!nextTokenIs(b, PROPERTY_KEY_ID)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PROPERTY_KEY_ID);
    exit_section_(b, m, PROPERTY_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // COMMENT * locale property_item *
  public static boolean property_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_list")) return false;
    if (!nextTokenIs(b, "<property list>", COMMENT, LOCALE_ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_LIST, "<property list>");
    r = property_list_0(b, l + 1);
    r = r && locale(b, l + 1);
    p = r; // pin = 2
    r = r && property_list_2(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // COMMENT *
  private static boolean property_list_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_list_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "property_list_0", c)) break;
    }
    return true;
  }

  // property_item *
  private static boolean property_list_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_list_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_list_2", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // PROPERTY_NUMBER
  static boolean property_number(PsiBuilder b, int l) {
    return consumeToken(b, PROPERTY_NUMBER);
  }

  /* ********************************************************** */
  // PROPERTY_REFERENCE_START [property_reference_name] [PIPE [property_reference_parameter]] PROPERTY_REFERENCE_END
  public static boolean property_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference")) return false;
    if (!nextTokenIs(b, PROPERTY_REFERENCE_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_REFERENCE, null);
    r = consumeToken(b, PROPERTY_REFERENCE_START);
    p = r; // pin = 1
    r = r && report_error_(b, property_reference_1(b, l + 1));
    r = p && report_error_(b, property_reference_2(b, l + 1)) && r;
    r = p && consumeToken(b, PROPERTY_REFERENCE_END) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [property_reference_name]
  private static boolean property_reference_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_1")) return false;
    property_reference_name(b, l + 1);
    return true;
  }

  // [PIPE [property_reference_parameter]]
  private static boolean property_reference_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2")) return false;
    property_reference_2_0(b, l + 1);
    return true;
  }

  // PIPE [property_reference_parameter]
  private static boolean property_reference_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PIPE);
    r = r && property_reference_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [property_reference_parameter]
  private static boolean property_reference_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2_0_1")) return false;
    property_reference_parameter(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // command | PROPERTY_REFERENCE_ID
  static boolean property_reference_name(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_name")) return false;
    if (!nextTokenIs(b, "", COMMAND_START, PROPERTY_REFERENCE_ID)) return false;
    boolean r;
    r = command(b, l + 1);
    if (!r) r = consumeToken(b, PROPERTY_REFERENCE_ID);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_REFERENCE_PARAMETER_TOKEN
  static boolean property_reference_parameter(PsiBuilder b, int l) {
    return consumeToken(b, PROPERTY_REFERENCE_PARAMETER_TOKEN);
  }

  /* ********************************************************** */
  // LEFT_QUOTE property_value_item RIGHT_QUOTE
  public static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    if (!nextTokenIs(b, LEFT_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_VALUE, null);
    r = consumeToken(b, LEFT_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, property_value_item(b, l + 1));
    r = p && consumeToken(b, RIGHT_QUOTE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (rich_text | COLORFUL_TEXT_END) *
  static boolean property_value_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value_item")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_value_item_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_value_item", c)) break;
    }
    return true;
  }

  // rich_text | COLORFUL_TEXT_END
  private static boolean property_value_item_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value_item_0")) return false;
    boolean r;
    r = rich_text(b, l + 1);
    if (!r) r = consumeToken(b, COLORFUL_TEXT_END);
    return r;
  }

  /* ********************************************************** */
  // property_reference | command | icon | colorful_text | escape | string
  public static boolean rich_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rich_text")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, RICH_TEXT, "<rich text>");
    r = property_reference(b, l + 1);
    if (!r) r = command(b, l + 1);
    if (!r) r = icon(b, l + 1);
    if (!r) r = colorful_text(b, l + 1);
    if (!r) r = escape(b, l + 1);
    if (!r) r = string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // property_list *
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_list(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
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

  static final Parser locale_auto_recover_ = (b, l) -> !nextTokenIsFast(b, COMMENT, LOCALE_ID, PROPERTY_KEY_ID);
  static final Parser property_auto_recover_ = locale_auto_recover_;
  static final Parser property_item_auto_recover_ = locale_auto_recover_;
}
