// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiListLikeElement;

public class ParadoxLocalisationVisitor extends PsiElementVisitor {

  public void visitColorfulText(@NotNull ParadoxLocalisationColorfulText o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitTextColorAwareElement(o);
  }

  public void visitCommand(@NotNull ParadoxLocalisationCommand o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitArgumentAwareElement(o);
  }

  public void visitCommandArgument(@NotNull ParadoxLocalisationCommandArgument o) {
    visitArgument(o);
    // visitTextColorAwareElement(o);
    // visitParameterAwareElement(o);
  }

  public void visitCommandText(@NotNull ParadoxLocalisationCommandText o) {
    visitNavigatablePsiElement(o);
    // visitContributedReferenceHost(o);
    // visitExpressionElement(o);
    // visitParameterAwareElement(o);
  }

  public void visitConceptCommand(@NotNull ParadoxLocalisationConceptCommand o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitConceptName(@NotNull ParadoxLocalisationConceptName o) {
    visitNavigatablePsiElement(o);
    // visitContributedReferenceHost(o);
    // visitExpressionElement(o);
    // visitParameterAwareElement(o);
  }

  public void visitConceptText(@NotNull ParadoxLocalisationConceptText o) {
    visitPsiElement(o);
  }

  public void visitIcon(@NotNull ParadoxLocalisationIcon o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitArgumentAwareElement(o);
    // visitParameterAwareElement(o);
    // visitCommandAwareElement(o);
  }

  public void visitIconArgument(@NotNull ParadoxLocalisationIconArgument o) {
    visitArgument(o);
    // visitParameterAwareElement(o);
  }

  public void visitLocale(@NotNull ParadoxLocalisationLocale o) {
    visitNavigatablePsiElement(o);
  }

  public void visitParameter(@NotNull ParadoxLocalisationParameter o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitArgumentAwareElement(o);
    // visitCommandAwareElement(o);
  }

  public void visitParameterArgument(@NotNull ParadoxLocalisationParameterArgument o) {
    visitArgument(o);
    // visitTextColorAwareElement(o);
  }

  public void visitProperty(@NotNull ParadoxLocalisationProperty o) {
    visitNamedElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxLocalisationPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitPropertyList(@NotNull ParadoxLocalisationPropertyList o) {
    visitPsiListLikeElement(o);
  }

  public void visitPropertyValue(@NotNull ParadoxLocalisationPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitRichText(@NotNull ParadoxLocalisationRichText o) {
    visitPsiElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxLocalisationScriptedVariableReference o) {
    visitNavigatablePsiElement(o);
    // visitParadoxScriptedVariableReference(o);
  }

  public void visitString(@NotNull ParadoxLocalisationString o) {
    visitRichText(o);
  }

  public void visitTextFormat(@NotNull ParadoxLocalisationTextFormat o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitParameterAwareElement(o);
    // visitCommandAwareElement(o);
  }

  public void visitTextFormatText(@NotNull ParadoxLocalisationTextFormatText o) {
    visitPsiElement(o);
  }

  public void visitTextIcon(@NotNull ParadoxLocalisationTextIcon o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
    // visitParameterAwareElement(o);
    // visitCommandAwareElement(o);
  }

  public void visitTextRoot(@NotNull ParadoxLocalisationTextRoot o) {
    visitPsiElement(o);
  }

  public void visitNavigatablePsiElement(@NotNull NavigatablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiListLikeElement(@NotNull PsiListLikeElement o) {
    visitElement(o);
  }

  public void visitArgument(@NotNull ParadoxLocalisationArgument o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxLocalisationNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
