// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.localisation.psi.impl.*;

public interface ParadoxLocalisationElementTypes {

  IElementType COLORFUL_TEXT = new ParadoxLocalisationElementType("COLORFUL_TEXT");
  IElementType COMMAND = new ParadoxLocalisationElementType("COMMAND");
  IElementType COMMAND_FIELD = new ParadoxLocalisationElementType("COMMAND_FIELD");
  IElementType COMMAND_IDENTIFIER = new ParadoxLocalisationElementType("COMMAND_IDENTIFIER");
  IElementType COMMAND_SCOPE = new ParadoxLocalisationElementType("COMMAND_SCOPE");
  IElementType ESCAPE = new ParadoxLocalisationElementType("ESCAPE");
  IElementType ICON = new ParadoxLocalisationElementType("ICON");
  IElementType LOCALE = new ParadoxLocalisationElementType("LOCALE");
  IElementType PROPERTY = ParadoxLocalisationStubElementTypes.getPropertyType("PROPERTY");
  IElementType PROPERTY_KEY = new ParadoxLocalisationElementType("PROPERTY_KEY");
  IElementType PROPERTY_LIST = new ParadoxLocalisationElementType("PROPERTY_LIST");
  IElementType PROPERTY_REFERENCE = new ParadoxLocalisationElementType("PROPERTY_REFERENCE");
  IElementType PROPERTY_REFERENCE_PARAMETER = new ParadoxLocalisationElementType("PROPERTY_REFERENCE_PARAMETER");
  IElementType PROPERTY_VALUE = new ParadoxLocalisationElementType("PROPERTY_VALUE");
  IElementType RICH_TEXT = new ParadoxLocalisationElementType("RICH_TEXT");
  IElementType STRING = new ParadoxLocalisationElementType("STRING");

  IElementType COLON = new ParadoxLocalisationTokenType("COLON");
  IElementType COLORFUL_TEXT_END = new ParadoxLocalisationTokenType("COLORFUL_TEXT_END");
  IElementType COLORFUL_TEXT_START = new ParadoxLocalisationTokenType("COLORFUL_TEXT_START");
  IElementType COLOR_ID = new ParadoxLocalisationTokenType("COLOR_ID");
  IElementType COMMAND_END = new ParadoxLocalisationTokenType("COMMAND_END");
  IElementType COMMAND_FIELD_ID = new ParadoxLocalisationTokenType("COMMAND_FIELD_ID");
  IElementType COMMAND_SCOPE_ID = new ParadoxLocalisationTokenType("COMMAND_SCOPE_ID");
  IElementType COMMAND_START = new ParadoxLocalisationTokenType("COMMAND_START");
  IElementType COMMENT = new ParadoxLocalisationTokenType("COMMENT");
  IElementType DOT = new ParadoxLocalisationTokenType("DOT");
  IElementType DOUBLE_LEFT_BRACKET = new ParadoxLocalisationTokenType("DOUBLE_LEFT_BRACKET");
  IElementType ICON_END = new ParadoxLocalisationTokenType("ICON_END");
  IElementType ICON_FRAME = new ParadoxLocalisationTokenType("ICON_FRAME");
  IElementType ICON_ID = new ParadoxLocalisationTokenType("ICON_ID");
  IElementType ICON_START = new ParadoxLocalisationTokenType("ICON_START");
  IElementType INVALID_ESCAPE_TOKEN = new ParadoxLocalisationTokenType("INVALID_ESCAPE_TOKEN");
  IElementType LEFT_QUOTE = new ParadoxLocalisationTokenType("LEFT_QUOTE");
  IElementType LOCALE_ID = new ParadoxLocalisationTokenType("LOCALE_ID");
  IElementType PIPE = new ParadoxLocalisationTokenType("PIPE");
  IElementType PROPERTY_KEY_ID = new ParadoxLocalisationTokenType("PROPERTY_KEY_ID");
  IElementType PROPERTY_NUMBER = new ParadoxLocalisationTokenType("PROPERTY_NUMBER");
  IElementType PROPERTY_REFERENCE_END = new ParadoxLocalisationTokenType("PROPERTY_REFERENCE_END");
  IElementType PROPERTY_REFERENCE_ID = new ParadoxLocalisationTokenType("PROPERTY_REFERENCE_ID");
  IElementType PROPERTY_REFERENCE_PARAMETER_TOKEN = new ParadoxLocalisationTokenType("PROPERTY_REFERENCE_PARAMETER_TOKEN");
  IElementType PROPERTY_REFERENCE_START = new ParadoxLocalisationTokenType("PROPERTY_REFERENCE_START");
  IElementType RIGHT_QUOTE = new ParadoxLocalisationTokenType("RIGHT_QUOTE");
  IElementType STRING_TOKEN = new ParadoxLocalisationTokenType("STRING_TOKEN");
  IElementType VALID_ESCAPE_TOKEN = new ParadoxLocalisationTokenType("VALID_ESCAPE_TOKEN");

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
      else if (type == PROPERTY_REFERENCE_PARAMETER) {
        return new ParadoxLocalisationPropertyReferenceParameterImpl(node);
      }
      else if (type == PROPERTY_VALUE) {
        return new ParadoxLocalisationPropertyValueImpl(node);
      }
      else if (type == STRING) {
        return new ParadoxLocalisationStringImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
