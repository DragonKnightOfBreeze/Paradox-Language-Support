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

public class ParadoxLocalisationColorfulTextImpl extends ParadoxLocalisationNamedElementImpl implements ParadoxLocalisationColorfulText {

  public ParadoxLocalisationColorfulTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitColorfulText(this);
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
  public PsiElement getColorId() {
    return findChildByType(COLOR_ID);
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
  @Nullable
  public PsiElement getNameIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNameIdentifier(this);
  }

  @Override
  public int getTextOffset() {
    return ParadoxLocalisationPsiImplUtil.getTextOffset(this);
  }

}
