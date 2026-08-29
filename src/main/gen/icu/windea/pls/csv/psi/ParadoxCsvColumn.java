// This is a generated file. Not intended for manual editing.
package icu.windea.pls.csv.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.text.QuotePattern;
import javax.swing.Icon;

public interface ParadoxCsvColumn extends PsiQuoteAwareElement, ParadoxCsvLiteralValue, ParadoxCsvExpressionElement {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxCsvColumn setValue(@NotNull String value);

  @NotNull ParadoxCsvColumn setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull QuotePattern getQuotePattern();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
