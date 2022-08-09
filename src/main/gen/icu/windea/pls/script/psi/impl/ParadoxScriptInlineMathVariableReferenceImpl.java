// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElementVisitor;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.script.reference.ParadoxScriptedVariableReference;
import javax.swing.Icon;

public class ParadoxScriptInlineMathVariableReferenceImpl extends ParadoxScriptInlineMathFactorImpl implements ParadoxScriptInlineMathVariableReference {

  public ParadoxScriptInlineMathVariableReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitInlineMathVariableReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxScriptInlineMathVariableReference setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public ParadoxScriptedVariableReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

}
