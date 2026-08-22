// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface CwtPropertyKey extends PsiQuoteAwareElement, CwtLiteralValue, CwtStringExpressionElement {

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull String getPresentableText();

  @NotNull CwtPropertyKey setValue(@NotNull String value);

  @NotNull CwtPropertyKey setContent(@NotNull String content, @NotNull TextRange range);

  boolean needQuote();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
