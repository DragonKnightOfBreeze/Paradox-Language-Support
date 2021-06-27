// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptTypes.*;
import icu.windea.pls.script.psi.*;
import icu.windea.pls.script.reference.ParadoxScriptVariableReferenceReference;

public class ParadoxScriptVariableReferenceImpl extends ParadoxScriptValueImpl implements ParadoxScriptVariableReference {

  public ParadoxScriptVariableReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitVariableReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getVariableReferenceId() {
    return notNullChild(findChildByType(VARIABLE_REFERENCE_ID));
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxScriptVariableReference setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public ParadoxScriptVariableReferenceReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @Nullable
  public ParadoxScriptValue getReferenceValue() {
    return ParadoxScriptPsiImplUtil.getReferenceValue(this);
  }

}
