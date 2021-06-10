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

public class ParadoxLocalisationStringImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationString {

  public ParadoxLocalisationStringImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitString(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public PsiElement getStringToken() {
    return notNullChild(findChildByType(STRING_TOKEN));
  }

}
