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
import icu.windea.pls.core.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptKeyReference;

public class ParadoxScriptPropertyKeyImpl extends ASTWrapperPsiElement implements ParadoxScriptPropertyKey {

  public ParadoxScriptPropertyKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitPropertyKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptKeyStringTemplate getKeyStringTemplate() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptKeyStringTemplate.class);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxScriptPropertyKey setValue(@NotNull String value) {
    return ParadoxScriptPsiImplUtil.setValue(this, value);
  }

  @Override
  @Nullable
  public ParadoxScriptKeyReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @Nullable
  public String getConfigExpression() {
    return ParadoxScriptPsiImplUtil.getConfigExpression(this);
  }

  @Override
  @NotNull
  public ParadoxValueType getValueType() {
    return ParadoxScriptPsiImplUtil.getValueType(this);
  }

}
