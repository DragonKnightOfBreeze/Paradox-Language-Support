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
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationCommandScopePsiReference;

public class ParadoxLocalisationCommandScopeImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationCommandScope {

  public ParadoxLocalisationCommandScopeImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandScope(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getCommandScopeToken() {
    return notNullChild(findChildByType(COMMAND_SCOPE_TOKEN));
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
  public void checkSetName(@NotNull String name) {
    ParadoxLocalisationPsiImplUtil.checkSetName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationCommandScopePsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
