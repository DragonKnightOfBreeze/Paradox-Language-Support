// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.util.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public class ParadoxScriptInlineParameterConditionImpl extends ASTWrapperPsiElement implements ParadoxScriptInlineParameterCondition {

  public ParadoxScriptInlineParameterConditionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineParameterCondition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptInlineParameterCondition> getInlineParameterConditionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptInlineParameterCondition.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptParameter.class);
  }

  @Override
  @Nullable
  public ParadoxScriptParameterConditionExpression getParameterConditionExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptParameterConditionExpression.class);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @Nullable
  public String getConditionExpression() {
    return ParadoxScriptPsiImplUtil.getConditionExpression(this);
  }

  @Override
  @Nullable
  public String getPresentationText() {
    return ParadoxScriptPsiImplUtil.getPresentationText(this);
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
