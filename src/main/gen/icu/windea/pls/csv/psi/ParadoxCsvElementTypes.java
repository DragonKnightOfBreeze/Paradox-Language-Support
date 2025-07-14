// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import com.intellij.psi.tree.IElementType;
import com.intellij.psi.PsiElement;
import com.intellij.lang.ASTNode;
import icu.windea.pls.csv.psi.impl.*;

public interface ParadoxCsvElementTypes {

  IElementType COLUMN = ParadoxCsvElementTypeFactory.getElementType("COLUMN");
  IElementType ROW = ParadoxCsvElementTypeFactory.getElementType("ROW");

  IElementType COLUMN_TOKEN = ParadoxCsvElementTypeFactory.getTokenType("COLUMN_TOKEN");
  IElementType COMMENT = ParadoxCsvElementTypeFactory.getTokenType("COMMENT");
  IElementType SEPARATOR = ParadoxCsvElementTypeFactory.getTokenType("SEPARATOR");

  class Factory {
    public static PsiElement createElement(ASTNode node) {
      IElementType type = node.getElementType();
      if (type == COLUMN) {
        return new ParadoxCsvColumnImpl(node);
      }
      else if (type == ROW) {
        return new ParadoxCsvRowImpl(node);
      }
      throw new AssertionError("Unknown element type: " + type);
    }
  }
}
