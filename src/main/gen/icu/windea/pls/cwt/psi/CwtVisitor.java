// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiDocCommentBase;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiComment;
import icu.windea.pls.core.psi.PsiRootBlock;
import com.intellij.psi.PsiListLikeElement;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
    // visitPsiListLikeElement(o);
  }

  public void visitBoolean(@NotNull CwtBoolean o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitDocComment(@NotNull CwtDocComment o) {
    visitPsiDocCommentBase(o);
  }

  public void visitFloat(@NotNull CwtFloat o) {
    visitNumber(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitNumber(o);
  }

  public void visitNumber(@NotNull CwtNumber o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitOption(@NotNull CwtOption o) {
    visitNamedElement(o);
    // visitOptionMember(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitOptionComment(@NotNull CwtOptionComment o) {
    visitPsiComment(o);
  }

  public void visitOptionKey(@NotNull CwtOptionKey o) {
    visitPsiQuoteAwareElement(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
    visitNamedElement(o);
    // visitMember(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitPropertyKey(@NotNull CwtPropertyKey o) {
    visitStringExpressionElement(o);
    // visitLiteralValue(o);
    // visitPsiQuoteAwareElement(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitRootBlock(@NotNull CwtRootBlock o) {
    visitMemberContainer(o);
    // visitPsiRootBlock(o);
    // visitPsiListLikeElement(o);
  }

  public void visitString(@NotNull CwtString o) {
    visitValue(o);
    // visitNamedElement(o);
    // visitStringExpressionElement(o);
    // visitLiteralValue(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitValue(@NotNull CwtValue o) {
    visitExpressionElement(o);
    // visitMember(o);
    // visitOptionMember(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
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

  public void visitExpressionElement(@NotNull CwtExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitMemberContainer(@NotNull CwtMemberContainer o) {
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
