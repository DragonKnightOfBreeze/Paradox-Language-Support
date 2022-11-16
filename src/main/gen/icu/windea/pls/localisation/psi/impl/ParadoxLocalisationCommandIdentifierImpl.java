// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.localisation.psi.*;

public class ParadoxLocalisationCommandIdentifierImpl extends ASTWrapperPsiElement implements ParadoxLocalisationCommandIdentifier {

  public ParadoxLocalisationCommandIdentifierImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandIdentifier(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandScope getPrevIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getPrevIdentifier(this);
  }

  @Override
  @Nullable
  public ParadoxLocalisationCommandIdentifier getNextIdentifier() {
    return ParadoxLocalisationPsiImplUtil.getNextIdentifier(this);
  }

}
