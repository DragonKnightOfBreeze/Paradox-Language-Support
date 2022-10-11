// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import icu.windea.pls.core.model.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

public class ParadoxScriptIntImpl extends ParadoxScriptValueImpl implements ParadoxScriptInt {

  public ParadoxScriptIntImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInt(this);
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

  @Override
  public int getIntValue() {
    return ParadoxScriptPsiImplUtil.getIntValue(this);
  }

  @Override
  @NotNull
  public ParadoxValueType getValueType() {
    return ParadoxScriptPsiImplUtil.getValueType(this);
  }

}
