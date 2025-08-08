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

public interface ParadoxLocalisationCommandText extends NavigatablePsiElement, ParadoxLocalisationExpressionElement, ParadoxLocalisationParameterAwareElement {

  @Nullable PsiElement getIdElement();

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxLocalisationCommandText setValue(@NotNull String value);

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
