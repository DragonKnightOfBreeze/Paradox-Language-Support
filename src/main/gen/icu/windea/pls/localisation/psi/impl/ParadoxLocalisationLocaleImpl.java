// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.localisation.psi.ParadoxLocalisationElementTypes.*;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationLocaleStub;
import icu.windea.pls.localisation.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxLocalisationLocaleImpl extends ParadoxLocalisationStubElementImpl<ParadoxLocalisationLocaleStub> implements ParadoxLocalisationLocale {

  public ParadoxLocalisationLocaleImpl(@NotNull ParadoxLocalisationLocaleStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxLocalisationLocaleImpl(@NotNull ParadoxLocalisationLocaleStub stub, @NotNull IElementType type) {
    super(stub, type);
  }

  public ParadoxLocalisationLocaleImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitLocale(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull PsiElement getIdElement() {
    return ParadoxLocalisationPsiImplUtil.getIdElement(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return ParadoxLocalisationPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull ParadoxLocalisationLocale setName(@NotNull String name) {
    return ParadoxLocalisationPsiImplUtil.setName(this, name);
  }

  @Override
  public @Nullable PsiReference getReference() {
    return ParadoxLocalisationPsiImplUtil.getReference(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return ParadoxLocalisationPsiImplUtil.getReferences(this);
  }

  @Override
  public @NotNull IElementType getIElementType() {
    return ParadoxLocalisationPsiImplUtil.getIElementType(this);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxLocalisationPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxLocalisationPsiImplUtil.getUseScope(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return ParadoxLocalisationPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxLocalisationPsiImplUtil.toString(this);
  }

}
