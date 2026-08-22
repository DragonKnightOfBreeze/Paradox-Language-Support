// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.script.psi.ParadoxScriptInlineConditionalBlock;
import icu.windea.pls.script.psi.ParadoxScriptPropertyKey;
import icu.windea.pls.script.psi.ParadoxScriptVisitor;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ParadoxScriptPropertyKeyImpl extends ASTWrapperPsiElement implements ParadoxScriptPropertyKey {

  public ParadoxScriptPropertyKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitPropertyKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptInlineConditionalBlock> getInlineConditionalBlockList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptInlineConditionalBlock.class);
  }

  @Override
  public @Nullable PsiElement getIdElement() {
    return ParadoxScriptPsiImplUtil.getIdElement(this);
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
  public @NotNull String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull String getPresentableText() {
    return ParadoxScriptPsiImplUtil.getPresentableText(this);
  }

  @Override
  public @NotNull ParadoxScriptPropertyKey setValue(@NotNull String value) {
    return ParadoxScriptPsiImplUtil.setValue(this, value);
  }

  @Override
  public @NotNull ParadoxScriptPropertyKey setContent(@NotNull String content, @NotNull TextRange range) {
    return ParadoxScriptPsiImplUtil.setContent(this, content, range);
  }

  @Override
  public boolean needQuote(@NotNull String text) {
    return ParadoxScriptPsiImplUtil.needQuote(this);
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
