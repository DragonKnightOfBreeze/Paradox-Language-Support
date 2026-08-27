// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import icu.windea.pls.core.psi.PsiBoundElement;
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

  public void visitConditionalBlock(@NotNull ParadoxScriptConditionalBlock o) {
    visitStatement(o);
    // visitMemberContainer(o);
    // visitBoundMemberContainer(o);
  }

  public void visitConditionalExpression(@NotNull ParadoxScriptConditionalExpression o) {
    visitPsiElement(o);
  }

  public void visitConditionalParameter(@NotNull ParadoxScriptConditionalParameter o) {
    visitParadoxConditionParameter(o);
  }

  public void visitFloat(@NotNull ParadoxScriptFloat o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitInlineConditionalBlock(@NotNull ParadoxScriptInlineConditionalBlock o) {
    visitPsiBoundElement(o);
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
    // visitParadoxArgumentAwareElement(o);
  }

  public void visitInlineMathRoot(@NotNull ParadoxScriptInlineMathRoot o) {
    visitPsiElement(o);
  }

  public void visitInlineMathScriptedVariableReference(@NotNull ParadoxScriptInlineMathScriptedVariableReference o) {
    visitInlineMathFactor(o);
    // visitedVariableReference(o);
    // visitParadoxParameterAwareElement(o);
    // visitInlineConditionalBlockAwareElement(o);
  }

  public void visitInlineMathUnaryExpression(@NotNull ParadoxScriptInlineMathUnaryExpression o) {
    visitInlineMathExpression(o);
  }

  public void visitInt(@NotNull ParadoxScriptInt o) {
    visitValue(o);
    // visitLiteralValue(o);
    // visitNumberExpressionElement(o);
  }

  public void visitParameter(@NotNull ParadoxScriptParameter o) {
    visitParadoxParameter(o);
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
    // visitParadoxParameterAwareElement(o);
    // visitInlineConditionalBlockAwareElement(o);
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
    visitParadoxParameterAwareElement(o);
    // visitInlineConditionalBlockAwareElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxScriptScriptedVariableReference o) {
    visitValue(o);
    // visitedVariableReference(o);
    // visitParadoxParameterAwareElement(o);
    // visitInlineConditionalBlockAwareElement(o);
  }

  public void visitString(@NotNull ParadoxScriptString o) {
    visitValue(o);
    // visitPsiQuoteAwareElement(o);
    // visitLiteralValue(o);
    // visitStringExpressionElement(o);
    // visitParadoxParameterAwareElement(o);
    // visitInlineConditionalBlockAwareElement(o);
  }

  public void visitValue(@NotNull ParadoxScriptValue o) {
    visitExpressionElement(o);
    // visitMember(o);
  }

  public void visitPsiBoundElement(@NotNull PsiBoundElement o) {
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

  public void visitParadoxConditionParameter(@NotNull ParadoxConditionParameter o) {
    visitElement(o);
  }

  public void visitParadoxParameter(@NotNull ParadoxParameter o) {
    visitElement(o);
  }

  public void visitParadoxParameterAwareElement(@NotNull ParadoxParameterAwareElement o) {
    visitElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxScriptExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxScriptNamedElement o) {
    visitPsiElement(o);
  }

  public void visitStatement(@NotNull ParadoxScriptStatement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
