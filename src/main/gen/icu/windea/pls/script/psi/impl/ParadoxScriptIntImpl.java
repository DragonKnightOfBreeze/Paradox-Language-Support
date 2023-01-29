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
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxScriptValue setValue(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setValue(this, name);
  }

  @Override
  public int getIntValue() {
    return ParadoxScriptPsiImplUtil.getIntValue(this);
  }

  @Override
  @NotNull
  public ParadoxDataType getType() {
    return ParadoxScriptPsiImplUtil.getType(this);
  }

}
