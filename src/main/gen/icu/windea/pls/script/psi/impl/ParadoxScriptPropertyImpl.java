// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.script.psi.ParadoxScriptElementTypes.*;
import icu.windea.pls.script.psi.ParadoxScriptPropertyStub;
import icu.windea.pls.script.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.SmartPsiElementPointer;
import icu.windea.pls.core.expression.ParadoxDataType;
import java.util.Map;
import java.util.Set;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxScriptPropertyImpl extends ParadoxScriptStubElementImpl<ParadoxScriptPropertyStub> implements ParadoxScriptProperty {

  public ParadoxScriptPropertyImpl(@NotNull ParadoxScriptPropertyStub stub, @Nullable IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxScriptPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxScriptPropertyKey getPropertyKey() {
    return notNullChild(PsiTreeUtil.getStubChildOfType(this, ParadoxScriptPropertyKey.class));
  }

  @Override
  @Nullable
  public ParadoxScriptPropertyValue getPropertyValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxScriptPropertyValue.class);
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
  public ParadoxScriptProperty setName(@NotNull String name) {
    return ParadoxScriptPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxScriptPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  public int getDepth() {
    return ParadoxScriptPsiImplUtil.getDepth(this);
  }

  @Override
  @Nullable
  public ParadoxScriptBlock getBlock() {
    return ParadoxScriptPsiImplUtil.getBlock(this);
  }

  @Override
  @Nullable
  public String getDefinitionType() {
    return ParadoxScriptPsiImplUtil.getDefinitionType(this);
  }

  @Override
  @Nullable
  public ParadoxDataType getExpressionType() {
    return ParadoxScriptPsiImplUtil.getExpressionType(this);
  }

  @Override
  @Nullable
  public String getConfigExpression() {
    return ParadoxScriptPsiImplUtil.getConfigExpression(this);
  }

  @Override
  @NotNull
  public String getExpression() {
    return ParadoxScriptPsiImplUtil.getExpression(this);
  }

  @Override
  @Nullable
  public String getPathName() {
    return ParadoxScriptPsiImplUtil.getPathName(this);
  }

  @Override
  @NotNull
  public String getOriginalPathName() {
    return ParadoxScriptPsiImplUtil.getOriginalPathName(this);
  }

  @Override
  @NotNull
  public Map<String, Set<SmartPsiElementPointer<ParadoxParameter>>> getParameterMap() {
    return ParadoxScriptPsiImplUtil.getParameterMap(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return ParadoxScriptPsiImplUtil.getPresentation(this);
  }

  @Override
  public boolean isEquivalentTo(@NotNull PsiElement another) {
    return ParadoxScriptPsiImplUtil.isEquivalentTo(this, another);
  }

}
