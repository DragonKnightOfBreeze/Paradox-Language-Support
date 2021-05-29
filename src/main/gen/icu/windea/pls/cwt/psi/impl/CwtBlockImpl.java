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

public class CwtBlockImpl extends CwtValueImpl implements CwtBlock {

  public CwtBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitBlock(this);
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
  public List<CwtOption> getOptionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtOption.class);
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
  @NotNull
  public String getTruncatedValue() {
    return CwtPsiImplUtil.getTruncatedValue(this);
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
  public boolean isObject() {
    return CwtPsiImplUtil.isObject(this);
  }

  @Override
  public boolean isArray() {
    return CwtPsiImplUtil.isArray(this);
  }

  @Override
  @NotNull
  public List<PsiElement> getComponents() {
    return CwtPsiImplUtil.getComponents(this);
  }

}
