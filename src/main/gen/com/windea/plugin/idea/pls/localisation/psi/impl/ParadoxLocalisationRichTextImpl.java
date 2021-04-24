// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.pls.localisation.psi.*;

public abstract class ParadoxLocalisationRichTextImpl extends ASTWrapperPsiElement implements ParadoxLocalisationRichText {

  public ParadoxLocalisationRichTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitRichText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

}
