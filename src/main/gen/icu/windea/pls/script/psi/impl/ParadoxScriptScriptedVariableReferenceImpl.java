// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.script.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

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
  public @NotNull ParadoxScriptScriptedVariableReference setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public @NotNull String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull ParadoxScriptValue setValue(@NotNull String value) {
    return ParadoxScriptPsiImplUtil.setValue(this, value);
  }

  @Override
  public @NotNull String getExpression() {
    return ParadoxScriptPsiImplUtil.getExpression(this);
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
