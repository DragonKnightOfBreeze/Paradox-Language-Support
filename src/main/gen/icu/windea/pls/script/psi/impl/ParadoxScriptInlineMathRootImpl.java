// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.script.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public class ParadoxScriptInlineMathRootImpl extends ASTWrapperPsiElement implements ParadoxScriptInlineMathRoot {

  public ParadoxScriptInlineMathRootImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMathRoot(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathAbsExpression getInlineMathAbsExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathAbsExpression.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathBiExpression getInlineMathBiExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathBiExpression.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathFactor getInlineMathFactor() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathFactor.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathParExpression getInlineMathParExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathParExpression.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathUnaryExpression getInlineMathUnaryExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathUnaryExpression.class);
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
