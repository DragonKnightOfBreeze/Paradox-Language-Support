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
import com.intellij.util.IncorrectOperationException;
import com.windea.plugin.idea.paradox.localisation.reference.ParadoxLocalisationCommandKeyPsiReference;

public class ParadoxLocalisationCommandKeyImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationCommandKey {

  public ParadoxLocalisationCommandKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getCommandKeyToken() {
    return notNullChild(findChildByType(COMMAND_KEY_TOKEN));
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) throws IncorrectOperationException {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationCommandKeyPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
