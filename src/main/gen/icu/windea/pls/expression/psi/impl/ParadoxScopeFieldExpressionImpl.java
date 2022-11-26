// This is a generated file. Not intended for manual editing.
package icu.windea.pls.expression.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.expression.psi.ParadoxExpressionElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.expression.psi.*;

public class ParadoxScopeFieldExpressionImpl extends ASTWrapperPsiElement implements ParadoxScopeFieldExpression {

  public ParadoxScopeFieldExpressionImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxExpressionVisitor visitor) {
    visitor.visitScopeFieldExpression(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxExpressionVisitor) accept((ParadoxExpressionVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScopeField> getScopeFieldList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScopeField.class);
  }

}
