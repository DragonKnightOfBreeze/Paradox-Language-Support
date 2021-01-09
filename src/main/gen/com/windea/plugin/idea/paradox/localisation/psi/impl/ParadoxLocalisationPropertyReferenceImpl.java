// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;
import com.windea.plugin.idea.paradox.localisation.psi.*;
import com.windea.plugin.idea.paradox.ParadoxColor;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationPropertyPsiReference;

public class ParadoxLocalisationPropertyReferenceImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationPropertyReference {

  public ParadoxLocalisationPropertyReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitPropertyReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommand getCommand() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationCommand.class);
  }

  @Override
  @Nullable
  public PsiElement getPropertyReferenceId() {
    return findChildByType(PROPERTY_REFERENCE_ID);
  }

  @Override
  @Nullable
  public PsiElement getPropertyReferenceParameter() {
    return findChildByType(PROPERTY_REFERENCE_PARAMETER);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxLocalisationPsiImplUtil.getTextOffset(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationPropertyPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  @Nullable
  public ParadoxColor getParadoxColor() {
    return ParadoxLocalisationPsiImplUtil.getParadoxColor(this);
  }

}
