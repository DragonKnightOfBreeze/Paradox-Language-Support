// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.util.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

public class ParadoxScriptInlineMathParExpressionImpl extends ParadoxScriptInlineMathExpressionImpl implements ParadoxScriptInlineMathParExpression {

  public ParadoxScriptInlineMathParExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMathParExpression(this);
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
  @NotNull
  public ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  @NotNull
  public GlobalSearchScope getResolveScope() {
    return ParadoxScriptPsiImplUtil.getResolveScope(this);
  }

  @Override
  @NotNull
  public SearchScope getUseScope() {
    return ParadoxScriptPsiImplUtil.getUseScope(this);
  }

}
