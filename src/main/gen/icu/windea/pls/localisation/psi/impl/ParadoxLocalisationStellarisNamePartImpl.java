// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import icu.windea.pls.localisation.psi.*;
import icu.windea.pls.localisation.references.ParadoxLocalisationStellarisNamePartPsiReference;

public class ParadoxLocalisationStellarisNamePartImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationStellarisNamePart {

  public ParadoxLocalisationStellarisNamePartImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitStellarisNamePart(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
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
  @Nullable
  public ParadoxLocalisationStellarisNamePartPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
