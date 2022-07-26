// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxScriptParameterCondition extends PsiListLikeElement {

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

  @Nullable
  ParadoxScriptParameterConditionExpression getParameterConditionExpression();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptString> getStringList();

  @NotNull
  List<ParadoxScriptVariableReference> getVariableReferenceList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getExpression();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull
  List<PsiElement> getComponents();

}
