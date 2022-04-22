// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import icu.windea.pls.cwt.psi.*;
import icu.windea.pls.config.cwt.config.CwtSeparatorType;
import javax.swing.Icon;

public class CwtOptionImpl extends CwtNamedElementImpl implements CwtOption {

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
  public CwtValue getValue() {
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
  @NotNull
  public CwtOption setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public String getOptionName() {
    return CwtPsiImplUtil.getOptionName(this);
  }

  @Override
  @NotNull
  public String getOptionValue() {
    return CwtPsiImplUtil.getOptionValue(this);
  }

  @Override
  @NotNull
  public String getOptionTruncatedValue() {
    return CwtPsiImplUtil.getOptionTruncatedValue(this);
  }

  @Override
  @NotNull
  public CwtSeparatorType getSeparatorType() {
    return CwtPsiImplUtil.getSeparatorType(this);
  }

}
