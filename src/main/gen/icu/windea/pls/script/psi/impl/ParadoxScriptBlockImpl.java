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

public class ParadoxScriptBlockImpl extends ParadoxScriptValueImpl implements ParadoxScriptBlock {

  public ParadoxScriptBlockImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxScriptVisitor visitor) {
    visitor.visitBlock(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxScriptVisitor) accept((ParadoxScriptVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxScriptProperty> getPropertyList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptProperty.class);
  }

  @Override
  @NotNull
  public List<ParadoxScriptValue> getValueList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxScriptValue.class);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxScriptPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public String getTruncatedValue() {
    return ParadoxScriptPsiImplUtil.getTruncatedValue(this);
  }

  @Override
  public boolean isEmpty() {
    return ParadoxScriptPsiImplUtil.isEmpty(this);
  }

  @Override
  public boolean isNotEmpty() {
    return ParadoxScriptPsiImplUtil.isNotEmpty(this);
  }

  @Override
  public boolean isObject() {
    return ParadoxScriptPsiImplUtil.isObject(this);
  }

  @Override
  public boolean isArray() {
    return ParadoxScriptPsiImplUtil.isArray(this);
  }

  @Override
  @NotNull
  public List<PsiElement> getComponents() {
    return ParadoxScriptPsiImplUtil.getComponents(this);
  }

}
