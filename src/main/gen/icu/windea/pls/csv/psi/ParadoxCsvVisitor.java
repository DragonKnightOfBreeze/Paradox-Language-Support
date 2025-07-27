// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ParadoxCsvVisitor extends PsiElementVisitor {

  public void visitColumn(@NotNull ParadoxCsvColumn o) {
    visitExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitHeader(@NotNull ParadoxCsvHeader o) {
    visitRowElement(o);
  }

  public void visitRow(@NotNull ParadoxCsvRow o) {
    visitRowElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxCsvExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitRowElement(@NotNull ParadoxCsvRowElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
