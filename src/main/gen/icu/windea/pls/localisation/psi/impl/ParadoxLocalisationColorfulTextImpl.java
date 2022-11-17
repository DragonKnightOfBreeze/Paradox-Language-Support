// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import icu.windea.pls.localisation.psi.*;
import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

import java.util.*;

public class ParadoxLocalisationColorfulTextImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationColorfulText {

  public ParadoxLocalisationColorfulTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
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
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationColorfulText setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public ParadoxLocalisationColorPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
