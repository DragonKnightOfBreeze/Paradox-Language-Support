// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.TextRange;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface ParadoxScriptValue extends ParadoxScriptExpressionElement, ParadoxScriptMember {

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull String getValue();

  @NotNull String getPresentableText();

  @NotNull ParadoxScriptValue setValue(@NotNull String value);

  @NotNull ParadoxScriptValue setContent(@NotNull String content, @NotNull TextRange range);

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
