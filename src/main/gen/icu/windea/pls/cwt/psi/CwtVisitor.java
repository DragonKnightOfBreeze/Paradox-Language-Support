// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.psi.PsiDocCommentBase;
import com.intellij.psi.PsiListLikeElement;
import icu.windea.pls.core.psi.PsiRootBlock;
import com.intellij.psi.PsiComment;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
    // visitPsiListLikeElement(o);
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
    // visitNumberExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitValue(o);
    // visitNumberExpressionElement(o);
    // visitLiteralValue(o);
  }

  public void visitOption(@NotNull CwtOption o) {
    visitNamedElement(o);
    // visitOptionMember(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitOptionComment(@NotNull CwtOptionComment o) {
    visitPsiComment(o);
  }

  public void visitOptionKey(@NotNull CwtOptionKey o) {
    visitPsiPresentableTextAwareElement(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
    visitNamedElement(o);
    // visitMember(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitPropertyKey(@NotNull CwtPropertyKey o) {
    visitStringExpressionElement(o);
    // visitLiteralValue(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitRootBlock(@NotNull CwtRootBlock o) {
    visitPsiListLikeElement(o);
    // visitMemberContainer(o);
    // visitPsiRootBlock(o);
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
  }

  public void visitPsiComment(@NotNull PsiComment o) {
    visitElement(o);
  }

  public void visitPsiDocCommentBase(@NotNull PsiDocCommentBase o) {
    visitElement(o);
  }

  public void visitPsiListLikeElement(@NotNull PsiListLikeElement o) {
    visitElement(o);
  }

  public void visitPsiPresentableTextAwareElement(@NotNull PsiPresentableTextAwareElement o) {
    visitElement(o);
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
