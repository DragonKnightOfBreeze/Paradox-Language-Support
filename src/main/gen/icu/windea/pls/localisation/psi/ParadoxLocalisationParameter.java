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

public interface ParadoxLocalisationParameter extends ParadoxLocalisationRichText, NavigatablePsiElement, ParadoxLocalisationArgumentAwareElement, ParadoxLocalisationCommandAwareElement {

  @Nullable
  ParadoxLocalisationScriptedVariableReference getScriptedVariableReference();

  @Nullable PsiElement getIdElement();

  @Nullable ParadoxLocalisationParameterArgument getArgumentElement();

  @NotNull String getName();

  @NotNull ParadoxLocalisationParameter setName(@NotNull String name);

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
