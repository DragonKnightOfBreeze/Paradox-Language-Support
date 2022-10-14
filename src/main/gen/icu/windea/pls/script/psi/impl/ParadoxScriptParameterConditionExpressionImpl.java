// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.extapi.psi.*;
import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

public class ParadoxScriptParameterConditionExpressionImpl extends ASTWrapperPsiElement implements ParadoxScriptParameterConditionExpression {

  public ParadoxScriptParameterConditionExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitParameterConditionExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptParameterConditionParameter getParameterConditionParameter() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptParameterConditionParameter.class));
  }

}
