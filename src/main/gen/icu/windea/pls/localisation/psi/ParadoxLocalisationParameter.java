// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxLocalisationParameter extends ParadoxLocalisationRichText, ParadoxLocalisationInterpolation, ParadoxLocalisationInterpolationContainer, ParadoxLocalisationArgumentAwareElement {

    @Nullable
    ParadoxLocalisationScriptedVariableReference getScriptedVariableReference();

    @Nullable PsiElement getIdElement();

    @Nullable ParadoxLocalisationParameterArgument getArgumentElement();

    @NotNull String getName();

    @NotNull ParadoxLocalisationParameter setName(@NotNull String name);

    @NotNull String getPresentableText();

    @Nullable PsiReference getReference();

    @NotNull PsiReference @NotNull [] getReferences();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
