// This is a generated file. Not intended for manual editing.
package icu.windea.pls.gameTypes.stellaris;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.gameTypes.stellaris.psi.StellarisFormatStringElementType;
import icu.windea.pls.gameTypes.stellaris.psi.StellarisFormatStringTokenType;
import icu.windea.pls.gameTypes.stellaris.psi.impl.*;

public interface StellarisFormatStringElementTypes {

  IElementType FORMAT_REFERENCE = new StellarisFormatStringElementType("FORMAT_REFERENCE");

  IElementType FORMAT_REFERENCE_TOKEN = new StellarisFormatStringTokenType("FORMAT_REFERENCE_TOKEN");
  IElementType LEFT_ANGLE_BRACKET = new StellarisFormatStringTokenType("<");
  IElementType RIGHT_ANGLE_BRACKET = new StellarisFormatStringTokenType(">");
  IElementType STRING_TOKEN = new StellarisFormatStringTokenType("STRING_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == FORMAT_REFERENCE) {
        return new StellarisFormatReferenceImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
