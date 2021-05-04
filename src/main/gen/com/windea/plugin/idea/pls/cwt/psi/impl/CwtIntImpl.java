// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*;
import com.windea.plugin.idea.pls.cwt.psi.*;

public class CwtIntImpl extends CwtNumberImpl implements CwtInt {

  public CwtIntImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitInt(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getIntToken() {
    return findNotNullChildByType(INT_TOKEN);
  }

  @Override
  public int getIntValue() {
    return CwtPsiImplUtil.getIntValue(this);
  }

}
