// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

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
