// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.cwt.psi.CwtFloat;
import icu.windea.pls.cwt.psi.CwtVisitor;
import org.jetbrains.annotations.NotNull;

public class CwtFloatImpl extends CwtValueImpl implements CwtFloat {

  public CwtFloatImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitFloat(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return CwtPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull String toString() {
    return CwtPsiImplUtil.toString(this);
  }

}
