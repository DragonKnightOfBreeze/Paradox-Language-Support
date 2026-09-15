// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi.impl;

import com.intellij.extapi.psi.ASTWrapperPsiElement;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.util.PsiTreeUtil;
import icu.windea.pls.csv.psi.ParadoxCsvColumn;
import icu.windea.pls.csv.psi.ParadoxCsvElementPresentation;
import icu.windea.pls.csv.psi.ParadoxCsvRow;
import icu.windea.pls.csv.psi.ParadoxCsvVisitor;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public class ParadoxCsvRowImpl extends ASTWrapperPsiElement implements ParadoxCsvRow {

  public ParadoxCsvRowImpl(@NotNull ASTNode node) {
    super(node);
  }

  public void accept(@NotNull ParadoxCsvVisitor visitor) {
    visitor.visitRow(this);
  }

  @Override
  public void accept(@NotNull PsiElementVisitor visitor) {
    if (visitor instanceof ParadoxCsvVisitor) accept((ParadoxCsvVisitor)visitor);
    else super.accept(visitor);
  }

  @Override
  @NotNull
  public List<ParadoxCsvColumn> getColumnList() {
    return PsiTreeUtil.getChildrenOfTypeAsList(this, ParadoxCsvColumn.class);
  }

  @Override
  public @NotNull List<@NotNull PsiElement> getComponents() {
    return ParadoxCsvPsiImplUtil.getComponents(this);
  }

  @Override
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxCsvPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull ParadoxCsvElementPresentation getPresentation() {
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

  @Override
  public @NotNull String toString() {
    return ParadoxCsvPsiImplUtil.toString(this);
  }

}
