// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.localisation.psi.impl.*;

public interface ParadoxLocalisationElementTypes {

  IElementType COLORFUL_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("COLORFUL_TEXT");
  IElementType COMMAND = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND");
  IElementType COMMAND_FIELD = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND_FIELD");
  IElementType COMMAND_IDENTIFIER = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND_IDENTIFIER");
  IElementType COMMAND_SCOPE = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND_SCOPE");
  IElementType CONCEPT_NAME = ParadoxLocalisationElementTypeFactory.getElementType("CONCEPT_NAME");
  IElementType CONCEPT_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("CONCEPT_TEXT");
  IElementType ESCAPE = ParadoxLocalisationElementTypeFactory.getElementType("ESCAPE");
  IElementType ICON = ParadoxLocalisationElementTypeFactory.getElementType("ICON");
  IElementType LOCALE = ParadoxLocalisationElementTypeFactory.getElementType("LOCALE");
  IElementType PROPERTY = ParadoxLocalisationStubElementTypes.getPropertyType("PROPERTY");
  IElementType PROPERTY_KEY = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_KEY");
  IElementType PROPERTY_LIST = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_LIST");
  IElementType PROPERTY_REFERENCE = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_REFERENCE");
  IElementType PROPERTY_VALUE = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_VALUE");
  IElementType RICH_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("RICH_TEXT");
  IElementType SCRIPTED_VARIABLE_REFERENCE = ParadoxLocalisationElementTypeFactory.getElementType("SCRIPTED_VARIABLE_REFERENCE");
  IElementType STRING = ParadoxLocalisationElementTypeFactory.getElementType("STRING");

  IElementType AT = ParadoxLocalisationElementTypeFactory.getTokenType("@");
  IElementType COLON = ParadoxLocalisationElementTypeFactory.getTokenType(":");
  IElementType COLORFUL_TEXT_END = ParadoxLocalisationElementTypeFactory.getTokenType("COLORFUL_TEXT_END");
  IElementType COLORFUL_TEXT_START = ParadoxLocalisationElementTypeFactory.getTokenType("COLORFUL_TEXT_START");
  IElementType COLOR_ID = ParadoxLocalisationElementTypeFactory.getTokenType("COLOR_ID");
  IElementType COMMA = ParadoxLocalisationElementTypeFactory.getTokenType("COMMA");
  IElementType COMMAND_END = ParadoxLocalisationElementTypeFactory.getTokenType("COMMAND_END");
  IElementType COMMAND_FIELD_ID = ParadoxLocalisationElementTypeFactory.getTokenType("COMMAND_FIELD_ID");
  IElementType COMMAND_SCOPE_ID = ParadoxLocalisationElementTypeFactory.getTokenType("COMMAND_SCOPE_ID");
  IElementType COMMAND_START = ParadoxLocalisationElementTypeFactory.getTokenType("COMMAND_START");
  IElementType COMMENT = ParadoxLocalisationElementTypeFactory.getTokenType("COMMENT");
  IElementType CONCEPT_NAME_ID = ParadoxLocalisationElementTypeFactory.getTokenType("CONCEPT_NAME_ID");
  IElementType DOT = ParadoxLocalisationElementTypeFactory.getTokenType("DOT");
  IElementType DOUBLE_LEFT_BRACKET = ParadoxLocalisationElementTypeFactory.getTokenType("DOUBLE_LEFT_BRACKET");
  IElementType ICON_END = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_END");
  IElementType ICON_FRAME = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_FRAME");
  IElementType ICON_ID = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_ID");
  IElementType ICON_START = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_START");
  IElementType INVALID_ESCAPE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("INVALID_ESCAPE_TOKEN");
  IElementType LEFT_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("LEFT_QUOTE");
  IElementType LEFT_SINGLE_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("LEFT_SINGLE_QUOTE");
  IElementType LOCALE_ID = ParadoxLocalisationElementTypeFactory.getTokenType("LOCALE_ID");
  IElementType PIPE = ParadoxLocalisationElementTypeFactory.getTokenType("|");
  IElementType PROPERTY_KEY_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_KEY_TOKEN");
  IElementType PROPERTY_NUMBER = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_NUMBER");
  IElementType PROPERTY_REFERENCE_END = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_REFERENCE_END");
  IElementType PROPERTY_REFERENCE_ID = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_REFERENCE_ID");
  IElementType PROPERTY_REFERENCE_PARAMETER_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_REFERENCE_PARAMETER_TOKEN");
  IElementType PROPERTY_REFERENCE_START = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_REFERENCE_START");
  IElementType RIGHT_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("RIGHT_QUOTE");
  IElementType RIGHT_SINGLE_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("RIGHT_SINGLE_QUOTE");
  IElementType SCRIPTED_VARIABLE_REFERENCE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("SCRIPTED_VARIABLE_REFERENCE_TOKEN");
  IElementType STRING_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("STRING_TOKEN");
  IElementType VALID_ESCAPE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("VALID_ESCAPE_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COLORFUL_TEXT) {
        return new ParadoxLocalisationColorfulTextImpl(node);
      }
      else if (type == COMMAND) {
        return new ParadoxLocalisationCommandImpl(node);
      }
      else if (type == COMMAND_FIELD) {
        return new ParadoxLocalisationCommandFieldImpl(node);
      }
      else if (type == COMMAND_IDENTIFIER) {
        return new ParadoxLocalisationCommandIdentifierImpl(node);
      }
      else if (type == COMMAND_SCOPE) {
        return new ParadoxLocalisationCommandScopeImpl(node);
      }
      else if (type == CONCEPT_NAME) {
        return new ParadoxLocalisationConceptNameImpl(node);
      }
      else if (type == CONCEPT_TEXT) {
        return new ParadoxLocalisationConceptTextImpl(node);
      }
      else if (type == ESCAPE) {
        return new ParadoxLocalisationEscapeImpl(node);
      }
      else if (type == ICON) {
        return new ParadoxLocalisationIconImpl(node);
      }
      else if (type == LOCALE) {
        return new ParadoxLocalisationLocaleImpl(node);
      }
      else if (type == PROPERTY) {
        return new ParadoxLocalisationPropertyImpl(node);
      }
      else if (type == PROPERTY_KEY) {
        return new ParadoxLocalisationPropertyKeyImpl(node);
      }
      else if (type == PROPERTY_LIST) {
        return new ParadoxLocalisationPropertyListImpl(node);
      }
      else if (type == PROPERTY_REFERENCE) {
        return new ParadoxLocalisationPropertyReferenceImpl(node);
      }
      else if (type == PROPERTY_VALUE) {
        return new ParadoxLocalisationPropertyValueImpl(node);
      }
      else if (type == SCRIPTED_VARIABLE_REFERENCE) {
        return new ParadoxLocalisationScriptedVariableReferenceImpl(node);
      }
      else if (type == STRING) {
        return new ParadoxLocalisationStringImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
