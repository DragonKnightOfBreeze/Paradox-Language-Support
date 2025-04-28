// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
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
  public CwtString setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public CwtString setValue(@NotNull String value) {
    return CwtPsiImplUtil.setValue(this, value);
  }

  @Override
  @NotNull
  public String getStringValue() {
    return CwtPsiImplUtil.getStringValue(this);
  }

  @Override
  @NotNull
  public CwtType getType() {
    return CwtPsiImplUtil.getType(this);
  }

  @Override
  @Nullable
  public CwtConfigType getConfigType() {
    return CwtPsiImplUtil.getConfigType(this);
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
