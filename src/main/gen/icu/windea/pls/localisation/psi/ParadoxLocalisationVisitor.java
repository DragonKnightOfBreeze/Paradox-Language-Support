// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.psi.PsiListLikeElement;
import icu.windea.pls.core.psi.PsiPresentableElement;

public class ParadoxLocalisationVisitor extends PsiElementVisitor {

  public void visitColorfulText(@NotNull ParadoxLocalisationColorfulText o) {
    visitRichText(o);
    // visitTextColorAwareElement(o);
    // visitRichTextContainer(o);
  }

  public void visitCommand(@NotNull ParadoxLocalisationCommand o) {
    visitRichText(o);
    // visitInterpolation(o);
    // visitArgumentAwareElement(o);
  }

  public void visitCommandArgument(@NotNull ParadoxLocalisationCommandArgument o) {
    visitArgument(o);
    // visitInterpolationContainer(o);
    // visitTextColorAwareElement(o);
  }

  public void visitCommandText(@NotNull ParadoxLocalisationCommandText o) {
    visitExpressionElement(o);
    // visitInterpolationContainer(o);
  }

  public void visitConceptCommand(@NotNull ParadoxLocalisationConceptCommand o) {
    visitRichText(o);
  }

  public void visitConceptName(@NotNull ParadoxLocalisationConceptName o) {
    visitExpressionElement(o);
    // visitInterpolationContainer(o);
  }

  public void visitConceptText(@NotNull ParadoxLocalisationConceptText o) {
    visitRichTextContainer(o);
  }

  public void visitIcon(@NotNull ParadoxLocalisationIcon o) {
    visitRichText(o);
    // visitInterpolationContainer(o);
    // visitArgumentAwareElement(o);
  }

  public void visitIconArgument(@NotNull ParadoxLocalisationIconArgument o) {
    visitArgument(o);
    // visitInterpolationContainer(o);
  }

  public void visitLocale(@NotNull ParadoxLocalisationLocale o) {
    visitNavigatablePsiElement(o);
  }

  public void visitParameter(@NotNull ParadoxLocalisationParameter o) {
    visitRichText(o);
    // visitInterpolation(o);
    // visitInterpolationContainer(o);
    // visitArgumentAwareElement(o);
  }

  public void visitParameterArgument(@NotNull ParadoxLocalisationParameterArgument o) {
    visitArgument(o);
    // visitTextColorAwareElement(o);
  }

  public void visitProperty(@NotNull ParadoxLocalisationProperty o) {
    visitNavigatablePsiElement(o);
    // visitNamedElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxLocalisationPropertyKey o) {
    visitNavigatablePsiElement(o);
  }

  public void visitPropertyList(@NotNull ParadoxLocalisationPropertyList o) {
    visitPsiListLikeElement(o);
  }

  public void visitPropertyValue(@NotNull ParadoxLocalisationPropertyValue o) {
    visitPsiQuoteAwareElement(o);
    // visitRichTextContainer(o);
  }

  public void visitRichText(@NotNull ParadoxLocalisationRichText o) {
    visitNavigatablePsiElement(o);
    // visitPsiPresentableElement(o);
  }

  public void visitScriptedVariableReference(@NotNull ParadoxLocalisationScriptedVariableReference o) {
    visitParadoxScriptedVariableReference(o);
  }

  public void visitText(@NotNull ParadoxLocalisationText o) {
    visitRichText(o);
  }

  public void visitTextFormat(@NotNull ParadoxLocalisationTextFormat o) {
    visitRichText(o);
    // visitInterpolationContainer(o);
  }

  public void visitTextFormatText(@NotNull ParadoxLocalisationTextFormatText o) {
    visitRichTextContainer(o);
  }

  public void visitTextIcon(@NotNull ParadoxLocalisationTextIcon o) {
    visitRichText(o);
    // visitInterpolationContainer(o);
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

  public void visitPsiQuoteAwareElement(@NotNull PsiQuoteAwareElement o) {
    visitElement(o);
  }

  public void visitParadoxScriptedVariableReference(@NotNull ParadoxScriptedVariableReference o) {
    visitElement(o);
  }

  public void visitArgument(@NotNull ParadoxLocalisationArgument o) {
    visitPsiElement(o);
  }

  public void visitExpressionElement(@NotNull ParadoxLocalisationExpressionElement o) {
    visitPsiElement(o);
  }

  public void visitRichTextContainer(@NotNull ParadoxLocalisationRichTextContainer o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
