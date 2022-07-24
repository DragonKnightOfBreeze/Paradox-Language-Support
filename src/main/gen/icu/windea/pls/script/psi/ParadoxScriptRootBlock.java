// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ParadoxScriptRootBlock extends IParadoxScriptBlock {

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
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptString> getStringList();

  @NotNull
  List<ParadoxScriptVariable> getVariableList();

  @NotNull
  List<ParadoxScriptVariableReference> getVariableReferenceList();

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<PsiElement> getComponents();

}
