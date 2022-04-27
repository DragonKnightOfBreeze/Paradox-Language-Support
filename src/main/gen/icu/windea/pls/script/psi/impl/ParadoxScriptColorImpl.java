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
