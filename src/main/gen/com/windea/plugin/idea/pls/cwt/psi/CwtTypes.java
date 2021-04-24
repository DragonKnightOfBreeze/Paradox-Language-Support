// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.windea.plugin.idea.pls.cwt.psi.impl.*;

public interface CwtTypes {

  IElementType BLOCK = new CwtElementType("BLOCK");
  IElementType BOOLEAN = new CwtElementType("BOOLEAN");
  IElementType FLOAT = new CwtElementType("FLOAT");
  IElementType INT = new CwtElementType("INT");
  IElementType KEY = new CwtElementType("KEY");
  IElementType NUMBER = new CwtElementType("NUMBER");
  IElementType PROPERTY = new CwtElementType("PROPERTY");
  IElementType ROOT_BLOCK = new CwtElementType("ROOT_BLOCK");
  IElementType STRING = new CwtElementType("STRING");
  IElementType VALUE = new CwtElementType("VALUE");

  IElementType BOOLEAN_TOKEN = new CwtTokenType("BOOLEAN_TOKEN");
  IElementType COMMENT = new CwtTokenType("COMMENT");
  IElementType DOCUMENTATION_COMMENT = new CwtTokenType("DOCUMENTATION_COMMENT");
  IElementType EQUAL_SIGN = new CwtTokenType("=");
  IElementType FLOAT_TOKEN = new CwtTokenType("FLOAT_TOKEN");
  IElementType INT_TOKEN = new CwtTokenType("INT_TOKEN");
  IElementType KEY_TOKEN = new CwtTokenType("KEY_TOKEN");
  IElementType LEFT_BRACE = new CwtTokenType("{");
  IElementType LEFT_QUOTE = new CwtTokenType("LEFT_QUOTE");
  IElementType NOT_EQUAL_SIGN = new CwtTokenType("!=");
  IElementType OPTION_COMMENT = new CwtTokenType("OPTION_COMMENT");
  IElementType QUOTED_KEY_TOKEN = new CwtTokenType("QUOTED_KEY_TOKEN");
  IElementType QUOTED_STRING_TOKEN = new CwtTokenType("QUOTED_STRING_TOKEN");
  IElementType RIGHT_BRACE = new CwtTokenType("}");
  IElementType RIGHT_QUOTE = new CwtTokenType("\"");
  IElementType STRING_TOKEN = new CwtTokenType("STRING_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BLOCK) {
        return new CwtBlockImpl(node);
      }
      else if (type == BOOLEAN) {
        return new CwtBooleanImpl(node);
      }
      else if (type == FLOAT) {
        return new CwtFloatImpl(node);
      }
      else if (type == INT) {
        return new CwtIntImpl(node);
      }
      else if (type == KEY) {
        return new CwtKeyImpl(node);
      }
      else if (type == PROPERTY) {
        return new CwtPropertyImpl(node);
      }
      else if (type == ROOT_BLOCK) {
        return new CwtRootBlockImpl(node);
      }
      else if (type == STRING) {
        return new CwtStringImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
