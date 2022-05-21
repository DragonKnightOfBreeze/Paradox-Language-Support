// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.core.ParadoxValueType;
import java.awt.Color;

public interface ParadoxScriptColor extends ParadoxScriptValue, PsiLiteralValue {

  @NotNull
  String getValue();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getType();

}
