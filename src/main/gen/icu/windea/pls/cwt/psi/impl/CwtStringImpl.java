// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.cwt.psi.CwtElementTypes.*;
import icu.windea.pls.cwt.psi.*;

public class CwtStringImpl extends CwtNamedElementImpl implements CwtString {

  public CwtStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull CwtVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof CwtVisitor) accept((CwtVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public String getValue() {
    return CwtPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public String getTruncatedValue() {
    return CwtPsiImplUtil.getTruncatedValue(this);
  }

  @Override
  @NotNull
  public String getStringValue() {
    return CwtPsiImplUtil.getStringValue(this);
  }

  @Override
  @NotNull
  public String getName() {
    return CwtPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public CwtString setName(@NotNull String name) {
    return CwtPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return CwtPsiImplUtil.getNameIdentifier(this);
  }

}
