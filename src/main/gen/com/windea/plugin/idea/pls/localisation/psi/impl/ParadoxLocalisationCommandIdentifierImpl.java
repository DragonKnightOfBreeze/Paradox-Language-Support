// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.pls.localisation.psi.*;

public class ParadoxLocalisationCommandIdentifierImpl extends ASTWrapperPsiElement implements ParadoxLocalisationCommandIdentifier {

  public ParadoxLocalisationCommandIdentifierImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandIdentifier(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getPrevIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getPrevIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getNextIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNextIdentifier(this);
  }

}
