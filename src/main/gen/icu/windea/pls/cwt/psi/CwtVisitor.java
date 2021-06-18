// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiComment;
import com.intellij.psi.PsiListLikeElement;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
    // visitPsiListLikeElement(o);
  }

  public void visitBoolean(@NotNull CwtBoolean o) {
    visitValue(o);
  }

  public void visitDocumentationComment(@NotNull CwtDocumentationComment o) {
    visitPsiComment(o);
  }

  public void visitDocumentationText(@NotNull CwtDocumentationText o) {
    visitPsiElement(o);
  }

  public void visitFloat(@NotNull CwtFloat o) {
    visitNumber(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitNumber(o);
  }

  public void visitNumber(@NotNull CwtNumber o) {
    visitValue(o);
  }

  public void visitOption(@NotNull CwtOption o) {
    visitNamedElement(o);
  }

  public void visitOptionComment(@NotNull CwtOptionComment o) {
    visitPsiComment(o);
  }

  public void visitOptionKey(@NotNull CwtOptionKey o) {
    visitPsiElement(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
    visitNamedElement(o);
  }

  public void visitPropertyKey(@NotNull CwtPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitRootBlock(@NotNull CwtRootBlock o) {
    visitBlock(o);
  }

  public void visitString(@NotNull CwtString o) {
    visitValue(o);
  }

  public void visitValue(@NotNull CwtValue o) {
    visitPsiElement(o);
  }

  public void visitPsiComment(@NotNull PsiComment o) {
    visitElement(o);
  }

  public void visitNamedElement(@NotNull CwtNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
