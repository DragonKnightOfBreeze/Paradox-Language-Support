// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.paradox.script.psi.*;

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

}
