// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class CwtVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull CwtBlock o) {
    visitValue(o);
  }

  public void visitBoolean(@NotNull CwtBoolean o) {
    visitValue(o);
  }

  public void visitFloat(@NotNull CwtFloat o) {
    visitNumber(o);
  }

  public void visitInt(@NotNull CwtInt o) {
    visitNumber(o);
  }

  public void visitKey(@NotNull CwtKey o) {
    visitPsiElement(o);
  }

  public void visitNumber(@NotNull CwtNumber o) {
    visitValue(o);
  }

  public void visitProperty(@NotNull CwtProperty o) {
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

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
