// This is a generated file. Not intended for manual editing.
package icu.windea.pls.gameTypes.stellaris;

import com.intellij.lang.PsiBuilder;
import com.intellij.lang.PsiBuilder.Marker;
import static icu.windea.pls.gameTypes.stellaris.StellarisFormatStringElementTypes.*;
import static com.intellij.lang.parser.GeneratedParserUtilBase.*;
import com.intellij.psi.tree.IElementType;
import com.intellij.lang.ASTNode;
import com.intellij.psi.tree.TokenSet;
import com.intellij.lang.PsiParser;
import com.intellij.lang.LightPsiParser;

@SuppressWarnings({"SimplifiableIfStatement", "UnusedAssignment"})
public class StellarisFormatStringParser implements PsiParser, LightPsiParser {

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
  // LEFT_ANGLE_BRACKET FORMAT_REFERENCE_TOKEN RIGHT_ANGLE_BRACKET
  public static boolean format_reference(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "format_reference")) return false;
    if (!nextTokenIs(b, LEFT_ANGLE_BRACKET)) return false;
    boolean r, p;
    Marker m = enter_section_(b, l, _NONE_, FORMAT_REFERENCE, null);
    r = consumeTokens(b, 1, LEFT_ANGLE_BRACKET, FORMAT_REFERENCE_TOKEN, RIGHT_ANGLE_BRACKET);
    p = r; // pin = 1
    exit_section_(b, l, m, r, p, null);
    return r || p;
  }

  /* ********************************************************** */
  // (STRING_TOKEN | format_reference)*
  static boolean root(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root")) return false;
    while (true) {
      int c = current_position_(b);
      if (!root_0(b, l + 1)) break;
      if (!empty_element_parsed_guard_(b, "root", c)) break;
    }
    return true;
  }

  // STRING_TOKEN | format_reference
  private static boolean root_0(PsiBuilder b, int l) {
    if (!recursion_guard_(b, l, "root_0")) return false;
    boolean r;
    r = consumeToken(b, STRING_TOKEN);
    if (!r) r = format_reference(b, l + 1);
    return r;
  }

}
