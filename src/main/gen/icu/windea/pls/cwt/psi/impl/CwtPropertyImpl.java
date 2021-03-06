// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtTypes.*;
import icu.windea.pls.cwt.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.model.SeparatorType;
import javax.swing.Icon;

public class CwtPropertyImpl extends CwtNamedElementImpl implements CwtProperty {

  public CwtPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public CwtPropertyKey getPropertyKey() {
    return findNotNullChildByClass(CwtPropertyKey.class);
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
  public CwtProperty setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public String getPropertyName() {
    return CwtPsiImplUtil.getPropertyName(this);
  }

  @Override
  @NotNull
  public String getPropertyValue() {
    return CwtPsiImplUtil.getPropertyValue(this);
  }

  @Override
  @NotNull
  public String getPropertyTruncatedValue() {
    return CwtPsiImplUtil.getPropertyTruncatedValue(this);
  }

  @Override
  @NotNull
  public SeparatorType getSeparatorType() {
    return CwtPsiImplUtil.getSeparatorType(this);
  }

}
