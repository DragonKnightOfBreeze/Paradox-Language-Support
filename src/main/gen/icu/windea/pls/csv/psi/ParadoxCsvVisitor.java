// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ParadoxCsvVisitor extends PsiElementVisitor {

  public void visitColumn(@NotNull ParadoxCsvColumn o) {
    visitLiteralValue(o);
    // visitExpressionElement(o);
  }

  public void visitHeader(@NotNull ParadoxCsvHeader o) {
    visitColumnContainer(o);
  }

  public void visitRow(@NotNull ParadoxCsvRow o) {
    visitColumnContainer(o);
  }

  public void visitColumnContainer(@NotNull ParadoxCsvColumnContainer o) {
    visitPsiElement(o);
  }

  public void visitLiteralValue(@NotNull ParadoxCsvLiteralValue o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
