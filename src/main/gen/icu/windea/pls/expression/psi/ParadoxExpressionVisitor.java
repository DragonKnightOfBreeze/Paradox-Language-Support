// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ParadoxExpressionVisitor extends PsiElementVisitor {

  public void visitDummyScopeField(@NotNull ParadoxDummyScopeField o) {
    visitScopeField(o);
  }

  public void visitScopeField(@NotNull ParadoxScopeField o) {
    visitExpressionNode(o);
  }

  public void visitScopeFieldExpression(@NotNull ParadoxScopeFieldExpression o) {
    visitExpression(o);
  }

  public void visitScopeLink(@NotNull ParadoxScopeLink o) {
    visitScopeField(o);
  }

  public void visitScopeLinkFromData(@NotNull ParadoxScopeLinkFromData o) {
    visitScopeField(o);
  }

  public void visitSystemScope(@NotNull ParadoxSystemScope o) {
    visitScopeField(o);
  }

  public void visitExpression(@NotNull ParadoxExpression o) {
    visitPsiElement(o);
  }

  public void visitExpressionNode(@NotNull ParadoxExpressionNode o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
