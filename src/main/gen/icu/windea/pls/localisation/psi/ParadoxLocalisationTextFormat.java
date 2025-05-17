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
import icu.windea.pls.localisation.references.ParadoxLocalisationTextFormatPsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationTextFormat extends ParadoxLocalisationRichText, NavigatablePsiElement, ParadoxLocalisationParameterAwareElement, ParadoxLocalisationCommandAwareElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxLocalisationTextFormat setName(@NotNull String name);

  @Nullable ParadoxLocalisationTextFormatPsiReference getReference();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
