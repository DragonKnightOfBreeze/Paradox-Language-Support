// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;

import javax.swing.*;

public interface ParadoxScriptInlineMathNumber extends ParadoxScriptInlineMathFactor, ParadoxScriptLiteralValue {

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getValue();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
