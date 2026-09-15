// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;
import java.util.List;

public interface ParadoxCsvHeader extends ParadoxCsvColumnContainer, NavigatablePsiElement, PsiListLikeElement {

  @NotNull
  List<ParadoxCsvColumn> getColumnList();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull ParadoxCsvElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
