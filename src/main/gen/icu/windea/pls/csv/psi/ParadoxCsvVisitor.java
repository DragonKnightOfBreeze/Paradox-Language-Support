// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiListLikeElement;

public class ParadoxCsvVisitor extends PsiElementVisitor {

  public void visitColumn(@NotNull ParadoxCsvColumn o) {
    visitPsiLiteralValue(o);
    // visitExpressionElement(o);
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

  public void visitPsiLiteralValue(@NotNull PsiLiteralValue o) {
    visitElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
