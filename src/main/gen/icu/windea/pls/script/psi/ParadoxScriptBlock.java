// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.model.ParadoxValueType;
import java.awt.Color;
import javax.swing.Icon;

public interface ParadoxScriptBlock extends ParadoxScriptValue, IParadoxScriptBlock {

  @NotNull
  List<ParadoxScriptBlock> getBlockList();

  @NotNull
  List<ParadoxScriptBoolean> getBooleanList();

  @NotNull
  List<ParadoxScriptColor> getColorList();

  @NotNull
  List<ParadoxScriptFloat> getFloatList();

  @NotNull
  List<ParadoxScriptInlineMath> getInlineMathList();

  @NotNull
  List<ParadoxScriptInt> getIntList();

  @NotNull
  List<ParadoxScriptParameterCondition> getParameterConditionList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptString> getStringList();

  @NotNull
  List<ParadoxScriptVariable> getVariableList();

  @NotNull
  List<ParadoxScriptVariableReference> getVariableReferenceList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<PsiElement> getComponents();

  @Nullable
  Color getColor();

  void setColor(@NotNull Color color);

  @NotNull
  ParadoxValueType getValueType();

}
