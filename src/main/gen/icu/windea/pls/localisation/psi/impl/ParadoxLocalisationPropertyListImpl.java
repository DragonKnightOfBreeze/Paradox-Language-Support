// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi.impl;

import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.stubs.IStubElementType;
import com.intellij.psi.tree.IElementType;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.localisation.psi.ParadoxLocalisationLocale;
import icu.windea.pls.localisation.psi.ParadoxLocalisationProperty;
import icu.windea.pls.localisation.psi.ParadoxLocalisationPropertyList;
import icu.windea.pls.localisation.psi.ParadoxLocalisationVisitor;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public class ParadoxLocalisationPropertyListImpl extends ParadoxLocalisationStubElementImpl<ParadoxLocalisationPropertyListStub> implements ParadoxLocalisationPropertyList {

  public ParadoxLocalisationPropertyListImpl(@NotNull ParadoxLocalisationPropertyListStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

  public ParadoxLocalisationPropertyListImpl(@NotNull ParadoxLocalisationPropertyListStub stub, @NotNull IElementType type) {
    super(stub, type);
  }

  public ParadoxLocalisationPropertyListImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxLocalisationVisitor visitor) {
    visitor.visitPropertyList(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxLocalisationVisitor) accept((ParadoxLocalisationVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @Nullable
  public ParadoxLocalisationLocale getLocale() {
    return PsiTreeUtil.getStubChildOfType(this, ParadoxLocalisationLocale.class);
  }

  @Override
  @NotNull
  public List<ParadoxLocalisationProperty> getPropertyList() {
    return PsiTreeUtil.getStubChildrenOfTypeAsList(this, ParadoxLocalisationProperty.class);
  }

  @Override
  public @NotNull List<@NotNull PsiElement> getComponents() {
    return ParadoxLocalisationPsiImplUtil.getComponents(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxLocalisationPsiImplUtil.getIcon(this, flags);
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
  public @NotNull String toString() {
    return ParadoxLocalisationPsiImplUtil.toString(this);
  }

}
