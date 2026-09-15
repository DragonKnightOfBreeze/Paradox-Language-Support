// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.localisation.psi.ParadoxLocalisationContextTag;
import icu.windea.pls.localisation.psi.ParadoxLocalisationContextTagPart;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParadoxLocalisationContextTagPartImpl extends ASTWrapperPsiElement implements ParadoxLocalisationContextTagPart {

  public ParadoxLocalisationContextTagPartImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitContextTagPart(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationContextTag getContextTag() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationContextTag.class);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxLocalisationPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxLocalisationPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxLocalisationPsiImplUtil.toString(this);
  }

}
