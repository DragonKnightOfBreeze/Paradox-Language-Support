// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.script.reference.ParadoxScriptPropertyKeyReference;

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
  public PsiElement getPropertyKeyId() {
    return findChildByType(PROPERTY_KEY_ID);
  }

  @Override
  @Nullable
  public PsiElement getQuotedPropertyKeyId() {
    return findChildByType(QUOTED_PROPERTY_KEY_ID);
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
  @NotNull
  public ParadoxScriptPropertyKeyReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

}
