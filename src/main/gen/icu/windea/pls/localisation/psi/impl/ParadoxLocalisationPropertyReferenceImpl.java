// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.lang.*;
import com.intellij.psi.*;
import com.intellij.psi.util.*;
import icu.windea.pls.localisation.psi.*;
import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

public class ParadoxLocalisationPropertyReferenceImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationPropertyReference {

  public ParadoxLocalisationPropertyReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitPropertyReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommand getCommand() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationCommand.class);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationPropertyReference setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public ParadoxLocalisationPropertyPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

}
