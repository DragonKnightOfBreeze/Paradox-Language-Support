// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariable;
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableName;
import icu.windea.pls.script.psi.ParadoxScriptValue;
import icu.windea.pls.script.psi.ParadoxScriptVisitor;
import icu.windea.pls.script.psi.stubs.ParadoxScriptScriptedVariableStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public class ParadoxScriptScriptedVariableImpl extends ParadoxScriptStubElementImpl<ParadoxScriptScriptedVariableStub> implements ParadoxScriptScriptedVariable {

  public ParadoxScriptScriptedVariableImpl(@NotNull ParadoxScriptScriptedVariableStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptScriptedVariableImpl(@NotNull ParadoxScriptScriptedVariableStub stub, @NotNull IElementType type) {
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
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @Nullable String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxScriptScriptedVariable setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxScriptPsiImplUtil.getTextOffset(this);
  }

  @Override
  public @Nullable String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ParadoxScriptPsiImplUtil.getPresentableText(this);
  }

  @Override
  public @NotNull IElementType getIElementType() {
    return ParadoxScriptPsiImplUtil.getIElementType(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
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
