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
  // COLUMN_TOKEN
  public static boolean column(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "column")) return false;
    if (!nextTokenIs(b, COLUMN_TOKEN)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COLUMN_TOKEN);
    exit_section_(b, m, COLUMN, r);
    return r;
  }

  /* ********************************************************** */
  // COMMENT | row
  static boolean line_item(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "line_item")) return false;
    if (!nextTokenIs(b, "", COLUMN_TOKEN, COMMENT)) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, COMMENT);
    if (!r) r = row(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  /* ********************************************************** */
  // line_item *
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!line_item(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
  }

  /* ********************************************************** */
  // column (SEPARATOR column ?) *
  public static boolean row(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row")) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, ROW, "<row>");
    r = column(b, l + 1);
    p = r; // pin = 1
    r = r && row_1(b, l + 1);
    exit_section_(b, l, m, r, p, row_auto_recover_);
    return r || p;
  }

  // (SEPARATOR column ?) *
  private static boolean row_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row_1")) return false;
    while (true) {
      int c = current_position_(b);
      if (!row_1_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "row_1", c)) break;
    }
    return true;
  }

  // SEPARATOR column ?
  private static boolean row_1_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row_1_0")) return false;
    boolean r;
    Marker m = enter_section_(b);
    r = consumeToken(b, SEPARATOR);
    r = r && row_1_0_1(b, l + 1);
    exit_section_(b, m, null, r);
    return r;
  }

  // column ?
  private static boolean row_1_0_1(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "row_1_0_1")) return false;
    column(b, l + 1);
    return true;
  }

  static final Parser row_auto_recover_ = (b, l) -> !nextTokenIsFast(b, COLUMN_TOKEN, COMMENT);
}
