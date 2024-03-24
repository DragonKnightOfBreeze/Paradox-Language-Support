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

public class ParadoxScriptParameterConditionImpl extends ASTWrapperPsiElement implements ParadoxScriptParameterCondition {

  public ParadoxScriptParameterConditionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitParameterCondition(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptParameterCondition> getParameterConditionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptParameterCondition.class);
  }

  @Override
  @Nullable
  public ParadoxScriptParameterConditionExpression getParameterConditionExpression() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptParameterConditionExpression.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptProperty.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptValue.class);
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
  public boolean isEmpty() {
    return ParadoxScriptPsiImplUtil.isEmpty(this);
  }

  @Override
  public boolean isNotEmpty() {
    return ParadoxScriptPsiImplUtil.isNotEmpty(this);
  }

  @Override
  @NotNull
  public List<PsiElement> getComponents() {
    return ParadoxScriptPsiImplUtil.getComponents(this);
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
