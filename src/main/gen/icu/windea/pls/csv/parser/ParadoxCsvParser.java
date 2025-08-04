// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.parser;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;
import static icu.windea.pls.csv.parser.ParadoxCsvParserUtil.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;
import static com.intellij.lang.WhitespacesBinders.*;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class ParadoxCsvParser implements PsiParser, LightPsiParser {

  public ASTNode parse(IElementType t, PsiBuilder b) {
    parseLight(t, b);
    return b.getTreeBuilt();
  }

  public void parseLight(IElementType t, PsiBuilder b) {
    boolean r;
    b = adapt_builder_(t, b, this, null);
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

  /* ********************************************************** */
  // <<checkColumnToken>> COLUMN_TOKEN ?
  public static boolean column(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, COLUMN, "<column>");
    r = checkColumnToken(b, l + 1);
    r = r && column_1(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  // COLUMN_TOKEN ?
  private static boolean column_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_1")) return false;
    consumeToken(b, COLUMN_TOKEN);
    return true;
  }

  /* ********************************************************** */
  // <<checkEol>> SEPARATOR [<<checkEol>> column]
  static boolean column_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_item")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = checkEol(b, l + 1);
    r = r && consumeToken(b, SEPARATOR);
    r = r && column_item_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // [<<checkEol>> column]
  private static boolean column_item_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_item_2")) return false;
    column_item_2_0(b, l + 1);
    return true;
  }

  // <<checkEol>> column
  private static boolean column_item_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_item_2_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = checkEol(b, l + 1);
    r = r && column(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // column column_item *
  static boolean column_items(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_items")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_);
    r = column(b, l + 1);
    p = r; // pin = 1
    r = r && column_items_1(b, l + 1);
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  // column_item *
  private static boolean column_items_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column_items_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!column_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "column_items_1", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // column_items
  public static boolean header(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "header")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, HEADER, "<header>");
    r = column_items(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

  /* ********************************************************** */
  // COMMENT * header ? (row | COMMENT) *
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = root_0(b, l + 1);
    r = r && root_1(b, l + 1);
    r = r && root_2(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // COMMENT *
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    while (true) {
      int c = current_position_(b);
      if (!consumeToken(b, COMMENT)) break;
      if (!empty_element_parsed_guard_(b, "root_0", c)) break;
    }
    return true;
  }

  // header ?
  private static boolean root_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_1")) return false;
    header(b, l + 1);
    return true;
  }

  // (row | COMMENT) *
  private static boolean root_2(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_2")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_2_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root_2", c)) break;
    }
    return true;
  }

  // row | COMMENT
  private static boolean root_2_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_2_0")) return false;
    boolean r;
    r = row(b, l + 1);
    if (!r) r = consumeToken(b, COMMENT);
    return r;
  }

  /* ********************************************************** */
  // column_items
  public static boolean row(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row")) return false;
    boolean r;
    Marker m = enter_section_(b, l, _NONE_, ROW, "<row>");
    r = column_items(b, l + 1);
    exit_section_(b, l, m, r, false, null);
    return r;
  }

}
