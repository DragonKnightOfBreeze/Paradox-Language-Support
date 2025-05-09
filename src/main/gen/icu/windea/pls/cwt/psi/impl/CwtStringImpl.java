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
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;
import javax.swing.Icon;

public class CwtStringImpl extends CwtNamedElementImpl implements CwtString {

  public CwtStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
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
  public @NotNull CwtString setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  public @NotNull PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public @NotNull String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull CwtValue setValue(@NotNull String value) {
    return CwtPsiImplUtil.setValue(this, value);
  }

  @Override
  public @NotNull String getStringValue() {
    return CwtPsiImplUtil.getStringValue(this);
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
