// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.core.references.ParadoxParameterPsiReference;
import javax.swing.Icon;

public class ParadoxScriptParameterConditionParameterImpl extends ASTWrapperPsiElement implements ParadoxScriptParameterConditionParameter {

  public ParadoxScriptParameterConditionParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitParameterConditionParameter(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
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
  public ParadoxScriptParameterConditionParameter setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public int getTextOffset() {
    return ParadoxScriptPsiImplUtil.getTextOffset(this);
  }

  @Override
  @NotNull
  public ParadoxParameterPsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

}
