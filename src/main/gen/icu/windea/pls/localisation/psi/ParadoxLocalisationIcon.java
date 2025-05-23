// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.localisation.references.ParadoxLocalisationIconPsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationIcon extends ParadoxLocalisationRichText, NavigatablePsiElement, ParadoxLocalisationArgumentAwareElement, ParadoxLocalisationParameterAwareElement, ParadoxLocalisationCommandAwareElement {

  @Nullable
  PsiElement getIdElement();

  @Nullable
  ParadoxLocalisationIconArgument getArgumentElement();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxLocalisationIcon setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationIconPsiReference getReference();

  int getFrame();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
