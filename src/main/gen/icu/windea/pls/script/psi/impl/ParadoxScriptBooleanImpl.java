// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import icu.windea.pls.core.expression.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

public class ParadoxScriptBooleanImpl extends ParadoxScriptValueImpl implements ParadoxScriptBoolean {

  public ParadoxScriptBooleanImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitBoolean(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public boolean getBooleanValue() {
    return ParadoxScriptPsiImplUtil.getBooleanValue(this);
  }

  @Override
  @NotNull
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

}
