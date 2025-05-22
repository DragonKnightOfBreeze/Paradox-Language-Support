// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.localisation.references.ParadoxLocalisationConceptPsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationConcept extends ParadoxLocalisationRichText, NavigatablePsiElement {

  @Nullable
  ParadoxLocalisationConceptName getConceptName();

  @Nullable
  ParadoxLocalisationConceptText getConceptText();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationConcept setName(@NotNull String name);

  @Nullable ParadoxLocalisationConceptPsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
