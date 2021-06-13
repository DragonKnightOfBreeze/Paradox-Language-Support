// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.cwt.psi.impl.*;

public interface CwtTypes {

  IElementType BLOCK = new CwtElementType("BLOCK");
  IElementType BOOLEAN = new CwtElementType("BOOLEAN");
  IElementType DOCUMENTATION_COMMENT = new CwtElementType("DOCUMENTATION_COMMENT");
  IElementType DOCUMENTATION_TEXT = new CwtElementType("DOCUMENTATION_TEXT");
  IElementType FLOAT = new CwtElementType("FLOAT");
  IElementType INT = new CwtElementType("INT");
  IElementType NUMBER = new CwtElementType("NUMBER");
  IElementType OPTION = new CwtElementType("OPTION");
  IElementType OPTION_COMMENT = new CwtElementType("OPTION_COMMENT");
  IElementType OPTION_KEY = new CwtElementType("OPTION_KEY");
  IElementType OPTION_SEPARATOR = new CwtElementType("OPTION_SEPARATOR");
  IElementType PROPERTY = new CwtElementType("PROPERTY");
  IElementType PROPERTY_KEY = new CwtElementType("PROPERTY_KEY");
  IElementType PROPERTY_SEPARATOR = new CwtElementType("PROPERTY_SEPARATOR");
  IElementType ROOT_BLOCK = new CwtElementType("ROOT_BLOCK");
  IElementType STRING = new CwtElementType("STRING");
  IElementType VALUE = new CwtElementType("VALUE");

  IElementType BOOLEAN_TOKEN = new CwtTokenType("BOOLEAN_TOKEN");
  IElementType COMMENT = new CwtTokenType("COMMENT");
  IElementType DOCUMENTATION_START = new CwtTokenType("###");
  IElementType DOCUMENTATION_TOKEN = new CwtTokenType("DOCUMENTATION_TOKEN");
  IElementType EQUAL_SIGN = new CwtTokenType("=");
  IElementType FLOAT_TOKEN = new CwtTokenType("FLOAT_TOKEN");
  IElementType INT_TOKEN = new CwtTokenType("INT_TOKEN");
  IElementType LEFT_BRACE = new CwtTokenType("{");
  IElementType NOT_EQUAL_SIGN = new CwtTokenType("<>");
  IElementType OPTION_KEY_TOKEN = new CwtTokenType("OPTION_KEY_TOKEN");
  IElementType OPTION_START = new CwtTokenType("##");
  IElementType PROPERTY_KEY_TOKEN = new CwtTokenType("PROPERTY_KEY_TOKEN");
  IElementType RIGHT_BRACE = new CwtTokenType("}");
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
      else if (type == DOCUMENTATION_COMMENT) {
        return new CwtDocumentationCommentImpl(node);
      }
      else if (type == DOCUMENTATION_TEXT) {
        return new CwtDocumentationTextImpl(node);
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
      else if (type == OPTION_KEY) {
        return new CwtOptionKeyImpl(node);
      }
      else if (type == OPTION_SEPARATOR) {
        return new CwtOptionSeparatorImpl(node);
      }
      else if (type == PROPERTY) {
        return new CwtPropertyImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new CwtPropertyKeyImpl(node);
      }
      else if (type == PROPERTY_SEPARATOR) {
        return new CwtPropertySeparatorImpl(node);
      }
      else if (type == ROOT_BLOCK) {
        return new CwtRootBlockImpl(node);
      }
      else if (type == STRING) {
        return new CwtStringImpl(node);
      }
      throw new AssertionError("Unknown element category: " + type);
    }
  }
}
