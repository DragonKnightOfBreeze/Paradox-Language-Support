// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import com.intellij.psi.util.*;
import icu.windea.pls.core.references.*;
import icu.windea.pls.model.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public class ParadoxScriptScriptedVariableReferenceImpl extends ParadoxScriptValueImpl implements ParadoxScriptScriptedVariableReference {

  public ParadoxScriptScriptedVariableReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitScriptedVariableReference(this);
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
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxScriptScriptedVariableReference setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @Nullable
  public ParadoxScriptedVariablePsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public ParadoxType getType() {
    return ParadoxScriptPsiImplUtil.getType(this);
  }

  @Override
  @NotNull
  public String getExpression() {
    return ParadoxScriptPsiImplUtil.getExpression(this);
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
