// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.localisation.psi.ParadoxLocalisationElementPresentation;
import icu.windea.pls.localisation.psi.ParadoxLocalisationTaggedParameter;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParadoxLocalisationTaggedParameterImpl extends ASTWrapperPsiElement implements ParadoxLocalisationTaggedParameter {

  public ParadoxLocalisationTaggedParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitTaggedParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @Nullable PsiElement getIdElement() {
    return ParadoxLocalisationPsiImplUtil.getIdElement(this);
  }

  @Override
  public @Nullable String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxLocalisationTaggedParameter setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ParadoxLocalisationPsiImplUtil.getPresentableText(this);
  }

  @Override
  public @NotNull ParadoxLocalisationElementPresentation getPresentation() {
    return ParadoxLocalisationPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxLocalisationPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxLocalisationPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxLocalisationPsiImplUtil.toString(this);
  }

}
