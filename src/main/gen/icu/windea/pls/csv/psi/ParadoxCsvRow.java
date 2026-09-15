// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxCsvRow extends PsiListLikeElement, NavigatablePsiElement, ParadoxCsvColumnContainer {

  @NotNull
  List<ParadoxCsvColumn> getColumnList();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull ParadoxCsvElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
