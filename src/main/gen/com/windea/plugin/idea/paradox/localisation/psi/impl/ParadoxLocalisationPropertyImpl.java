// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationTypes.*;
import com.windea.plugin.idea.paradox.localisation.psi.ParadoxLocalisationPropertyStub;
import com.windea.plugin.idea.paradox.localisation.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.windea.plugin.idea.paradox.ParadoxLocale;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxLocalisationPropertyImpl extends ParadoxLocalisationStubElementImpl<ParadoxLocalisationPropertyStub> implements ParadoxLocalisationProperty {

  public ParadoxLocalisationPropertyImpl(@NotNull ParadoxLocalisationPropertyStub stub, @Nullable IStubElementType<?, ?> nodeType) {
    super(stub, nodeType);
  }

  public ParadoxLocalisationPropertyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitProperty(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public ParadoxLocalisationPropertyKey getPropertyKey() {
    return notNullChild(PsiTreeUtil.getChildOfType(this, ParadoxLocalisationPropertyKey.class));
  }

  @Override
  @Nullable
  public ParadoxLocalisationPropertyValue getPropertyValue() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationPropertyValue.class);
  }

  @Override
  @Nullable
  public PsiElement getNumber() {
    return findChildByType(NUMBER);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @NotNull
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @Nullable
  public String getValue() {
    return ParadoxLocalisationPsiImplUtil.getValue(this);
  }

  @Override
  @Nullable
  public ParadoxLocale getParadoxLocale() {
    return ParadoxLocalisationPsiImplUtil.getParadoxLocale(this);
  }

}
