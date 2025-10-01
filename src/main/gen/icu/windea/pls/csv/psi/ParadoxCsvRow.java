// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxCsvRow extends ParadoxCsvRowElement {

  @NotNull
  List<ParadoxCsvColumn> getColumnList();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
