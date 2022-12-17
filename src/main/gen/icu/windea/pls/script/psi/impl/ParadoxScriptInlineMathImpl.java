// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.core.expression.ParadoxDataType;

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
  public ParadoxDataType getType() {
    return ParadoxScriptPsiImplUtil.getType(this);
  }

  @Override
  @NotNull
  public String getExpression() {
    return ParadoxScriptPsiImplUtil.getExpression(this);
  }

}
