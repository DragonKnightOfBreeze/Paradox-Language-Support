// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import org.jetbrains.annotations.*;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitIParadoxScriptBlock(o);
  }

  public void visitBoolean(@NotNull ParadoxScriptBoolean o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitColor(@NotNull ParadoxScriptColor o) {
    visitValue(o);
  }

  public void visitFloat(@NotNull ParadoxScriptFloat o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitInlineMath(@NotNull ParadoxScriptInlineMath o) {
    visitValue(o);
  }

  public void visitInlineMathAbsExpression(@NotNull ParadoxScriptInlineMathAbsExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathBiExpression(@NotNull ParadoxScriptInlineMathBiExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathExpression(@NotNull ParadoxScriptInlineMathExpression o) {
    visitPsiElement(o);
  }

  public void visitInlineMathFactor(@NotNull ParadoxScriptInlineMathFactor o) {
    visitPsiElement(o);
  }

  public void visitInlineMathNumber(@NotNull ParadoxScriptInlineMathNumber o) {
    visitInlineMathFactor(o);
    // visitPsiLiteralValue(o);
    // visitTypedElement(o);
  }

  public void visitInlineMathParExpression(@NotNull ParadoxScriptInlineMathParExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathParameter(@NotNull ParadoxScriptInlineMathParameter o) {
    visitInlineMathFactor(o);
    // visitIParadoxScriptParameter(o);
  }

  public void visitInlineMathUnaryExpression(@NotNull ParadoxScriptInlineMathUnaryExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathVariableReference(@NotNull ParadoxScriptInlineMathVariableReference o) {
    visitInlineMathFactor(o);
    // visitIParadoxScriptVariableReference(o);
  }

  public void visitInt(@NotNull ParadoxScriptInt o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
  }

  public void visitParameter(@NotNull ParadoxScriptParameter o) {
    visitIParadoxScriptParameter(o);
  }

  public void visitParameterCondition(@NotNull ParadoxScriptParameterCondition o) {
    visitPsiListLikeElement(o);
  }

  public void visitParameterConditionExpression(@NotNull ParadoxScriptParameterConditionExpression o) {
    visitPsiElement(o);
  }

  public void visitParameterConditionParameter(@NotNull ParadoxScriptParameterConditionParameter o) {
    visitIParadoxScriptInputParameter(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitTypedElement(o);
    // visitParadoxDefinitionProperty(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitExpressionElement(o);
  }

  public void visitPropertyValue(@NotNull ParadoxScriptPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitIParadoxScriptBlock(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitExpressionElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitTypedElement(o);
  }

  public void visitVariable(@NotNull ParadoxScriptVariable o) {
    visitNamedElement(o);
    // visitTypedElement(o);
  }

  public void visitVariableName(@NotNull ParadoxScriptVariableName o) {
    visitPsiElement(o);
  }

  public void visitVariableReference(@NotNull ParadoxScriptVariableReference o) {
    visitValue(o);
    // visitIParadoxScriptVariableReference(o);
  }

  public void visitVariableValue(@NotNull ParadoxScriptVariableValue o) {
    visitPsiElement(o);
  }

  public void visitPsiListLikeElement(@NotNull PsiListLikeElement o) {
    visitElement(o);
  }

  public void visitIParadoxScriptBlock(@NotNull IParadoxScriptBlock o) {
    visitElement(o);
  }

  public void visitIParadoxScriptInputParameter(@NotNull IParadoxScriptInputParameter o) {
    visitElement(o);
  }

  public void visitIParadoxScriptParameter(@NotNull IParadoxScriptParameter o) {
    visitElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxScriptExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitTypedElement(@NotNull ParadoxScriptTypedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
