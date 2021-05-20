// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import icu.windea.pls.localisation.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.localisation.psi.*;

public class ParadoxLocalisationPropertyKeyImpl extends ASTWrapperPsiElement implements ParadoxLocalisationPropertyKey {

  public ParadoxLocalisationPropertyKeyImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitPropertyKey(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getPropertyKeyId() {
    return notNullChild(findChildByType(PROPERTY_KEY_ID));
  }

}
