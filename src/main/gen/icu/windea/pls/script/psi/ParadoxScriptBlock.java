// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;

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

  @Nullable
  ParadoxScriptProperty findProperty(@NotNull String propertyName);

  @Nullable
  ParadoxScriptValue findValue(@NotNull String value);

}
