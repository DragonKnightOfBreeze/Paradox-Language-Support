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
import icu.windea.pls.model.ParadoxValueType;

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
  public ParadoxValueType getValueType() {
    return ParadoxScriptPsiImplUtil.getValueType(this);
  }

}
