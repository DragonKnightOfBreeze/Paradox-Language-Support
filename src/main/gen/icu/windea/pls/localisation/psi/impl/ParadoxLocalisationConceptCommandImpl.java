// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import icu.windea.pls.localisation.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.localisation.references.ParadoxLocalisationConceptPsiReference;
import javax.swing.Icon;

public class ParadoxLocalisationConceptCommandImpl extends ParadoxLocalisationRichTextImpl implements ParadoxLocalisationConceptCommand {

  public ParadoxLocalisationConceptCommandImpl(@NotNull ASTNode node) {
    super(node);
  }

  @Override
  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitConceptCommand(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationConceptName getConceptName() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationConceptName.class);
  }

  @Override
  @Nullable
  public ParadoxLocalisationConceptText getConceptText() {
    return PsiTreeUtil.getChildOfType(this, ParadoxLocalisationConceptText.class);
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
  public ParadoxLocalisationConceptCommand setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  @Nullable
  public ParadoxLocalisationConceptPsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
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
