// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.text.QuotePattern;
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxScriptPropertyKey extends ParadoxScriptStringExpressionElement, ParadoxScriptLiteralValue, ParadoxScriptInterpolationContainer, ParadoxLanguageInjectionHost, PsiQuoteAwareElement, PsiPresentableTextAwareElement, NavigatablePsiElement {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxScriptPropertyKey setValue(@NotNull String value);

  @NotNull ParadoxScriptPropertyKey setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull QuotePattern getQuotePattern();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
