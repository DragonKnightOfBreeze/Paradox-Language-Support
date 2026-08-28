// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.script.psi.ParadoxScriptConditionalExpression;
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock;
import icu.windea.pls.script.psi.ParadoxScriptNormalParameter;
import icu.windea.pls.script.psi.ParadoxScriptVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ParadoxScriptInlineConditionalBlockImpl extends ASTWrapperPsiElement implements ParadoxScriptInlineConditionalBlock {

  public ParadoxScriptInlineConditionalBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineConditionalBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptConditionalExpression getConditionalExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptConditionalExpression.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptNormalParameter> getNormalParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptNormalParameter.class);
  }

  @Override
  public @Nullable PsiElement getLeftBound() {
    return ParadoxScriptPsiImplUtil.getLeftBound(this);
  }

  @Override
  public @Nullable PsiElement getRightBound() {
    return ParadoxScriptPsiImplUtil.getRightBound(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ParadoxScriptPsiImplUtil.getPresentableText(this);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxScriptPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxScriptPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxScriptPsiImplUtil.toString(this);
  }

}
