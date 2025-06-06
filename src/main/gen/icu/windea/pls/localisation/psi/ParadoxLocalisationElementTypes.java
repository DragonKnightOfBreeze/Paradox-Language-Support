// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.localisation.psi.impl.*;

public interface ParadoxLocalisationElementTypes {

  IElementType COLORFUL_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("COLORFUL_TEXT");
  IElementType COMMAND = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND");
  IElementType COMMAND_ARGUMENT = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND_ARGUMENT");
  IElementType COMMAND_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("COMMAND_TEXT");
  IElementType CONCEPT_COMMAND = ParadoxLocalisationElementTypeFactory.getElementType("CONCEPT_COMMAND");
  IElementType CONCEPT_NAME = ParadoxLocalisationElementTypeFactory.getElementType("CONCEPT_NAME");
  IElementType CONCEPT_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("CONCEPT_TEXT");
  IElementType ICON = ParadoxLocalisationElementTypeFactory.getElementType("ICON");
  IElementType ICON_ARGUMENT = ParadoxLocalisationElementTypeFactory.getElementType("ICON_ARGUMENT");
  IElementType LOCALE = ParadoxLocalisationElementTypeFactory.getElementType("LOCALE");
  IElementType PARAMETER = ParadoxLocalisationElementTypeFactory.getElementType("PARAMETER");
  IElementType PARAMETER_ARGUMENT = ParadoxLocalisationElementTypeFactory.getElementType("PARAMETER_ARGUMENT");
  IElementType PROPERTY = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY");
  IElementType PROPERTY_KEY = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_KEY");
  IElementType PROPERTY_LIST = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_LIST");
  IElementType PROPERTY_VALUE = ParadoxLocalisationElementTypeFactory.getElementType("PROPERTY_VALUE");
  IElementType RICH_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("RICH_TEXT");
  IElementType SCRIPTED_VARIABLE_REFERENCE = ParadoxLocalisationElementTypeFactory.getElementType("SCRIPTED_VARIABLE_REFERENCE");
  IElementType STRING = ParadoxLocalisationElementTypeFactory.getElementType("STRING");
  IElementType TEXT_FORMAT = ParadoxLocalisationElementTypeFactory.getElementType("TEXT_FORMAT");
  IElementType TEXT_FORMAT_TEXT = ParadoxLocalisationElementTypeFactory.getElementType("TEXT_FORMAT_TEXT");
  IElementType TEXT_ICON = ParadoxLocalisationElementTypeFactory.getElementType("TEXT_ICON");
  IElementType TEXT_ROOT = ParadoxLocalisationElementTypeFactory.getElementType("TEXT_ROOT");

