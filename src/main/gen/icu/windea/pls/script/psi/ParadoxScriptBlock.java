// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.script.exp.ParadoxDataType;
import java.awt.Color;
import javax.swing.Icon;

public interface ParadoxScriptBlock extends ParadoxScriptValue, ParadoxScriptBlockElement {

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptString> getStringList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

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
  ParadoxDataType getExpressionType();

  @NotNull
  String getExpression();

}
