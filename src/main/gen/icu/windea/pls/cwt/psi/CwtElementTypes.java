// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.cwt.psi.impl.*;

public interface CwtElementTypes {

  IElementType BLOCK = CwtElementTypeFactory.getElementType("BLOCK");
  IElementType BOOLEAN = CwtElementTypeFactory.getElementType("BOOLEAN");
  IElementType DOC_COMMENT = CwtElementTypeFactory.getElementType("DOC_COMMENT");
  IElementType FLOAT = CwtElementTypeFactory.getElementType("FLOAT");
  IElementType INT = CwtElementTypeFactory.getElementType("INT");
  IElementType OPTION = CwtElementTypeFactory.getElementType("OPTION");
  IElementType OPTION_COMMENT = CwtElementTypeFactory.getElementType("OPTION_COMMENT");
  IElementType OPTION_COMMENT_ROOT = CwtElementTypeFactory.getElementType("OPTION_COMMENT_ROOT");
  IElementType OPTION_KEY = CwtElementTypeFactory.getElementType("OPTION_KEY");
  IElementType PROPERTY = CwtElementTypeFactory.getElementType("PROPERTY");
  IElementType PROPERTY_KEY = CwtElementTypeFactory.getElementType("PROPERTY_KEY");
  IElementType ROOT_BLOCK = CwtElementTypeFactory.getElementType("ROOT_BLOCK");
  IElementType STRING = CwtElementTypeFactory.getElementType("STRING");
  IElementType VALUE = CwtElementTypeFactory.getElementType("VALUE");

  IElementType BOOLEAN_TOKEN = CwtElementTypeFactory.getTokenType("BOOLEAN_TOKEN");
  IElementType COMMENT = CwtElementTypeFactory.getTokenType("COMMENT");
  IElementType DOC_COMMENT_TOKEN = CwtElementTypeFactory.getTokenType("DOC_COMMENT_TOKEN");
  IElementType EQUAL_SIGN = CwtElementTypeFactory.getTokenType("EQUAL_SIGN");
  IElementType FLOAT_TOKEN = CwtElementTypeFactory.getTokenType("FLOAT_TOKEN");
  IElementType INT_TOKEN = CwtElementTypeFactory.getTokenType("INT_TOKEN");
  IElementType LEFT_BRACE = CwtElementTypeFactory.getTokenType("LEFT_BRACE");
  IElementType NOT_EQUAL_SIGN = CwtElementTypeFactory.getTokenType("NOT_EQUAL_SIGN");
  IElementType OPTION_COMMENT_START = CwtElementTypeFactory.getTokenType("OPTION_COMMENT_START");
  IElementType OPTION_COMMENT_TOKEN = CwtElementTypeFactory.getTokenType("OPTION_COMMENT_TOKEN");
  IElementType OPTION_KEY_TOKEN = CwtElementTypeFactory.getTokenType("OPTION_KEY_TOKEN");
  IElementType PROPERTY_KEY_TOKEN = CwtElementTypeFactory.getTokenType("PROPERTY_KEY_TOKEN");
  IElementType RIGHT_BRACE = CwtElementTypeFactory.getTokenType("RIGHT_BRACE");
  IElementType STRING_TOKEN = CwtElementTypeFactory.getTokenType("STRING_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BLOCK) {
        return new CwtBlockImpl(node);
      }
      else if (type == BOOLEAN) {
        return new CwtBooleanImpl(node);
      }
      else if (type == DOC_COMMENT) {
        return new CwtDocCommentImpl(node);
      }
      else if (type == FLOAT) {
        return new CwtFloatImpl(node);
      }
      else if (type == INT) {
        return new CwtIntImpl(node);
      }
      else if (type == OPTION) {
        return new CwtOptionImpl(node);
      }
      else if (type == OPTION_COMMENT) {
        return new CwtOptionCommentImpl(node);
      }
      else if (type == OPTION_COMMENT_ROOT) {
        return new CwtOptionCommentRootImpl(node);
      }
      else if (type == OPTION_KEY) {
        return new CwtOptionKeyImpl(node);
      }
      else if (type == PROPERTY) {
        return new CwtPropertyImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new CwtPropertyKeyImpl(node);
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
