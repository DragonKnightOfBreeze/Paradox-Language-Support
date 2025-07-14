// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import icu.windea.pls.script.psi.ParadoxScriptScriptedVariableStub;
import icu.windea.pls.script.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxScriptScriptedVariableImpl extends ParadoxScriptStubElementImpl<ParadoxScriptScriptedVariableStub> implements ParadoxScriptScriptedVariable {

  public ParadoxScriptScriptedVariableImpl(@NotNull ParadoxScriptScriptedVariableStub stub, @Nullable IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptScriptedVariableImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitScriptedVariable(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptScriptedVariableName getScriptedVariableName() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxScriptScriptedVariableName.class));
  }

  @Override
  @Nullable
  public ParadoxScriptValue getScriptedVariableValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptValue.class);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @Nullable String getName() {
    return ParadoxScriptPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxScriptScriptedVariable setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  public @Nullable PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxScriptPsiImplUtil.getTextOffset(this);
  }

  @Override
  public @Nullable String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public @Nullable String getUnquotedValue() {
    return ParadoxScriptPsiImplUtil.getUnquotedValue(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxScriptPsiImplUtil.toString(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxScriptPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxScriptPsiImplUtil.getUseScope(this);
  }

}
