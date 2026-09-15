// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxLocalisationConceptName extends ParadoxLocalisationExpressionElement, ParadoxLocalisationInterpolationContainer, PsiPresentableTextAwareElement {

  @Nullable PsiElement getIdElement();

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxLocalisationConceptName setValue(@NotNull String value);

  @NotNull ParadoxLocalisationConceptName setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
