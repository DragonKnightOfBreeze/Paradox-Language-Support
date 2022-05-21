// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.script.psi.*;

public class ParadoxScriptInlineMathFactorImpl extends ASTWrapperPsiElement implements ParadoxScriptInlineMathFactor {

  public ParadoxScriptInlineMathFactorImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMathFactor(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathNumber getInlineMathNumber() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathNumber.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathParameter getInlineMathParameter() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathParameter.class);
  }

  @Override
  @Nullable
  public ParadoxScriptInlineMathVariableReference getInlineMathVariableReference() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptInlineMathVariableReference.class);
  }

}
