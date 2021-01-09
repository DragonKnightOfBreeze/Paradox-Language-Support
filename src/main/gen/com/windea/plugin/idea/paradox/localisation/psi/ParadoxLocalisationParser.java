// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;
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
    create_token_set_(COLORFUL_TEXT, COMMAND, ESCAPE, ICON,
      PROPERTY_REFERENCE, RICH_TEXT, SERIAL_NUMBER, STRING),
  };

  /* ********************************************************** */
  // COLORFUL_TEXT_START COLOR_CODE rich_text* [COLORFUL_TEXT_END]
  public static boolean colorful_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text")) return false;
    if (!nextTokenIs(b, COLORFUL_TEXT_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, COLORFUL_TEXT, null);
    r = consumeTokens(b, 1, COLORFUL_TEXT_START, COLOR_CODE);
    p = r; // pin = 1
    r = r && report_error_(b, colorful_text_2(b, l + 1));
    r = p && colorful_text_3(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // rich_text*
  private static boolean colorful_text_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rich_text(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "colorful_text_2", c)) break;
    }
    return true;
  }

  // [COLORFUL_TEXT_END]
  private static boolean colorful_text_3(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "colorful_text_3")) return false;
    consumeToken(b, COLORFUL_TEXT_END);
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
  // COMMAND_KEY_TOKEN
  public static boolean command_Key(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_Key")) return false;
    if (!nextTokenIs(b, COMMAND_KEY_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMAND_KEY_TOKEN);
    exit_section_(b, m, COMMAND_KEY, r);
    return r;
  }

  /* ********************************************************** */
  // command_Key (COMMAND_KEY_SEPARATOR command_Key)*
  static boolean command_expression(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression")) return false;
    if (!nextTokenIs(b, COMMAND_KEY_TOKEN)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = command_Key(b, l + 1);
    p = r; // pin = 1
    r = r && command_expression_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // (COMMAND_KEY_SEPARATOR command_Key)*
  private static boolean command_expression_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!command_expression_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "command_expression_1", c)) break;
    }
    return true;
  }

  // COMMAND_KEY_SEPARATOR command_Key
  private static boolean command_expression_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "command_expression_1_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, COMMAND_KEY_SEPARATOR);
    p = r; // pin = 1
    r = r && command_Key(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // VALID_ESCAPE_TOKEN | INVALID_ESCAPE_TOKEN
  public static boolean escape(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "escape")) return false;
    if (!nextTokenIs(b, "<escape>", INVALID_ESCAPE_TOKEN, VALID_ESCAPE_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ESCAPE, "<escape>");
    r = consumeToken(b, VALID_ESCAPE_TOKEN);
    if (!r) r = consumeToken(b, INVALID_ESCAPE_TOKEN);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ICON_START icon_name [PARAMETER_SEPARATOR [icon_param]] ICON_END
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

  // [PARAMETER_SEPARATOR [icon_param]]
  private static boolean icon_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_2")) return false;
    icon_2_0(b, l + 1);
    return true;
  }

  // PARAMETER_SEPARATOR [icon_param]
  private static boolean icon_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_2_0")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = consumeToken(b, PARAMETER_SEPARATOR);
    p = r; // pin = 1
    r = r && icon_2_0_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [icon_param]
  private static boolean icon_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_2_0_1")) return false;
    icon_param(b, l + 1);
    return true;
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
  // ICON_PARAMETER | property_reference
  static boolean icon_param(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "icon_param")) return false;
    if (!nextTokenIs(b, "", ICON_PARAMETER, PROPERTY_REFERENCE_START)) return false;
    boolean r;
    r = consumeToken(b, ICON_PARAMETER);
    if (!r) r = property_reference(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // LOCALE_ID ":"
  public static boolean locale(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "locale")) return false;
    if (!nextTokenIs(b, LOCALE_ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, LOCALE, null);
    r = consumeTokens(b, 1, LOCALE_ID, COLON);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // property_key ":" [NUMBER] property_value
  public static boolean property(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property")) return false;
    if (!nextTokenIs(b, PROPERTY_KEY_ID)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY, null);
    r = property_key(b, l + 1);
    p = r; // pin = 1
    r = r && report_error_(b, consumeToken(b, COLON));
    r = p && report_error_(b, property_2(b, l + 1)) && r;
    r = p && property_value(b, l + 1) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // [NUMBER]
  private static boolean property_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_2")) return false;
    consumeToken(b, NUMBER);
    return true;
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
  // END_OF_LINE_COMMENT | COMMENT | property
  static boolean property_list(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_list")) return false;
    boolean r;
    r = consumeToken(b, END_OF_LINE_COMMENT);
    if (!r) r = consumeToken(b, COMMENT);
    if (!r) r = property(b, l + 1);
    return r;
  }

  /* ********************************************************** */
  // PROPERTY_REFERENCE_START [property_reference_name] [PARAMETER_SEPARATOR [property_reference_param]] PROPERTY_REFERENCE_END
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

  // [PARAMETER_SEPARATOR [property_reference_param]]
  private static boolean property_reference_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2")) return false;
    property_reference_2_0(b, l + 1);
    return true;
  }

  // PARAMETER_SEPARATOR [property_reference_param]
  private static boolean property_reference_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, PARAMETER_SEPARATOR);
    r = r && property_reference_2_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [property_reference_param]
  private static boolean property_reference_2_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_reference_2_0_1")) return false;
    property_reference_param(b, l + 1);
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
  // PROPERTY_REFERENCE_PARAMETER
  static boolean property_reference_param(PsiBuilder b, int l) {
    return consumeToken(b, PROPERTY_REFERENCE_PARAMETER);
  }

  /* ********************************************************** */
  // LEFT_QUOTE rich_text * RIGHT_QUOTE
  public static boolean property_value(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value")) return false;
    if (!nextTokenIs(b, LEFT_QUOTE)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, PROPERTY_VALUE, null);
    r = consumeToken(b, LEFT_QUOTE);
    p = r; // pin = 1
    r = r && report_error_(b, property_value_1(b, l + 1));
    r = p && consumeToken(b, RIGHT_QUOTE) && r;
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // rich_text *
  private static boolean property_value_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "property_value_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!rich_text(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "property_value_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // property_reference | command | icon | serial_number | colorful_text | escape | string
  public static boolean rich_text(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "rich_text")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _COLLAPSE_, RICH_TEXT, "<rich text>");
    r = property_reference(b, l + 1);
    if (!r) r = command(b, l + 1);
    if (!r) r = icon(b, l + 1);
    if (!r) r = serial_number(b, l + 1);
    if (!r) r = colorful_text(b, l + 1);
    if (!r) r = escape(b, l + 1);
    if (!r) r = string(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // ROOT_COMMENT * [root_item]
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_0(b, l + 1);
    r = r && root_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // ROOT_COMMENT *
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, ROOT_COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "root_0", c)) break;
    }
    return true;
  }

  // [root_item]
  private static boolean root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1")) return false;
    root_item(b, l + 1);
    return true;
  }

  /* ********************************************************** */
  // [locale] property_list *
  static boolean root_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_item_0(b, l + 1);
    r = r && root_item_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [locale]
  private static boolean root_item_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_item_0")) return false;
    locale(b, l + 1);
    return true;
  }

  // property_list *
  private static boolean root_item_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_item_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!property_list(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_item_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // SERIAL_NUMBER_START SERIAL_NUMBER_ID SERIAL_NUMBER_END
  public static boolean serial_number(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "serial_number")) return false;
    if (!nextTokenIs(b, SERIAL_NUMBER_START)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, SERIAL_NUMBER, null);
    r = consumeTokens(b, 1, SERIAL_NUMBER_START, SERIAL_NUMBER_ID, SERIAL_NUMBER_END);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
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

}
