// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.tree.*;
import icu.windea.pls.cwt.psi.*;
import org.jetbrains.annotations.*;

public class CwtOptionCommentImpl extends ASTWrapperPsiElement implements CwtOptionComment {

  public CwtOptionCommentImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitOptionComment(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public CwtOption getOption() {
    return findChildByClass(CwtOption.class);
  }

  @Override
  @Nullable
  public CwtValue getValue() {
    return findChildByClass(CwtValue.class);
  }

  @Override
  @NotNull
  public IElementType getTokenType() {
    return CwtPsiImplUtil.getTokenType(this);
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
