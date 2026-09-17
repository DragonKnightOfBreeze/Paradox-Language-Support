// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.openapi.util.TextRange;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.psi.ParadoxLanguageInjectionHost;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxScriptNormalParameterArgument extends ParadoxScriptArgument, ParadoxLanguageInjectionHost {

  @Nullable PsiElement getIdElement();

  @NotNull String getValue();

  @NotNull ParadoxScriptNormalParameterArgument setValue(@NotNull String value);

  @NotNull ParadoxScriptNormalParameterArgument setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
