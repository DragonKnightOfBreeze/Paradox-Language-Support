// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import icu.windea.pls.script.psi.ParadoxScriptStringStub;
import icu.windea.pls.script.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import icu.windea.pls.script.exp.ParadoxDataType;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxScriptStringImpl extends ParadoxScriptStubElementImpl<ParadoxScriptStringStub> implements ParadoxScriptString {

  public ParadoxScriptStringImpl(@NotNull ParadoxScriptStringStub stub, @Nullable IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptParameter> getParameterList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptParameter.class);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxScriptPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxScriptString setValue(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setValue(this, name);
  }

  @Override
  @Nullable
  public PsiReference getReference() {
    return ParadoxScriptPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return ParadoxScriptPsiImplUtil.getReferences(this);
  }

  @Override
  @NotNull
  public String getStringValue() {
    return ParadoxScriptPsiImplUtil.getStringValue(this);
  }

  @Override
  @NotNull
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

  @Override
  @Nullable
  public String getConfigExpression() {
    return ParadoxScriptPsiImplUtil.getConfigExpression(this);
  }

  @Override
  @Nullable
  public ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
  }

}
