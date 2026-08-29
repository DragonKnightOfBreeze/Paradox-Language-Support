// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.psi.PsiDocCommentBase;
import icu.windea.pls.core.psi.PsiRootBlock;
import com.intellij.psi.PsiComment;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
  }

  public void visitBoolean(@NotNull CwtBoolean o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitDocComment(@NotNull CwtDocComment o) {
    visitPsiDocCommentBase(o);
  }

  public void visitFloat(@NotNull CwtFloat o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitOption(@NotNull CwtOption o) {
    visitNamedElement(o);
    // visitOptionMember(o);
  }

  public void visitOptionComment(@NotNull CwtOptionComment o) {
    visitPsiComment(o);
  }

  public void visitOptionKey(@NotNull CwtOptionKey o) {
    visitNavigatablePsiElement(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
    visitNamedElement(o);
    // visitMember(o);
  }

  public void visitPropertyKey(@NotNull CwtPropertyKey o) {
    visitPsiQuoteAwareElement(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
  }

  public void visitRootBlock(@NotNull CwtRootBlock o) {
    visitPsiRootBlock(o);
    // visitMemberContainer(o);
  }

  public void visitString(@NotNull CwtString o) {
    visitValue(o);
    // visitPsiQuoteAwareElement(o);
    // visitNamedElement(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
  }

  public void visitValue(@NotNull CwtValue o) {
    visitExpressionElement(o);
    // visitMember(o);
    // visitOptionMember(o);
  }

  public void visitNavigatablePsiElement(@NotNull NavigatablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiComment(@NotNull PsiComment o) {
    visitElement(o);
  }

  public void visitPsiDocCommentBase(@NotNull PsiDocCommentBase o) {
    visitElement(o);
  }

  public void visitPsiQuoteAwareElement(@NotNull PsiQuoteAwareElement o) {
    visitElement(o);
  }

  public void visitPsiRootBlock(@NotNull PsiRootBlock o) {
    visitElement(o);
  }

  public void visitExpressionElement(@NotNull CwtExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull CwtNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
