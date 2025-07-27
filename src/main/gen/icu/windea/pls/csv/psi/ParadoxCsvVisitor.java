// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ParadoxCsvVisitor extends PsiElementVisitor {

  public void visitColumn(@NotNull ParadoxCsvColumn o) {
    visitExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitHeader(@NotNull ParadoxCsvHeader o) {
    visitNavigatablePsiElement(o);
    // visitPsiListLikeElement(o);
  }

  public void visitRow(@NotNull ParadoxCsvRow o) {
    visitNavigatablePsiElement(o);
    // visitPsiListLikeElement(o);
  }

  public void visitNavigatablePsiElement(@NotNull NavigatablePsiElement o) {
    visitElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxCsvExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
