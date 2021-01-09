// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.script.psi.ParadoxScriptTypes.*;
import com.windea.plugin.idea.paradox.script.psi.ParadoxScriptVariableStub;
import com.windea.plugin.idea.paradox.script.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxScriptVariableImpl extends ParadoxScriptStubElementImpl<ParadoxScriptVariableStub> implements ParadoxScriptVariable {

  public ParadoxScriptVariableImpl(@NotNull ParadoxScriptVariableStub stub, @Nullable IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public ParadoxScriptVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitVariable(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptVariableName getVariableName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptVariableName.class));
  }

  @Override
  @Nullable
  public ParadoxScriptVariableValue getVariableValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptVariableValue.class);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @Nullable
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @Nullable
  public String getUnquotedValue() {
    return ParadoxScriptPsiImplUtil.getUnquotedValue(this);
  }

}
