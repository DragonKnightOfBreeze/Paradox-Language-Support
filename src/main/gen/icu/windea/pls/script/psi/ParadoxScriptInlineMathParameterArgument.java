// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface ParadoxScriptInlineMathParameterArgument extends ParadoxScriptArgument {

  @Nullable PsiElement getIdElement();

  @NotNull String getValue();

  @NotNull ParadoxScriptInlineMathParameterArgument setValue(@NotNull String value);

  @NotNull ParadoxScriptInlineMathParameterArgument setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
