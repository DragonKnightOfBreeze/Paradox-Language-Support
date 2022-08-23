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
import com.intellij.psi.PsiReference;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.expression.reference.ParadoxScriptValueReference;

public class ParadoxScriptStringImpl extends ParadoxScriptValueImpl implements ParadoxScriptString {

  public ParadoxScriptStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptParameter.class);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxScriptString setName(@NotNull String value) {
    return ParadoxScriptPsiImplUtil.setName(this, value);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxScriptString setValue(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setValue(this, name);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxScriptValueReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return ParadoxScriptPsiImplUtil.getReferences(this);
  }

  @Override
  @NotNull
  public String getStringValue() {
    return ParadoxScriptPsiImplUtil.getStringValue(this);
  }

  @Override
  @NotNull
  public ParadoxValueType getValueType() {
    return ParadoxScriptPsiImplUtil.getValueType(this);
  }

}
