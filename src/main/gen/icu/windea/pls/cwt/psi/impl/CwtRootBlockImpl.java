// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.util.*;
import icu.windea.pls.cwt.psi.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class CwtRootBlockImpl extends ASTWrapperPsiElement implements CwtRootBlock {

  public CwtRootBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitRootBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<CwtDocumentationComment> getDocumentationCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtDocumentationComment.class);
  }

  @Override
  @NotNull
  public List<CwtOptionComment> getOptionCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtOptionComment.class);
  }

  @Override
  @NotNull
  public List<CwtProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtProperty.class);
  }

  @Override
  @NotNull
  public List<CwtValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtValue.class);
  }

  @Override
  @NotNull
  public String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  public boolean isEmpty() {
    return CwtPsiImplUtil.isEmpty(this);
  }

  @Override
  public boolean isNotEmpty() {
    return CwtPsiImplUtil.isNotEmpty(this);
  }

  @Override
  @NotNull
  public List<PsiElement> getComponents() {
    return CwtPsiImplUtil.getComponents(this);
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
