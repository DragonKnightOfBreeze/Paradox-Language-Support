// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.pls.cwt.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public abstract class CwtValueImpl extends ASTWrapperPsiElement implements CwtValue {

  public CwtValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return CwtPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public String getTruncatedValue() {
    return CwtPsiImplUtil.getTruncatedValue(this);
  }

}
