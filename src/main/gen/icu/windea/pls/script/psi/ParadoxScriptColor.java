// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.exp.ParadoxDataType;
import java.awt.Color;

public interface ParadoxScriptColor extends ParadoxScriptValue {

  @NotNull
  String getValue();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

  @NotNull
  ParadoxDataType getExpressionType();

}
