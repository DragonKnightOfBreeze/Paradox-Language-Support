// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import icu.windea.pls.cwt.psi.*;
import icu.windea.pls.cwt.psi.util.CwtPsiImplUtil;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.CwtSeparatorType;
import javax.swing.Icon;

public class CwtOptionImpl extends CwtNamedElementImpl implements CwtOption {

  public CwtOptionImpl(@NotNull ASTNode node) {
    super(node);
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
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return CwtPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return CwtPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull CwtOption setName(@NotNull String name) {
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
