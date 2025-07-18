// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.localisation.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationTextFormatPsiReference;
import javax.swing.Icon;

public class ParadoxLocalisationTextFormatImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationTextFormat {

  public ParadoxLocalisationTextFormatImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitTextFormat(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationTextFormatText getTextFormatText() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationTextFormatText.class);
  }

  @Override
  public @Nullable PsiElement getIdElement() {
    return ParadoxLocalisationPsiImplUtil.getIdElement(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @Nullable String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxLocalisationTextFormat setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  public @Nullable ParadoxLocalisationTextFormatPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
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
