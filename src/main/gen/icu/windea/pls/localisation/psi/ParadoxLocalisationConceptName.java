// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxLocalisationConceptName extends ParadoxLocalisationExpressionElement, ParadoxLocalisationInterpolationContainer {

  @Nullable PsiElement getIdElement();

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxLocalisationConceptName setValue(@NotNull String value);

  @NotNull ParadoxLocalisationConceptName setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
