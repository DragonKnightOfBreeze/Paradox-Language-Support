// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import icu.windea.pls.core.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.awt.*;
import java.util.List;

public interface ParadoxScriptBlock extends ParadoxScriptValue, IParadoxScriptBlock {

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<ParadoxScriptVariable> getVariableList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

  @NotNull
  ParadoxValueType getValueType();

}
