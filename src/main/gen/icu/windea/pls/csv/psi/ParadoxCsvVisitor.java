// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;

public class ParadoxCsvVisitor extends PsiElementVisitor {

  public void visitColumn(@NotNull ParadoxCsvColumn o) {
    visitPsiQuoteAwareElement(o);
    // visitLiteralValue(o);
    // visitExpressionElement(o);
  }

  public void visitHeader(@NotNull ParadoxCsvHeader o) {
    visitColumnContainer(o);
  }

  public void visitRow(@NotNull ParadoxCsvRow o) {
    visitColumnContainer(o);
  }

  public void visitPsiQuoteAwareElement(@NotNull PsiQuoteAwareElement o) {
    visitElement(o);
  }

  public void visitColumnContainer(@NotNull ParadoxCsvColumnContainer o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
