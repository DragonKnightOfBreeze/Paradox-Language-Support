// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

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
import java.util.List;

public interface ParadoxScriptString extends ParadoxScriptValue, PsiQuoteAwareElement, ParadoxScriptLiteralValue, ParadoxScriptStringExpressionElement, ParadoxScriptInterpolationContainer, ParadoxScriptInlineConditionalBlockAwareElement {

  @NotNull
  List<ParadoxScriptInlineConditionalBlock> getInlineConditionalBlockList();

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull ParadoxScriptValue setValue(@NotNull String value);

  @NotNull ParadoxScriptValue setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull QuotePattern getQuotePattern();

  @NotNull String getPresentableText();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
