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
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

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
  public List<CwtDocComment> getDocCommentList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, CwtDocComment.class);
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
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return CwtPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return CwtPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull CwtBlock setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
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
  public @Nullable CwtMemberContainer getMembersRoot() {
    return CwtPsiImplUtil.getMembersRoot(this);
  }

  @Override
  public @NotNull List<@NotNull CwtMember> getMembers() {
    return CwtPsiImplUtil.getMembers(this);
  }

  @Override
  public @NotNull List<@NotNull PsiElement> getComponents() {
    return CwtPsiImplUtil.getComponents(this);
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
  public @NotNull ItemPresentation getPresentation() {
    return CwtPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull String toString() {
    return CwtPsiImplUtil.toString(this);
  }

}