  IElementType ARGUMENT_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("ARGUMENT_TOKEN");
  IElementType AT = ParadoxLocalisationElementTypeFactory.getTokenType("AT");
  IElementType COLON = ParadoxLocalisationElementTypeFactory.getTokenType("COLON");
  IElementType COLORFUL_TEXT_END = ParadoxLocalisationElementTypeFactory.getTokenType("COLORFUL_TEXT_END");
  IElementType COLORFUL_TEXT_START = ParadoxLocalisationElementTypeFactory.getTokenType("COLORFUL_TEXT_START");
  IElementType COLOR_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("COLOR_TOKEN");
  IElementType COMMA = ParadoxLocalisationElementTypeFactory.getTokenType("COMMA");
  IElementType COMMAND_TEXT_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("COMMAND_TEXT_TOKEN");
  IElementType COMMENT = ParadoxLocalisationElementTypeFactory.getTokenType("COMMENT");
  IElementType CONCEPT_NAME_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("CONCEPT_NAME_TOKEN");
  IElementType ICON_END = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_END");
  IElementType ICON_START = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_START");
  IElementType ICON_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("ICON_TOKEN");
  IElementType LEFT_BRACKET = ParadoxLocalisationElementTypeFactory.getTokenType("LEFT_BRACKET");
  IElementType LEFT_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("LEFT_QUOTE");
  IElementType LEFT_SINGLE_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("LEFT_SINGLE_QUOTE");
  IElementType LOCALE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("LOCALE_TOKEN");
  IElementType PARAMETER_END = ParadoxLocalisationElementTypeFactory.getTokenType("PARAMETER_END");
  IElementType PARAMETER_START = ParadoxLocalisationElementTypeFactory.getTokenType("PARAMETER_START");
  IElementType PARAMETER_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("PARAMETER_TOKEN");
  IElementType PIPE = ParadoxLocalisationElementTypeFactory.getTokenType("PIPE");
  IElementType PROPERTY_KEY_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_KEY_TOKEN");
  IElementType PROPERTY_NUMBER = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_NUMBER");
  IElementType PROPERTY_VALUE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("PROPERTY_VALUE_TOKEN");
  IElementType RIGHT_BRACKET = ParadoxLocalisationElementTypeFactory.getTokenType("RIGHT_BRACKET");
  IElementType RIGHT_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("RIGHT_QUOTE");
  IElementType RIGHT_SINGLE_QUOTE = ParadoxLocalisationElementTypeFactory.getTokenType("RIGHT_SINGLE_QUOTE");
  IElementType SCRIPTED_VARIABLE_REFERENCE_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("SCRIPTED_VARIABLE_REFERENCE_TOKEN");
  IElementType STRING_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("STRING_TOKEN");
  IElementType TEXT_FORMAT_END = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_FORMAT_END");
  IElementType TEXT_FORMAT_START = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_FORMAT_START");
  IElementType TEXT_FORMAT_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_FORMAT_TOKEN");
  IElementType TEXT_ICON_END = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_ICON_END");
  IElementType TEXT_ICON_START = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_ICON_START");
  IElementType TEXT_ICON_TOKEN = ParadoxLocalisationElementTypeFactory.getTokenType("TEXT_ICON_TOKEN");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COLORFUL_TEXT) {
        return new ParadoxLocalisationColorfulTextImpl(node);
      }
      else if (type == COMMAND) {
        return new ParadoxLocalisationCommandImpl(node);
      }
      else if (type == COMMAND_ARGUMENT) {
        return new ParadoxLocalisationCommandArgumentImpl(node);
      }
      else if (type == COMMAND_TEXT) {
        return new ParadoxLocalisationCommandTextImpl(node);
      }
      else if (type == CONCEPT_COMMAND) {
        return new ParadoxLocalisationConceptCommandImpl(node);
      }
      else if (type == CONCEPT_NAME) {
        return new ParadoxLocalisationConceptNameImpl(node);
      }
      else if (type == CONCEPT_TEXT) {
        return new ParadoxLocalisationConceptTextImpl(node);
      }
      else if (type == ICON) {
        return new ParadoxLocalisationIconImpl(node);
      }
      else if (type == ICON_ARGUMENT) {
        return new ParadoxLocalisationIconArgumentImpl(node);
      }
      else if (type == LOCALE) {
        return new ParadoxLocalisationLocaleImpl(node);
      }
      else if (type == PARAMETER) {
        return new ParadoxLocalisationParameterImpl(node);
      }
      else if (type == PARAMETER_ARGUMENT) {
        return new ParadoxLocalisationParameterArgumentImpl(node);
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
      else if (type == PROPERTY_VALUE) {
        return new ParadoxLocalisationPropertyValueImpl(node);
      }
      else if (type == SCRIPTED_VARIABLE_REFERENCE) {
        return new ParadoxLocalisationScriptedVariableReferenceImpl(node);
      }
      else if (type == STRING) {
        return new ParadoxLocalisationStringImpl(node);
      }
      else if (type == TEXT_FORMAT) {
        return new ParadoxLocalisationTextFormatImpl(node);
      }
      else if (type == TEXT_FORMAT_TEXT) {
        return new ParadoxLocalisationTextFormatTextImpl(node);
      }
      else if (type == TEXT_ICON) {
        return new ParadoxLocalisationTextIconImpl(node);
      }
      else if (type == TEXT_ROOT) {
        return new ParadoxLocalisationTextRootImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
