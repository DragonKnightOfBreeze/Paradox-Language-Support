// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.localisation.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public class ParadoxLocalisationPropertyReferenceArgumentImpl extends ASTWrapperPsiElement implements ParadoxLocalisationPropertyReferenceArgument {

  public ParadoxLocalisationPropertyReferenceArgumentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitPropertyReferenceArgument(this);
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
