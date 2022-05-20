// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.script.psi.impl.*;

public interface ParadoxScriptElementTypes {

  IElementType BLOCK = ParadoxScriptElementTypeFactory.getElementType("BLOCK");
  IElementType BOOLEAN = ParadoxScriptElementTypeFactory.getElementType("BOOLEAN");
  IElementType CODE = ParadoxScriptElementTypeFactory.getElementType("CODE");
  IElementType COLOR = ParadoxScriptElementTypeFactory.getElementType("COLOR");
  IElementType FLOAT = ParadoxScriptElementTypeFactory.getElementType("FLOAT");
  IElementType INT = ParadoxScriptElementTypeFactory.getElementType("INT");
  IElementType NUMBER = ParadoxScriptElementTypeFactory.getElementType("NUMBER");
  IElementType PROPERTY = ParadoxScriptElementTypeFactory.getElementType("PROPERTY");
  IElementType PROPERTY_KEY = ParadoxScriptElementTypeFactory.getElementType("PROPERTY_KEY");
  IElementType PROPERTY_VALUE = ParadoxScriptElementTypeFactory.getElementType("PROPERTY_VALUE");
  IElementType ROOT_BLOCK = ParadoxScriptElementTypeFactory.getElementType("ROOT_BLOCK");
  IElementType STRING = ParadoxScriptElementTypeFactory.getElementType("STRING");
  IElementType TAG = ParadoxScriptElementTypeFactory.getElementType("TAG");
  IElementType VALUE = ParadoxScriptElementTypeFactory.getElementType("VALUE");
  IElementType VARIABLE = ParadoxScriptElementTypeFactory.getElementType("VARIABLE");
  IElementType VARIABLE_NAME = ParadoxScriptElementTypeFactory.getElementType("VARIABLE_NAME");
  IElementType VARIABLE_REFERENCE = ParadoxScriptElementTypeFactory.getElementType("VARIABLE_REFERENCE");
  IElementType VARIABLE_VALUE = ParadoxScriptElementTypeFactory.getElementType("VARIABLE_VALUE");

  IElementType BOOLEAN_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("BOOLEAN_TOKEN");
  IElementType CODE_END = ParadoxScriptElementTypeFactory.getTokenType("CODE_END");
  IElementType CODE_START = ParadoxScriptElementTypeFactory.getTokenType("CODE_START");
  IElementType CODE_TEXT_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("CODE_TEXT_TOKEN");
  IElementType COLOR_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("COLOR_TOKEN");
  IElementType COMMENT = ParadoxScriptElementTypeFactory.getTokenType("COMMENT");
  IElementType END_OF_LINE_COMMENT = ParadoxScriptElementTypeFactory.getTokenType("END_OF_LINE_COMMENT");
  IElementType EQUAL_SIGN = ParadoxScriptElementTypeFactory.getTokenType("EQUAL_SIGN");
  IElementType FLOAT_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("FLOAT_TOKEN");
  IElementType GE_SIGN = ParadoxScriptElementTypeFactory.getTokenType("GE_SIGN");
  IElementType GT_SIGN = ParadoxScriptElementTypeFactory.getTokenType("GT_SIGN");
  IElementType INT_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("INT_TOKEN");
  IElementType LEFT_BRACE = ParadoxScriptElementTypeFactory.getTokenType("LEFT_BRACE");
  IElementType LE_SIGN = ParadoxScriptElementTypeFactory.getTokenType("LE_SIGN");
  IElementType LT_SIGN = ParadoxScriptElementTypeFactory.getTokenType("LT_SIGN");
  IElementType NOT_EQUAL_SIGN = ParadoxScriptElementTypeFactory.getTokenType("NOT_EQUAL_SIGN");
  IElementType PROPERTY_KEY_ID = ParadoxScriptElementTypeFactory.getTokenType("PROPERTY_KEY_ID");
  IElementType QUOTED_PROPERTY_KEY_ID = ParadoxScriptElementTypeFactory.getTokenType("QUOTED_PROPERTY_KEY_ID");
  IElementType QUOTED_STRING_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("QUOTED_STRING_TOKEN");
  IElementType RIGHT_BRACE = ParadoxScriptElementTypeFactory.getTokenType("RIGHT_BRACE");
  IElementType STRING_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("STRING_TOKEN");
  IElementType TAG_TOKEN = ParadoxScriptElementTypeFactory.getTokenType("TAG_TOKEN");
  IElementType VARIABLE_NAME_ID = ParadoxScriptElementTypeFactory.getTokenType("VARIABLE_NAME_ID");
  IElementType VARIABLE_REFERENCE_ID = ParadoxScriptElementTypeFactory.getTokenType("VARIABLE_REFERENCE_ID");

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
      else if (type == FLOAT) {
        return new ParadoxScriptFloatImpl(node);
      }
      else if (type == INT) {
        return new ParadoxScriptIntImpl(node);
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
      else if (type == TAG) {
        return new ParadoxScriptTagImpl(node);
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
