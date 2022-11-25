// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import icu.windea.pls.core.expression.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ParadoxScriptInlineMathImpl extends ParadoxScriptValueImpl implements ParadoxScriptInlineMath {

  public ParadoxScriptInlineMathImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMath(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptInlineMathExpression> getInlineMathExpressionList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptInlineMathExpression.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathFactor getInlineMathFactor() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathFactor.class);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

  @Override
  @NotNull
  public String getExpression() {
    return ParadoxScriptPsiImplUtil.getExpression(this);
  }

}
