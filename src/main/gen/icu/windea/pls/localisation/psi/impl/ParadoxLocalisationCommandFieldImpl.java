// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import icu.windea.pls.localisation.psi.*;
import icu.windea.pls.localisation.reference.ParadoxLocalisationCommandFieldReference;
import javax.swing.Icon;

public class ParadoxLocalisationCommandFieldImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationCommandField {

  public ParadoxLocalisationCommandFieldImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandField(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationPropertyReference getPropertyReference() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationPropertyReference.class);
  }

  @Override
  @Nullable
  public PsiElement getCommandFieldId() {
    return findChildByType(COMMAND_FIELD_ID);
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
  public ParadoxLocalisationCommandField setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandFieldReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getPrevIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getPrevIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getNextIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNextIdentifier(this);
  }

}
