// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.PsiListLikeElement;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitPsiListLikeElement(o);
  }

  public void visitBoolean(@NotNull ParadoxScriptBoolean o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitColor(@NotNull ParadoxScriptColor o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitFloat(@NotNull ParadoxScriptFloat o) {
    visitNumber(o);
  }

  public void visitInlineMath(@NotNull ParadoxScriptInlineMath o) {
    visitValue(o);
  }

  public void visitInlineMathFactor(@NotNull ParadoxScriptInlineMathFactor o) {
    visitPsiElement(o);
  }

  public void visitInlineMathNumber(@NotNull ParadoxScriptInlineMathNumber o) {
    visitPsiLiteralValue(o);
    // visitExpression(o);
  }

  public void visitInlineMathParameter(@NotNull ParadoxScriptInlineMathParameter o) {
    visitPsiElement(o);
  }

  public void visitInlineMathVariableReference(@NotNull ParadoxScriptInlineMathVariableReference o) {
    visitExpression(o);
  }

  public void visitInt(@NotNull ParadoxScriptInt o) {
    visitNumber(o);
  }

  public void visitNumber(@NotNull ParadoxScriptNumber o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitExpression(o);
    // visitParadoxDefinitionProperty(o);
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
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitTag(@NotNull ParadoxScriptTag o) {
    visitPsiElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpression(o);
  }

  public void visitVariable(@NotNull ParadoxScriptVariable o) {
    visitNamedElement(o);
    // visitExpression(o);
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

  public void visitExpression(@NotNull ParadoxScriptExpression o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
