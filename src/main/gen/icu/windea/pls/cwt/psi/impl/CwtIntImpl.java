// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import icu.windea.pls.cwt.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;

public class CwtIntImpl extends CwtValueImpl implements CwtInt {

  public CwtIntImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitInt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public int getIntValue() {
    return CwtPsiImplUtil.getIntValue(this);
  }

  @Override
  public @NotNull CwtType getType() {
    return CwtPsiImplUtil.getType(this);
  }

  @Override
  public @Nullable CwtConfigType getConfigType() {
    return CwtPsiImplUtil.getConfigType(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

}
