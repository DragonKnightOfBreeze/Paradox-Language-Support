// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import java.awt.Color;

public interface ParadoxScriptColor extends ParadoxScriptStringValue {

  @NotNull
  PsiElement getColorToken();

  @NotNull
  String getValue();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

}
