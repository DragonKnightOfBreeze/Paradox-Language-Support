// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.script.psi.ParadoxScriptValue;
import icu.windea.pls.script.psi.ParadoxScriptVisitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public abstract class ParadoxScriptValueImpl extends ASTWrapperPsiElement implements ParadoxScriptValue {

  public ParadoxScriptValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
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
  public @NotNull ParadoxScriptValue setValue(@NotNull String value) {
    return ParadoxScriptPsiImplUtil.setValue(this, value);
  }

  @Override
  public @NotNull ParadoxScriptValue setContent(@NotNull String content, @NotNull TextRange range) {
    return ParadoxScriptPsiImplUtil.setContent(this, content, range);
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
