// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.cwt.psi.*;
import org.jetbrains.annotations.*;

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
  public float getFloatValue() {
    return CwtPsiImplUtil.getFloatValue(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  @NotNull
  public SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

}
