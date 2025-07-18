// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;
import com.intellij.extapi.psi.ASTWrapperPsiElement;
import icu.windea.pls.csv.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public class ParadoxCsvColumnImpl extends ASTWrapperPsiElement implements ParadoxCsvColumn {

  public ParadoxCsvColumnImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxCsvVisitor visitor) {
    visitor.visitColumn(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxCsvVisitor) accept((ParadoxCsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxCsvPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull String getName() {
    return ParadoxCsvPsiImplUtil.getName(this);
  }

  @Override
  public @NotNull String getValue() {
    return ParadoxCsvPsiImplUtil.getValue(this);
  }

  @Override
  public @NotNull ParadoxCsvColumn setValue(@NotNull String value) {
    return ParadoxCsvPsiImplUtil.setValue(this, value);
  }

  @Override
  public @Nullable PsiReference getReference() {
    return ParadoxCsvPsiImplUtil.getReference(this);
  }

  @Override
  public @NotNull PsiReference @NotNull [] getReferences() {
    return ParadoxCsvPsiImplUtil.getReferences(this);
  }

  @Override
  public @NotNull String toString() {
    return ParadoxCsvPsiImplUtil.toString(this);
  }

  @Override
  public @NotNull ItemPresentation getPresentation() {
    return ParadoxCsvPsiImplUtil.getPresentation(this);
  }

  @Override
  public @NotNull GlobalSearchScope getResolveScope() {
    return ParadoxCsvPsiImplUtil.getResolveScope(this);
  }

  @Override
  public @NotNull SearchScope getUseScope() {
    return ParadoxCsvPsiImplUtil.getUseScope(this);
  }

}
