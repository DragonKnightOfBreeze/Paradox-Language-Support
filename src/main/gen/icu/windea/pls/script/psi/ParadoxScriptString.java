// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiQuoteAwareElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.text.QuotePattern;
import javax.swing.Icon;

public interface ParadoxScriptString extends ParadoxScriptValue, ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement, ParadoxScriptInterpolationContainer, PsiQuoteAwareElement {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxScriptValue setValue(@NotNull String value);

  @NotNull ParadoxScriptValue setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull QuotePattern getQuotePattern();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
