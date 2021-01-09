// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import com.windea.plugin.idea.paradox.script.psi.impl.*;

public interface ParadoxScriptTypes {

  IElementType BLOCK = new ParadoxScriptElementType("BLOCK");
  IElementType BOOLEAN = new ParadoxScriptElementType("BOOLEAN");
  IElementType CODE = new ParadoxScriptElementType("CODE");
  IElementType COLOR = new ParadoxScriptElementType("COLOR");
  IElementType NUMBER = new ParadoxScriptElementType("NUMBER");
  IElementType PROPERTY = ParadoxScriptStubElementTypes.getPropertyType("PROPERTY");
  IElementType PROPERTY_KEY = new ParadoxScriptElementType("PROPERTY_KEY");
  IElementType PROPERTY_VALUE = new ParadoxScriptElementType("PROPERTY_VALUE");
  IElementType ROOT_BLOCK = new ParadoxScriptElementType("ROOT_BLOCK");
  IElementType STRING = new ParadoxScriptElementType("STRING");
  IElementType STRING_VALUE = new ParadoxScriptElementType("STRING_VALUE");
  IElementType VALUE = new ParadoxScriptElementType("VALUE");
  IElementType VARIABLE = ParadoxScriptStubElementTypes.getVariableType("VARIABLE");
  IElementType VARIABLE_NAME = new ParadoxScriptElementType("VARIABLE_NAME");
  IElementType VARIABLE_REFERENCE = new ParadoxScriptElementType("VARIABLE_REFERENCE");
  IElementType VARIABLE_VALUE = new ParadoxScriptElementType("VARIABLE_VALUE");

  IElementType BOOLEAN_TOKEN = new ParadoxScriptTokenType("BOOLEAN_TOKEN");
  IElementType CODE_END = new ParadoxScriptTokenType("]");
  IElementType CODE_START = new ParadoxScriptTokenType("@\\[");
  IElementType CODE_TEXT_TOKEN = new ParadoxScriptTokenType("CODE_TEXT_TOKEN");
  IElementType COLOR_TOKEN = new ParadoxScriptTokenType("COLOR_TOKEN");
  IElementType COMMENT = new ParadoxScriptTokenType("COMMENT");
  IElementType END_OF_LINE_COMMENT = new ParadoxScriptTokenType("END_OF_LINE_COMMENT");
  IElementType EQUAL_SIGN = new ParadoxScriptTokenType("=");
  IElementType GE_SIGN = new ParadoxScriptTokenType(">=");
  IElementType GT_SIGN = new ParadoxScriptTokenType(">");
  IElementType LEFT_BRACE = new ParadoxScriptTokenType("{");
  IElementType LE_SIGN = new ParadoxScriptTokenType("<=");
  IElementType LT_SIGN = new ParadoxScriptTokenType("<");
  IElementType NOT_EQUAL_SIGN = new ParadoxScriptTokenType("<>");
  IElementType NUMBER_TOKEN = new ParadoxScriptTokenType("NUMBER_TOKEN");
  IElementType PROPERTY_KEY_ID = new ParadoxScriptTokenType("PROPERTY_KEY_ID");
  IElementType QUOTED_PROPERTY_KEY_ID = new ParadoxScriptTokenType("QUOTED_PROPERTY_KEY_ID");
  IElementType QUOTED_STRING_TOKEN = new ParadoxScriptTokenType("QUOTED_STRING_TOKEN");
  IElementType RIGHT_BRACE = new ParadoxScriptTokenType("}");
  IElementType RIGHT_QUOTE = new ParadoxScriptTokenType("\"");
  IElementType STRING_TOKEN = new ParadoxScriptTokenType("STRING_TOKEN");
  IElementType VARIABLE_NAME_ID = new ParadoxScriptTokenType("VARIABLE_NAME_ID");
  IElementType VARIABLE_REFERENCE_ID = new ParadoxScriptTokenType("VARIABLE_REFERENCE_ID");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == BLOCK) {
        return new ParadoxScriptBlockImpl(node);
      }
      else if (type == BOOLEAN) {
        return new ParadoxScriptBooleanImpl(node);
      }
      else if (type == CODE) {
        return new ParadoxScriptCodeImpl(node);
      }
      else if (type == COLOR) {
        return new ParadoxScriptColorImpl(node);
      }
      else if (type == NUMBER) {
        return new ParadoxScriptNumberImpl(node);
      }
      else if (type == PROPERTY) {
        return new ParadoxScriptPropertyImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new ParadoxScriptPropertyKeyImpl(node);
      }
      else if (type == PROPERTY_VALUE) {
        return new ParadoxScriptPropertyValueImpl(node);
      }
      else if (type == ROOT_BLOCK) {
        return new ParadoxScriptRootBlockImpl(node);
      }
      else if (type == STRING) {
        return new ParadoxScriptStringImpl(node);
      }
      else if (type == VARIABLE) {
        return new ParadoxScriptVariableImpl(node);
      }
      else if (type == VARIABLE_NAME) {
        return new ParadoxScriptVariableNameImpl(node);
      }
      else if (type == VARIABLE_REFERENCE) {
        return new ParadoxScriptVariableReferenceImpl(node);
      }
      else if (type == VARIABLE_VALUE) {
        return new ParadoxScriptVariableValueImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
