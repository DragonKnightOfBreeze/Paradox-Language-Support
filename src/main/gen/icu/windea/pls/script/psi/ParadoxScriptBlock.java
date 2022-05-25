// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.core.ParadoxValueType;
import javax.swing.Icon;

public interface ParadoxScriptBlock extends ParadoxScriptValue, PsiListLikeElement {

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

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getType();

}
