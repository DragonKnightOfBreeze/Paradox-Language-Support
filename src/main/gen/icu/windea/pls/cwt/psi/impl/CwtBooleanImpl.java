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
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;

public class CwtBooleanImpl extends CwtValueImpl implements CwtBoolean {

  public CwtBooleanImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public boolean getBooleanValue() {
    return CwtPsiImplUtil.getBooleanValue(this);
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
