// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.text.QuotePattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface CwtString extends CwtValue, PsiQuoteAwareElement, CwtNamedElement, CwtLiteralValue, CwtStringExpressionElement {

    @NotNull Icon getIcon(@IconFlags int flags);

    @NotNull String getName();

    @NotNull CwtString setName(@NotNull String name);

    @NotNull PsiElement getNameIdentifier();

    @NotNull String getValue();

    @NotNull CwtValue setValue(@NotNull String value);

    @NotNull CwtValue setContent(@NotNull String content, @NotNull TextRange range);

    @NotNull QuotePattern getQuotePattern();

    @NotNull String getPresentableText();

    @Nullable PsiReference getReference();

    @NotNull PsiReference @NotNull [] getReferences();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
