// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;

import icu.windea.pls.localisation.psi.util.ParadoxLocalisationPsiImplUtil;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.localisation.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;

public class ParadoxLocalisationConceptNameImpl extends ASTWrapperPsiElement implements ParadoxLocalisationConceptName {

  public ParadoxLocalisationConceptNameImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitConceptName(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxLocalisationPropertyReference> getPropertyReferenceList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxLocalisationPropertyReference.class);
  }

  @Override
  public @NotNull String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull String getValue() {
    return ParadoxLocalisationPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull ParadoxLocalisationConceptName setValue(@NotNull String value) {
    return ParadoxLocalisationPsiImplUtil.setValue(this, value);
  }

  @Override
  public @Nullable ParadoxType getType() {
    return ParadoxLocalisationPsiImplUtil.getType(this);
  }

  @Override
  public @NotNull String getExpression() {
    return ParadoxLocalisationPsiImplUtil.getExpression(this);
  }

  @Override
  public @Nullable PsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return ParadoxLocalisationPsiImplUtil.getReferences(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
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

}
