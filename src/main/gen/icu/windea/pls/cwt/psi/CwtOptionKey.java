// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import icu.windea.pls.core.psi.PsiPresentableTextAwareElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.text.QuotePattern;
import javax.swing.Icon;

public interface CwtOptionKey extends PsiQuoteAwareElement, PsiPresentableTextAwareElement, NavigatablePsiElement {

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull String getPresentableText();

  @NotNull QuotePattern getQuotePattern();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
