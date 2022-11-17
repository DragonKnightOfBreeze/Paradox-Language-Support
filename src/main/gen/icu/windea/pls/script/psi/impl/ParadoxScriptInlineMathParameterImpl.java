// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.script.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public class ParadoxScriptInlineMathParameterImpl extends ParadoxScriptInlineMathFactorImpl implements ParadoxScriptInlineMathParameter {

  public ParadoxScriptInlineMathParameterImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMathParameter(this);
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
  @Nullable
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxScriptInlineMathParameter setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public int getTextOffset() {
    return ParadoxScriptPsiImplUtil.getTextOffset(this);
  }

  @Override
  @Nullable
  public String getDefaultValue() {
    return ParadoxScriptPsiImplUtil.getDefaultValue(this);
  }

  @Override
  @Nullable
  public ParadoxParameterPsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

}
