// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import icu.windea.pls.lang.psi.ParadoxTypedElement;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiListLikeElement;

public class ParadoxLocalisationVisitor extends PsiElementVisitor {

  public void visitColorfulText(@NotNull ParadoxLocalisationColorfulText o) {
    visitRichText(o);
  }

  public void visitCommand(@NotNull ParadoxLocalisationCommand o) {
    visitRichText(o);
    // visitRichText(o);
  }

  public void visitCommandField(@NotNull ParadoxLocalisationCommandField o) {
    visitCommandIdentifier(o);
  }

  public void visitCommandIdentifier(@NotNull ParadoxLocalisationCommandIdentifier o) {
    visitParadoxTypedElement(o);
  }

  public void visitCommandScope(@NotNull ParadoxLocalisationCommandScope o) {
    visitCommandIdentifier(o);
  }

  public void visitConcept(@NotNull ParadoxLocalisationConcept o) {
    visitPsiElement(o);
  }

  public void visitConceptName(@NotNull ParadoxLocalisationConceptName o) {
    visitExpressionElement(o);
    // visitContributedReferenceHost(o);
  }

  public void visitConceptText(@NotNull ParadoxLocalisationConceptText o) {
    visitPsiElement(o);
  }

  public void visitIcon(@NotNull ParadoxLocalisationIcon o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitLocale(@NotNull ParadoxLocalisationLocale o) {
    visitNavigatablePsiElement(o);
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

  public void visitPropertyReference(@NotNull ParadoxLocalisationPropertyReference o) {
    visitRichText(o);
    // visitNavigatablePsiElement(o);
  }

  public void visitPropertyValue(@NotNull ParadoxLocalisationPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitRichText(@NotNull ParadoxLocalisationRichText o) {
    visitPsiElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxLocalisationScriptedVariableReference o) {
    visitParadoxScriptedVariableReference(o);
  }

  public void visitString(@NotNull ParadoxLocalisationString o) {
    visitRichText(o);
  }

  public void visitNavigatablePsiElement(@NotNull NavigatablePsiElement o) {
    visitElement(o);
  }

  public void visitPsiListLikeElement(@NotNull PsiListLikeElement o) {
    visitElement(o);
  }

  public void visitParadoxScriptedVariableReference(@NotNull ParadoxScriptedVariableReference o) {
    visitElement(o);
  }

  public void visitParadoxTypedElement(@NotNull ParadoxTypedElement o) {
    visitElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxLocalisationExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitNamedElement(@NotNull ParadoxLocalisationNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
