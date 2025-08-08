// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxLocalisationColorfulText extends ParadoxLocalisationRichText, NavigatablePsiElement, ParadoxLocalisationTextColorAwareElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable PsiElement getIdElement();

  @Nullable String getName();

  @NotNull ParadoxLocalisationColorfulText setName(@NotNull String name);

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
