// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxLocalisationPropertyList extends PsiListLikeElement {

  @NotNull
  ParadoxLocalisationLocale getLocale();

  @NotNull
  List<ParadoxLocalisationProperty> getPropertyList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  List<ParadoxLocalisationProperty> getComponents();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
