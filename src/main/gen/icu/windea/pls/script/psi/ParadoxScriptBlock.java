// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import icu.windea.pls.core.ParadoxValueType;

public interface ParadoxScriptBlock extends ParadoxScriptValue, PsiListLikeElement {

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<ParadoxScriptVariable> getVariableList();

  @NotNull
  String getValue();

  @NotNull
  String getTruncatedValue();

  boolean isEmpty();

  boolean isNotEmpty();

  boolean isObject();

  boolean isArray();

  @NotNull
  List<PsiElement> getComponents();

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getType();

}
