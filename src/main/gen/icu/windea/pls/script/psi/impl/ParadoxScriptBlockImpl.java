// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ParadoxScriptBlockImpl extends ParadoxScriptValueImpl implements ParadoxScriptBlock {

  public ParadoxScriptBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptConditionalBlock> getConditionalBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptConditionalBlock.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptProperty.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptScriptedVariable> getScriptedVariableList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptScriptedVariable.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptValue.class);
  }

  @Override
  public @NotNull ParadoxScriptBlock getMemberContainer() {
    return ParadoxScriptPsiImplUtil.getMemberContainer(this);
  }

  @Override
  public @NotNull List<@NotNull ParadoxScriptMember> getMembers() {
    return ParadoxScriptPsiImplUtil.getMembers(this);
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
  public @NotNull List<@NotNull ParadoxScriptStatement> getComponents() {
    return ParadoxScriptPsiImplUtil.getComponents(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
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
  public @Nullable PsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return ParadoxScriptPsiImplUtil.getReferences(this);
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
