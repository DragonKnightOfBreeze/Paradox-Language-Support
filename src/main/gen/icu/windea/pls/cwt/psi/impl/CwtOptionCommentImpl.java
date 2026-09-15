// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.cwt.psi.CwtOption;
import icu.windea.pls.cwt.psi.CwtOptionComment;
import icu.windea.pls.cwt.psi.CwtValue;
import icu.windea.pls.cwt.psi.CwtVisitor;
import org.jetbrains.annotations.NotNull;

import java.util.List;

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
  public @NotNull IElementType getTokenType() {
    return CwtPsiImplUtil.getTokenType(this);
  }

  @Override
  @NotNull
  public List<CwtOption> getOptionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtOption.class);
  }

  @Override
  @NotNull
  public List<CwtValue> getOptionValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtValue.class);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return CwtPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return CwtPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull String toString() {
    return CwtPsiImplUtil.toString(this);
  }

}
