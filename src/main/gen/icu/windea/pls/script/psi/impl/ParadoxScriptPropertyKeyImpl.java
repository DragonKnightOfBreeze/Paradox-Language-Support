// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import com.intellij.lang.*;
import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.stubs.*;
import com.intellij.psi.util.*;
import icu.windea.pls.core.expression.*;
import icu.windea.pls.script.psi.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ParadoxScriptPropertyKeyImpl extends ParadoxScriptStubElementImpl<ParadoxScriptPropertyKeyStub> implements ParadoxScriptPropertyKey {

  public ParadoxScriptPropertyKeyImpl(@NotNull ParadoxScriptPropertyKeyStub stub, @Nullable IStubElementType<?, ?> type) {
    super(stub, type);
  }

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
  @NotNull
  public List<ParadoxScriptParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptParameter.class);
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
  public PsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return ParadoxScriptPsiImplUtil.getReferences(this);
  }

  @Override
  @NotNull
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

  @Override
  @Nullable
  public String getConfigExpression() {
    return ParadoxScriptPsiImplUtil.getConfigExpression(this);
  }

  @Override
  @Nullable
  public ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
  }

  @Override
  @NotNull
  public String toString() {
    return ParadoxScriptPsiImplUtil.toString(this);
  }

}
