// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;
import com.windea.plugin.idea.paradox.script.psi.*;
import java.awt.Color;

public class ParadoxScriptColorImpl extends ParadoxScriptStringValueImpl implements ParadoxScriptColor {

  public ParadoxScriptColorImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitColor(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getColorToken() {
    return notNullChild(findChildByType(COLOR_TOKEN));
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @Nullable
  public Color getColor() {
    return ParadoxScriptPsiImplUtil.getColor(this);
  }

  @Override
  public void setColor(@NotNull Color color) {
    ParadoxScriptPsiImplUtil.setColor(this, color);
  }

}
