// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.cwt.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;
import javax.swing.Icon;

public class CwtPropertyKeyImpl extends ASTWrapperPsiElement implements CwtPropertyKey {

  public CwtPropertyKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitPropertyKey(this);
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
  public @NotNull String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull CwtPropertyKey setValue(@NotNull String value) {
    return CwtPsiImplUtil.setValue(this, value);
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
