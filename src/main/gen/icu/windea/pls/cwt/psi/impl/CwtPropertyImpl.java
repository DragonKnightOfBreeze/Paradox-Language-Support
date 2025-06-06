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
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.CwtSeparatorType;
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
  public CwtValue getPropertyValue() {
    return findChildByClass(CwtValue.class);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return CwtPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return CwtPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull CwtProperty setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  public @NotNull PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public @Nullable String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull CwtSeparatorType getSeparatorType() {
    return CwtPsiImplUtil.getSeparatorType(this);
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
