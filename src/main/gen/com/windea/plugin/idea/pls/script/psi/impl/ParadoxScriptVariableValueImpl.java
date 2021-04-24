// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.pls.script.psi.*;

public class ParadoxScriptVariableValueImpl extends ASTWrapperPsiElement implements ParadoxScriptVariableValue {

  public ParadoxScriptVariableValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitVariableValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptValue getValue() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptValue.class));
  }

}
