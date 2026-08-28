// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.text.QuotePattern;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ParadoxLocalisationPropertyValue extends PsiQuoteAwareElement, ParadoxLocalisationRichTextContainer {

    @Nullable PsiElement getTokenElement();

    @NotNull List<@NotNull ParadoxLocalisationRichText> getRichTextList();

    @NotNull QuotePattern getQuotePattern();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
