// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;
import com.windea.plugin.idea.paradox.localisation.psi.*;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationCommandFieldPsiReference;

public class ParadoxLocalisationCommandFieldImpl extends ParadoxLocalisationNamedElementImpl implements
    ParadoxLocalisationCommandField {

  public ParadoxLocalisationCommandFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationPropertyReference getPropertyReference() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationPropertyReference.class);
  }

  @Override
  @Nullable
  public PsiElement getCommandFieldToken() {
    return findChildByType(COMMAND_FIELD_TOKEN);
  }

  @Override
  @Nullable
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  public void checkRename() {
    ParadoxLocalisationPsiImplUtil.checkRename(this);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandFieldPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
