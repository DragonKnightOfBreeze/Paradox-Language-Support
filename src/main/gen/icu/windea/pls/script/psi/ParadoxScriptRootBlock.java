// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ParadoxScriptRootBlock extends ParadoxScriptBlockElement {

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptString> getStringList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

}
