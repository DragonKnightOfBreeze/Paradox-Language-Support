// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.expression.psi.impl.*;

public interface ParadoxExpressionElementTypes {

  IElementType STRING = ParadoxExpressionElementTypeFactory.getElementType("STRING");

  IElementType EXPRESSION_PREFIX = ParadoxExpressionElementTypeFactory.getTokenType("EXPRESSION_PREFIX");
  IElementType EXPRESSION_TOKEN = ParadoxExpressionElementTypeFactory.getTokenType("EXPRESSION_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == STRING) {
        return new ParadoxExpressionStringImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
