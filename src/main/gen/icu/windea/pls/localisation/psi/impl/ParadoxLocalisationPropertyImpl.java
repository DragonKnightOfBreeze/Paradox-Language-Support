// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyStub;
import icu.windea.pls.localisation.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.model.ParadoxLocalisationCategory;
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
  public Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationProperty setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @NotNull
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCategory getCategory() {
    return ParadoxLocalisationPsiImplUtil.getCategory(this);
  }

  @Override
  @Nullable
  public String getValue() {
    return ParadoxLocalisationPsiImplUtil.getValue(this);
  }

}
