// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.script.psi.ParadoxScriptInlineMath;
import icu.windea.pls.script.psi.ParadoxScriptInlineMathExpression;
import icu.windea.pls.script.psi.ParadoxScriptVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public class ParadoxScriptInlineMathImpl extends ParadoxScriptValueImpl implements ParadoxScriptInlineMath {

  public ParadoxScriptInlineMathImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMath(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ParadoxScriptPsiImplUtil.getPresentableText(this);
  }

  @Override
  public @Nullable PsiElement getTokenElement() {
    return ParadoxScriptPsiImplUtil.getTokenElement(this);
  }

  @Override
  public @Nullable ParadoxScriptInlineMathExpression getInlineMathExpression() {
    return ParadoxScriptPsiImplUtil.getInlineMathExpression(this);
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
