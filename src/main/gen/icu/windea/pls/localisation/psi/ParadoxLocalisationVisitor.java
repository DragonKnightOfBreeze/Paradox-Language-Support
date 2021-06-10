// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiElement;

public class ParadoxLocalisationVisitor extends PsiElementVisitor {

  public void visitColorfulText(@NotNull ParadoxLocalisationColorfulText o) {
    visitRichText(o);
    // visitNamedElement(o);
  }

  public void visitCommand(@NotNull ParadoxLocalisationCommand o) {
    visitRichText(o);
    // visitRichText(o);
  }

  public void visitCommandField(@NotNull ParadoxLocalisationCommandField o) {
    visitCommandIdentifier(o);
    // visitNamedElement(o);
  }

  public void visitCommandIdentifier(@NotNull ParadoxLocalisationCommandIdentifier o) {
    visitPsiElement(o);
  }

  public void visitCommandScope(@NotNull ParadoxLocalisationCommandScope o) {
    visitCommandIdentifier(o);
    // visitNamedElement(o);
  }

  public void visitEscape(@NotNull ParadoxLocalisationEscape o) {
    visitRichText(o);
  }

  public void visitIcon(@NotNull ParadoxLocalisationIcon o) {
    visitRichText(o);
  }

  public void visitLocale(@NotNull ParadoxLocalisationLocale o) {
    visitNamedElement(o);
  }

  public void visitProperty(@NotNull ParadoxLocalisationProperty o) {
    visitNamedElement(o);
  }

  public void visitPropertyKey(@NotNull ParadoxLocalisationPropertyKey o) {
    visitPsiElement(o);
  }

  public void visitPropertyReference(@NotNull ParadoxLocalisationPropertyReference o) {
    visitRichText(o);
  }

  public void visitPropertyValue(@NotNull ParadoxLocalisationPropertyValue o) {
    visitPsiElement(o);
  }

  public void visitRichText(@NotNull ParadoxLocalisationRichText o) {
    visitPsiElement(o);
  }

  public void visitSequentialNumber(@NotNull ParadoxLocalisationSequentialNumber o) {
    visitRichText(o);
    // visitNamedElement(o);
  }

  public void visitString(@NotNull ParadoxLocalisationString o) {
    visitRichText(o);
  }

  public void visitNamedElement(@NotNull ParadoxLocalisationNamedElement o) {
    visitPsiElement(o);
  }

  public void visitPsiElement(@NotNull PsiElement o) {
    visitElement(o);
  }

}
