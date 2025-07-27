// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.awt.*;
import java.util.List;

public interface ParadoxScriptColor extends ParadoxScriptValue, ParadoxScriptLiteralValue {

  @NotNull String getValue();

  @NotNull String getColorType();

  @NotNull List<@NotNull String> getColorArgs();

  @Nullable Color getColor();

  void setColor(@NotNull Color color);

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
