// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import icu.windea.pls.localisation.psi.*;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.localisation.reference.ParadoxLocalisationIconReference;
import javax.swing.Icon;

public class ParadoxLocalisationIconImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationIcon {

  public ParadoxLocalisationIconImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitIcon(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxLocalisationRichText> getRichTextList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxLocalisationRichText.class);
  }

  @Override
  @Nullable
  public PsiElement getIconId() {
    return findChildByType(ICON_ID);
  }

  @Override
  @Nullable
  public PsiElement getIconParameter() {
    return findChildByType(ICON_PARAMETER);
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
  public ParadoxLocalisationIcon setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxLocalisationPsiImplUtil.getTextOffset(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationIconReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
