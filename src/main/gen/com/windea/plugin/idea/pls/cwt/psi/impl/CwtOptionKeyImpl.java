// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.pls.cwt.psi.CwtTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.windea.plugin.idea.pls.cwt.psi.*;

public class CwtOptionKeyImpl extends ASTWrapperPsiElement implements CwtOptionKey {

  public CwtOptionKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitOptionKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getOptionKeyToken() {
    return findNotNullChildByType(OPTION_KEY_TOKEN);
  }

  @Override
  @NotNull
  public String getName() {
    return CwtPsiImplUtil.getName(this);
  }

}
