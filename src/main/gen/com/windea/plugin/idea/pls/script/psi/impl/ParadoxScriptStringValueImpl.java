// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.windea.plugin.idea.pls.script.psi.*;

public abstract class ParadoxScriptStringValueImpl extends ParadoxScriptValueImpl implements ParadoxScriptStringValue {

  public ParadoxScriptStringValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitStringValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

}
