// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.model.*;
import org.jetbrains.annotations.*;

import java.awt.*;
import java.util.List;

public interface ParadoxScriptColor extends ParadoxScriptValue {

  @NotNull
  String getValue();

  @NotNull
  String getColorType();

  @NotNull
  List<String> getColorArgs();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

  @NotNull
  ParadoxType getType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
