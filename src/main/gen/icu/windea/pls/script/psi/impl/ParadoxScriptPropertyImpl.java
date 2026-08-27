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
import icu.windea.pls.script.psi.*;
import icu.windea.pls.script.psi.stubs.ParadoxScriptPropertyStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ParadoxScriptPropertyImpl extends ParadoxScriptStubElementImpl<ParadoxScriptPropertyStub> implements ParadoxScriptProperty {

  public ParadoxScriptPropertyImpl(@NotNull ParadoxScriptPropertyStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptPropertyImpl(@NotNull ParadoxScriptPropertyStub stub, @NotNull IElementType type) {
    super(stub, type);
  }

  public ParadoxScriptPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @Nullable ParadoxScriptBlock getMemberContainer() {
    return ParadoxScriptPsiImplUtil.getMemberContainer(this);
  }

  @Override
  public @Nullable List<@NotNull ParadoxScriptMember> getMembers() {
    return ParadoxScriptPsiImplUtil.getMembers(this);
  }

  @Override
  public @Nullable ParadoxScriptBlock getBlock() {
    return ParadoxScriptPsiImplUtil.getBlock(this);
  }

  @Override
  @NotNull
  public ParadoxScriptPropertyKey getPropertyKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptPropertyKey.class));
  }

  @Override
  @Nullable
  public ParadoxScriptValue getPropertyValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptValue.class);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxScriptProperty setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
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
