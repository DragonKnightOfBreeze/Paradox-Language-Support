// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static com.windea.plugin.idea.pls.script.psi.ParadoxScriptTypes.*;
import com.windea.plugin.idea.pls.script.psi.*;
import com.windea.plugin.idea.pls.script.reference.ParadoxScriptStringPropertyPsiReference;

public class ParadoxScriptStringImpl extends ParadoxScriptStringValueImpl implements ParadoxScriptString {

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
  @Nullable
  public PsiElement getQuotedStringToken() {
    return findChildByType(QUOTED_STRING_TOKEN);
  }

  @Override
  @Nullable
  public PsiElement getStringToken() {
    return findChildByType(STRING_TOKEN);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public PsiElement setValue(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setValue(this, name);
  }

  @Override
  @NotNull
  public ParadoxScriptStringPropertyPsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

}
