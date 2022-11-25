// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.*;
import icu.windea.pls.core.expression.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class ParadoxScriptScriptedVariableImpl extends ParadoxScriptStubElementImpl<ParadoxScriptScriptedVariableStub> implements ParadoxScriptScriptedVariable {

  public ParadoxScriptScriptedVariableImpl(@NotNull ParadoxScriptScriptedVariableStub stub, @Nullable IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptScriptedVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitScriptedVariable(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptScriptedVariableName getScriptedVariableName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptScriptedVariableName.class));
  }

  @Override
  @Nullable
  public ParadoxScriptValue getScriptedVariableValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptValue.class);
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
  public ParadoxScriptScriptedVariable setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxScriptPsiImplUtil.getTextOffset(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @Nullable
  public String getUnquotedValue() {
    return ParadoxScriptPsiImplUtil.getUnquotedValue(this);
  }

  @Override
  @Nullable
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
  }

  @Override
  @NotNull
  public String toString() {
    return ParadoxScriptPsiImplUtil.toString(this);
  }

}
