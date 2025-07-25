// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.ContributedReferenceHost;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import com.intellij.psi.PsiListLikeElement;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitContributedReferenceHost(o);
    // visitBlockElement(o);
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
    // visitContributedReferenceHost(o);
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
  }

  public void visitInlineMathParExpression(@NotNull ParadoxScriptInlineMathParExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathParameter(@NotNull ParadoxScriptInlineMathParameter o) {
    visitInlineMathFactor(o);
    // visitParadoxParameter(o);
    // visitArgumentAwareElement(o);
  }

  public void visitInlineMathScriptedVariableReference(@NotNull ParadoxScriptInlineMathScriptedVariableReference o) {
    visitInlineMathFactor(o);
    // visitContributedReferenceHost(o);
    // visitedVariableReference(o);
    // visitParadoxParameterAwareElement(o);
  }

  public void visitInlineMathUnaryExpression(@NotNull ParadoxScriptInlineMathUnaryExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineParameterCondition(@NotNull ParadoxScriptInlineParameterCondition o) {
    visitPsiElement(o);
  }

  public void visitInt(@NotNull ParadoxScriptInt o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
    // visitContributedReferenceHost(o);
  }

  public void visitParameter(@NotNull ParadoxScriptParameter o) {
    visitParadoxParameter(o);
    // visitArgumentAwareElement(o);
  }

  public void visitParameterArgument(@NotNull ParadoxScriptParameterArgument o) {
    visitArgument(o);
  }

  public void visitParameterCondition(@NotNull ParadoxScriptParameterCondition o) {
    visitPsiListLikeElement(o);
  }

  public void visitParameterConditionExpression(@NotNull ParadoxScriptParameterConditionExpression o) {
    visitPsiElement(o);
  }

  public void visitParameterConditionParameter(@NotNull ParadoxScriptParameterConditionParameter o) {
    visitParadoxConditionParameter(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitDefinitionElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitPsiLiteralValue(o);
    // visitContributedReferenceHost(o);
    // visitStringExpressionElement(o);
    // visitParadoxParameterAwareElement(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitBlockElement(o);
  }

  public void visitScriptedVariable(@NotNull ParadoxScriptScriptedVariable o) {
    visitNamedElement(o);
  }

  public void visitScriptedVariableName(@NotNull ParadoxScriptScriptedVariableName o) {
    visitParadoxParameterAwareElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxScriptScriptedVariableReference o) {
    visitValue(o);
    // visitContributedReferenceHost(o);
    // visitedVariableReference(o);
    // visitParadoxParameterAwareElement(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitPsiLiteralValue(o);
    // visitContributedReferenceHost(o);
    // visitStringExpressionElement(o);
    // visitParadoxParameterAwareElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpressionElement(o);
    // visitMemberElement(o);
  }

  public void visitPsiListLikeElement(@NotNull PsiListLikeElement o) {
    visitElement(o);
  }

  public void visitPsiLiteralValue(@NotNull PsiLiteralValue o) {
    visitElement(o);
  }

  public void visitParadoxConditionParameter(@NotNull ParadoxConditionParameter o) {
    visitElement(o);
  }

  public void visitParadoxParameter(@NotNull ParadoxParameter o) {
    visitElement(o);
  }

  public void visitParadoxParameterAwareElement(@NotNull ParadoxParameterAwareElement o) {
    visitElement(o);
  }

  public void visitArgument(@NotNull ParadoxScriptArgument o) {
    visitPsiElement(o);
  }

  public void visitBlockElement(@NotNull ParadoxScriptBlockElement o) {
    visitPsiElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxScriptExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
