// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.localisation.ParadoxLocalisationConceptPsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationConceptCommand extends ParadoxLocalisationRichText, NavigatablePsiElement {

  @Nullable
  ParadoxLocalisationConceptName getConceptName();

  @Nullable
  ParadoxLocalisationConceptText getConceptText();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationConceptCommand setName(@NotNull String name);

  @Nullable ParadoxLocalisationConceptPsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
