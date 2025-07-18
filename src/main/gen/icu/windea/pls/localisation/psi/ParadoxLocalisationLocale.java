// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationLocalePsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationLocale extends NavigatablePsiElement {

  @NotNull PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationLocale setName(@NotNull String name);

  @NotNull ParadoxLocalisationLocalePsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
