// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import icu.windea.pls.localisation.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;

import static icu.windea.pls.localisation.psi.ParadoxLocalisationTypes.*;
import icu.windea.pls.localisation.psi.*;

public class ParadoxLocalisationEscapeImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationEscape {

  public ParadoxLocalisationEscapeImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitEscape(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getInvalidEscapeToken() {
    return findChildByType(INVALID_ESCAPE_TOKEN);
  }

  @Override
  @Nullable
  public PsiElement getValidEscapeToken() {
    return findChildByType(VALID_ESCAPE_TOKEN);
  }

}
