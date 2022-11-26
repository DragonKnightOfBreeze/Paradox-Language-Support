// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.expression.psi.impl.*;

public interface ParadoxExpressionElementTypes {

  IElementType DUMMY_SCOPE_FIELD = new ParadoxExpressionElementType("DUMMY_SCOPE_FIELD");
  IElementType SCOPE_FIELD = new ParadoxExpressionElementType("SCOPE_FIELD");
  IElementType SCOPE_FIELD_EXPRESSION = new ParadoxExpressionElementType("SCOPE_FIELD_EXPRESSION");
  IElementType SCOPE_LINK = new ParadoxExpressionElementType("SCOPE_LINK");
  IElementType SCOPE_LINK_FROM_DATA = new ParadoxExpressionElementType("SCOPE_LINK_FROM_DATA");
  IElementType SYSTEM_SCOPE = new ParadoxExpressionElementType("SYSTEM_SCOPE");

  IElementType AT = new ParadoxExpressionTokenType("@");
  IElementType BOOLEAN_TOKEN = new ParadoxExpressionTokenType("boolean_token");
  IElementType DOT = new ParadoxExpressionTokenType(".");
  IElementType FLOAT_TOKEN = new ParadoxExpressionTokenType("float_token");
  IElementType IDENTIFIER_TOKEN = new ParadoxExpressionTokenType("identifier_token");
  IElementType INT_TOKEN = new ParadoxExpressionTokenType("int_token");
  IElementType PIPE = new ParadoxExpressionTokenType("|");
  IElementType PREFIX_TOKEN = new ParadoxExpressionTokenType("prefix_token");
  IElementType STRING_TOKEN = new ParadoxExpressionTokenType("string_token");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == DUMMY_SCOPE_FIELD) {
        return new ParadoxDummyScopeFieldImpl(node);
      }
      else if (type == SCOPE_FIELD_EXPRESSION) {
        return new ParadoxScopeFieldExpressionImpl(node);
      }
      else if (type == SCOPE_LINK) {
        return new ParadoxScopeLinkImpl(node);
      }
      else if (type == SCOPE_LINK_FROM_DATA) {
        return new ParadoxScopeLinkFromDataImpl(node);
      }
      else if (type == SYSTEM_SCOPE) {
        return new ParadoxSystemScopeImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
