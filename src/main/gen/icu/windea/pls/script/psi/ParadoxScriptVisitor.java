// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import org.jetbrains.annotations.NotNull;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
    // visitPsiListLikeElement(o);
  }

  public void visitBoolean(@NotNull ParadoxScriptBoolean o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitColor(@NotNull ParadoxScriptColor o) {
    visitValue(o);
  }

  public void visitConditionalExpression(@NotNull ParadoxScriptConditionalExpression o) {
    visitConditionExpression(o);
  }

  public void visitConditionalParameter(@NotNull ParadoxScriptConditionalParameter o) {
    visitConditionParameter(o);
  }

  public void visitFloat(@NotNull ParadoxScriptFloat o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitInlineConditionalBlock(@NotNull ParadoxScriptInlineConditionalBlock o) {
    visitConditionalBlock(o);
    // visitInterpolation(o);
    // visitInterpolationContainer(o);
  }

  public void visitInlineMath(@NotNull ParadoxScriptInlineMath o) {
    visitValue(o);
    // visitMacro(o);
    // visitPsiBoundElement(o);
  }

  public void visitInlineMathBinaryExpression(@NotNull ParadoxScriptInlineMathBinaryExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathExpression(@NotNull ParadoxScriptInlineMathExpression o) {
    visitPsiElement(o);
  }

  public void visitInlineMathFactor(@NotNull ParadoxScriptInlineMathFactor o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathGroupingExpression(@NotNull ParadoxScriptInlineMathGroupingExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInlineMathNumber(@NotNull ParadoxScriptInlineMathNumber o) {
    visitInlineMathFactor(o);
    // visitLiteralValue(o);
  }

  public void visitInlineMathParameter(@NotNull ParadoxScriptInlineMathParameter o) {
    visitInlineMathFactor(o);
    // visitParameter(o);
    // visitArgumentAwareElement(o);
  }

  public void visitInlineMathParameterArgument(@NotNull ParadoxScriptInlineMathParameterArgument o) {
    visitArgument(o);
  }

  public void visitInlineMathRoot(@NotNull ParadoxScriptInlineMathRoot o) {
    visitPsiElement(o);
  }

  public void visitInlineMathScriptedVariableReference(@NotNull ParadoxScriptInlineMathScriptedVariableReference o) {
    visitInlineMathFactor(o);
    // visitedVariableReference(o);
    // visitMacro(o);
    // visitInterpolationContainer(o);
  }

  public void visitInlineMathUnaryExpression(@NotNull ParadoxScriptInlineMathUnaryExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInt(@NotNull ParadoxScriptInt o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitNormalConditionalBlock(@NotNull ParadoxScriptNormalConditionalBlock o) {
    visitConditionalBlock(o);
    // visitStatement(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
    // visitPsiListLikeElement(o);
  }

  public void visitNormalParameter(@NotNull ParadoxScriptNormalParameter o) {
    visitParameter(o);
    // visitArgumentAwareElement(o);
    // visitParadoxLanguageInjectionHost(o);
  }

  public void visitNormalParameterArgument(@NotNull ParadoxScriptNormalParameterArgument o) {
    visitArgument(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitMember(o);
    // visitParadoxDefinitionElement(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitStringExpressionElement(o);
    // visitLiteralValue(o);
    // visitInterpolationContainer(o);
    // visitParadoxLanguageInjectionHost(o);
    // visitPsiQuoteAwareElement(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitMemberContainer(o);
    // visitPsiRootBlock(o);
    // visitPsiListLikeElement(o);
  }

  public void visitScriptedVariable(@NotNull ParadoxScriptScriptedVariable o) {
    visitNamedElement(o);
    // visitStatement(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitScriptedVariableName(@NotNull ParadoxScriptScriptedVariableName o) {
    visitInterpolationContainer(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxScriptScriptedVariableReference o) {
    visitValue(o);
    // visitedVariableReference(o);
    // visitMacro(o);
    // visitInterpolationContainer(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
    // visitInterpolationContainer(o);
    // visitParadoxLanguageInjectionHost(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpressionElement(o);
    // visitMember(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitArgument(@NotNull ParadoxScriptArgument o) {
    visitPsiElement(o);
  }

  public void visitConditionExpression(@NotNull ParadoxScriptConditionExpression o) {
    visitPsiElement(o);
  }

  public void visitConditionParameter(@NotNull ParadoxScriptConditionParameter o) {
    visitPsiElement(o);
  }

  public void visitConditionalBlock(@NotNull ParadoxScriptConditionalBlock o) {
    visitPsiElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxScriptExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitInterpolationContainer(@NotNull ParadoxScriptInterpolationContainer o) {
    visitPsiElement(o);
  }

  public void visitMemberContainer(@NotNull ParadoxScriptMemberContainer o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitParameter(@NotNull ParadoxScriptParameter o) {
    visitPsiElement(o);
  }

  public void visitStringExpressionElement(@NotNull ParadoxScriptStringExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
