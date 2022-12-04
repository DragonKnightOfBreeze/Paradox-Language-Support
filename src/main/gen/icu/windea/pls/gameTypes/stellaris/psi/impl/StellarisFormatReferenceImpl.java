// This is a generated file. Not intended for manual editing.
package icu.windea.pls.gameTypes.stellaris.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.gameTypes.stellaris.StellarisFormatStringElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.gameTypes.stellaris.psi.*;
import icu.windea.pls.gameTypes.stellaris.references.StellarisFormatPsiReference;

public class StellarisFormatReferenceImpl extends ASTWrapperPsiElement implements StellarisFormatReference {

  public StellarisFormatReferenceImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull StellarisVisitor visitor) {
    visitor.visitFormatReference(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof StellarisVisitor) accept((StellarisVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public PsiElement getFormatReferenceToken() {
    return findChildByType(FORMAT_REFERENCE_TOKEN);
  }

  @Override
  @Nullable
  public String getName() {
    return StellarisFormatStringPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public PsiElement setName(@NotNull String name) {
    return StellarisFormatStringPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public StellarisFormatPsiReference getReference() {
    return StellarisFormatStringPsiImplUtil.getReference(this);
  }

}
