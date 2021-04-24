// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.windea.plugin.idea.pls.script.psi.*;

public class ParadoxScriptNumberImpl extends ParadoxScriptValueImpl implements ParadoxScriptNumber {

  public ParadoxScriptNumberImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitNumber(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptFloat getFloat() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptFloat.class);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

}
