// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import icu.windea.pls.core.psi.PsiPresentableElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.psi.PsiRootBlock;
import org.jetbrains.annotations.NotNull;

public class ParadoxScriptVisitor extends PsiElementVisitor {

  public void visitBlock(@NotNull ParadoxScriptBlock o) {
    visitValue(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
  }

  public void visitBoolean(@NotNull ParadoxScriptBoolean o) {
    visitValue(o);
    // visitLiteralValue(o);
  }

  public void visitColor(@NotNull ParadoxScriptColor o) {
    visitValue(o);
  }

  public void visitConditionalExpression(@NotNull ParadoxScriptConditionalExpression o) {
    visitPsiPresentableElement(o);
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
    // visitParadoxParameter(o);
    // visitInterpolation(o);
    // visitParadoxArgumentAwareElement(o);
  }

  public void visitInlineMathRoot(@NotNull ParadoxScriptInlineMathRoot o) {
    visitPsiElement(o);
  }

  public void visitInlineMathScriptedVariableReference(@NotNull ParadoxScriptInlineMathScriptedVariableReference o) {
    visitInlineMathFactor(o);
    // visitedVariableReference(o);
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
  }

  public void visitParameter(@NotNull ParadoxScriptParameter o) {
    visitParadoxParameter(o);
    // visitInterpolation(o);
    // visitParadoxArgumentAwareElement(o);
  }

  public void visitParameterArgument(@NotNull ParadoxScriptParameterArgument o) {
    visitParadoxArgument(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitMember(o);
    // visitParadoxDefinitionElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitPsiQuoteAwareElement(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
    // visitInterpolationContainer(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitPsiRootBlock(o);
    // visitMemberContainer(o);
  }

  public void visitScriptedVariable(@NotNull ParadoxScriptScriptedVariable o) {
    visitNamedElement(o);
    // visitStatement(o);
  }

  public void visitScriptedVariableName(@NotNull ParadoxScriptScriptedVariableName o) {
    visitInterpolationContainer(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxScriptScriptedVariableReference o) {
    visitValue(o);
    // visitedVariableReference(o);
    // visitInterpolationContainer(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitPsiQuoteAwareElement(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
    // visitInterpolationContainer(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpressionElement(o);
    // visitMember(o);
  }

  public void visitPsiPresentableElement(@NotNull PsiPresentableElement o) {
    visitElement(o);
  }

  public void visitPsiQuoteAwareElement(@NotNull PsiQuoteAwareElement o) {
    visitElement(o);
  }

  public void visitPsiRootBlock(@NotNull PsiRootBlock o) {
    visitElement(o);
  }

  public void visitParadoxArgument(@NotNull ParadoxArgument o) {
    visitElement(o);
  }

  public void visitParadoxParameter(@NotNull ParadoxParameter o) {
    visitElement(o);
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

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
