// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi.impl;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.lang.ASTNode;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiElementVisitor;
import com.intellij.psi.util.PsiTreeUtil;
import static icu.windea.pls.csv.psi.ParadoxCsvElementTypes.*;
import icu.windea.pls.csv.psi.ParadoxCsvRowStub;
import icu.windea.pls.csv.psi.*;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;
import com.intellij.psi.stubs.IStubElementType;

public class ParadoxCsvRowImpl extends ParadoxCsvStubElementImpl<ParadoxCsvRowStub> implements ParadoxCsvRow {

  public ParadoxCsvRowImpl(@NotNull ParadoxCsvRowStub stub, @NotNull IStubElementType<?, ?> type) {
    super(stub, type);
  }

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
  public @NotNull Icon getIcon(@IconFlags int flags) {
    return ParadoxCsvPsiImplUtil.getIcon(this, flags);
  }

  @Override
  public @NotNull List<@NotNull ParadoxCsvColumn> getComponents() {
    return ParadoxCsvPsiImplUtil.getComponents(this);
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
