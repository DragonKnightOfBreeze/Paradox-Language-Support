// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.PsiLiteralValue;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitPsiListLikeElement(o);
  }

  public void visitBoolean(@NotNull ParadoxScriptBoolean o) {
    visitValue(o);
  }

  public void visitCode(@NotNull ParadoxScriptCode o) {
    visitStringValue(o);
  }

  public void visitColor(@NotNull ParadoxScriptColor o) {
    visitStringValue(o);
  }

  public void visitNumber(@NotNull ParadoxScriptNumber o) {
    visitValue(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitPropertyValue(@NotNull ParadoxScriptPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitBlock(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitStringValue(o);
  }

  public void visitStringValue(@NotNull ParadoxScriptStringValue o) {
    visitValue(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitPsiLiteralValue(o);
  }

  public void visitVariable(@NotNull ParadoxScriptVariable o) {
    visitNamedElement(o);
  }

  public void visitVariableName(@NotNull ParadoxScriptVariableName o) {
    visitPsiElement(o);
  }

  public void visitVariableReference(@NotNull ParadoxScriptVariableReference o) {
    visitValue(o);
  }

  public void visitVariableValue(@NotNull ParadoxScriptVariableValue o) {
    visitPsiElement(o);
  }

  public void visitPsiLiteralValue(@NotNull PsiLiteralValue o) {
    visitElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
