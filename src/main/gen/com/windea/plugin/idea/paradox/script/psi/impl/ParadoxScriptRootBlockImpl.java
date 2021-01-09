// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;
import com.windea.plugin.idea.paradox.script.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public class ParadoxScriptRootBlockImpl extends ParadoxScriptBlockImpl implements ParadoxScriptRootBlock {

  public ParadoxScriptRootBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitRootBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptVariable> getVariableList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptVariable.class);
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
  public boolean isObject() {
    return ParadoxScriptPsiImplUtil.isObject(this);
  }

  @Override
  public boolean isArray() {
    return ParadoxScriptPsiImplUtil.isArray(this);
  }

  @Override
  @NotNull
  public List<PsiElement> getComponents() {
    return ParadoxScriptPsiImplUtil.getComponents(this);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

}
