// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.localisation.references.ParadoxLocalisationTextColorPsiReference;

public interface ParadoxLocalisationColorfulText extends ParadoxLocalisationRichText, NavigatablePsiElement, ParadoxLocalisationTextColorAwareElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable
  PsiElement getIdElement();

  @Nullable
  String getName();

  @NotNull
  ParadoxLocalisationColorfulText setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationTextColorPsiReference getReference();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
