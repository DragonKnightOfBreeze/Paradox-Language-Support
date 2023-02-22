// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.expression.ParadoxDataType;
import java.awt.Color;

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
  ParadoxDataType getType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
