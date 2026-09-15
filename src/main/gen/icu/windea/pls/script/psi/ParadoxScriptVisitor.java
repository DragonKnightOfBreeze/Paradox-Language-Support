// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import icu.windea.pls.lang.psi.ParadoxDefinitionElement;
import icu.windea.pls.core.psi.PsiBoundElement;
import icu.windea.pls.core.psi.PsiRootBlock;

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
    visitPsiPresentableTextAwareElement(o);
  }

  public void visitConditionalParameter(@NotNull ParadoxScriptConditionalParameter o) {
    visitConditionParameter(o);
    // visitPsiPresentableTextAwareElement(o);
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
    // visitPsiPresentableTextAwareElement(o);
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
    // visitParameter(o);
    // visitInterpolation(o);
    // visitArgumentAwareElement(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitInlineMathRoot(@NotNull ParadoxScriptInlineMathRoot o) {
    visitPsiElement(o);
  }

  public void visitInlineMathScriptedVariableReference(@NotNull ParadoxScriptInlineMathScriptedVariableReference o) {
    visitInlineMathFactor(o);
    // visitedVariableReference(o);
    // visitInterpolationContainer(o);
    // visitPsiPresentableTextAwareElement(o);
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
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitNormalParameter(@NotNull ParadoxScriptNormalParameter o) {
    visitParameter(o);
    // visitInterpolation(o);
    // visitArgumentAwareElement(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitParameterArgument(@NotNull ParadoxScriptParameterArgument o) {
    visitArgument(o);
  }

  public void visitProperty(@NotNull ParadoxScriptProperty o) {
    visitNamedElement(o);
    // visitMember(o);
    // visitParadoxDefinitionElement(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxScriptPropertyKey o) {
    visitStringExpressionElement(o);
    // visitLiteralValue(o);
    // visitInterpolationContainer(o);
    // visitPsiPresentableTextAwareElement(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitRootBlock(@NotNull ParadoxScriptRootBlock o) {
    visitPsiRootBlock(o);
    // visitMemberContainer(o);
  }

  public void visitScriptedVariable(@NotNull ParadoxScriptScriptedVariable o) {
    visitNamedElement(o);
    // visitStatement(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitScriptedVariableName(@NotNull ParadoxScriptScriptedVariableName o) {
    visitInterpolationContainer(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxScriptScriptedVariableReference o) {
    visitValue(o);
    // visitedVariableReference(o);
    // visitInterpolationContainer(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
    // visitInterpolationContainer(o);
    // visitPsiQuoteAwareElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpressionElement(o);
    // visitMember(o);
    // visitPsiPresentableTextAwareElement(o);
  }

  public void visitPsiPresentableTextAwareElement(@NotNull PsiPresentableTextAwareElement o) {
    visitElement(o);
  }

  public void visitPsiRootBlock(@NotNull PsiRootBlock o) {
    visitElement(o);
  }

  public void visitArgument(@NotNull ParadoxScriptArgument o) {
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
