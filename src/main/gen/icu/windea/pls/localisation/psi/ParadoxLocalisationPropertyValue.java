// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.text.QuotePattern;

public interface ParadoxLocalisationPropertyValue extends ParadoxLocalisationRichTextContainer, PsiPresentableTextAwareElement, PsiQuoteAwareElement {

  @Nullable PsiElement getTokenElement();

  @NotNull List<@NotNull ParadoxLocalisationRichText> getRichTextList();

  @NotNull String getPresentableText();

  @NotNull QuotePattern getQuotePattern();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
