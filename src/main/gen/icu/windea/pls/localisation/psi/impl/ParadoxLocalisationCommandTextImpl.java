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
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;

public class ParadoxLocalisationCommandTextImpl extends ASTWrapperPsiElement implements ParadoxLocalisationCommandText {

  public ParadoxLocalisationCommandTextImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitCommandText(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxLocalisationPropertyReference> getPropertyReferenceList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxLocalisationPropertyReference.class);
  }

  @Override
  @NotNull
  public String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  @NotNull
  public String getValue() {
    return ParadoxLocalisationPsiImplUtil.getValue(this);
  }

  @Override
  @NotNull
  public ParadoxLocalisationCommandText setValue(@NotNull String value) {
    return ParadoxLocalisationPsiImplUtil.setValue(this, value);
  }

  @Override
  @Nullable
  public ParadoxType getType() {
    return ParadoxLocalisationPsiImplUtil.getType(this);
  }

  @Override
  @NotNull
  public String getExpression() {
    return ParadoxLocalisationPsiImplUtil.getExpression(this);
  }

  @Override
  @Nullable
  public PsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  @NotNull
  public PsiReference[] getReferences() {
    return ParadoxLocalisationPsiImplUtil.getReferences(this);
  }

  @Override
  @NotNull
  public ItemPresentation getPresentation() {
    return ParadoxLocalisationPsiImplUtil.getPresentation(this);
  }

  @Override
  @NotNull
  public GlobalSearchScope getResolveScope() {
    return ParadoxLocalisationPsiImplUtil.getResolveScope(this);
  }

  @Override
  @NotNull
  public SearchScope getUseScope() {
    return ParadoxLocalisationPsiImplUtil.getUseScope(this);
  }

}
