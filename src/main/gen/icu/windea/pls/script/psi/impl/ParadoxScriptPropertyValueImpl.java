// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.script.psi.*;

public class ParadoxScriptPropertyValueImpl extends ASTWrapperPsiElement implements ParadoxScriptPropertyValue {

  public ParadoxScriptPropertyValueImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitPropertyValue(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptBlock getBlock() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptBlock.class);
  }

  @Override
  @Nullable
  public ParadoxScriptBoolean getBoolean() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptBoolean.class);
  }

  @Override
  @Nullable
  public ParadoxScriptColor getColor() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptColor.class);
  }

  @Override
  @Nullable
  public ParadoxScriptFloat getFloat() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptFloat.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMath getInlineMath() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMath.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInt getInt() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInt.class);
  }

  @Override
  @Nullable
  public ParadoxScriptString getString() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptString.class);
  }

  @Override
  @Nullable
  public ParadoxScriptVariableReference getVariableReference() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptVariableReference.class);
  }

  @Override
  @NotNull
  public ParadoxScriptValue getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

}
