// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiComment;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
    // visitNamedElement(o);
    // visitBlockElement(o);
  }

  public void visitBoolean(@NotNull CwtBoolean o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitDocComment(@NotNull CwtDocComment o) {
    visitPsiComment(o);
  }

  public void visitFloat(@NotNull CwtFloat o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitOption(@NotNull CwtOption o) {
    visitNamedElement(o);
    // visitOptionMemberElement(o);
  }

  public void visitOptionComment(@NotNull CwtOptionComment o) {
    visitPsiComment(o);
  }

  public void visitOptionCommentRoot(@NotNull CwtOptionCommentRoot o) {
    visitPsiElement(o);
  }

  public void visitOptionKey(@NotNull CwtOptionKey o) {
    visitPsiElement(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
    visitNamedElement(o);
    // visitMemberElement(o);
  }

  public void visitPropertyKey(@NotNull CwtPropertyKey o) {
    visitStringExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitRootBlock(@NotNull CwtRootBlock o) {
    visitBlockElement(o);
  }

  public void visitString(@NotNull CwtString o) {
    visitValue(o);
    // visitNamedElement(o);
    // visitStringExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitValue(@NotNull CwtValue o) {
    visitExpressionElement(o);
    // visitMemberElement(o);
    // visitOptionMemberElement(o);
  }

  public void visitPsiComment(@NotNull PsiComment o) {
    visitElement(o);
  }

  public void visitBlockElement(@NotNull CwtBlockElement o) {
    visitPsiElement(o);
  }

  public void visitExpressionElement(@NotNull CwtExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull CwtNamedElement o) {
    visitPsiElement(o);
  }

  public void visitStringExpressionElement(@NotNull CwtStringExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
