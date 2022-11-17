// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.psi.*;
import icu.windea.pls.cwt.*;
import icu.windea.pls.cwt.psi.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class CwtOptionImpl extends ASTWrapperPsiElement implements CwtOption {

  public CwtOptionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitOption(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public CwtOptionKey getOptionKey() {
    return findNotNullChildByClass(CwtOptionKey.class);
  }

  @Override
  @Nullable
  public CwtValue getOptionValue() {
    return findChildByClass(CwtValue.class);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return CwtPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getName() {
    return CwtPsiImplUtil.getName(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public CwtSeparatorType getSeparatorType() {
    return CwtPsiImplUtil.getSeparatorType(this);
  }

}
