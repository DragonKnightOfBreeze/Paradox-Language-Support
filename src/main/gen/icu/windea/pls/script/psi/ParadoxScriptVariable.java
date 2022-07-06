// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import icu.windea.pls.model.ParadoxValueType;
import javax.swing.Icon;

public interface ParadoxScriptVariable extends ParadoxScriptNamedElement, ParadoxScriptPsiExpression, StubBasedPsiElement<ParadoxScriptVariableStub> {

  @NotNull
  ParadoxScriptVariableName getVariableName();

  @Nullable
  ParadoxScriptVariableValue getVariableValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptVariable setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable
  String getValue();

  @Nullable
  String getUnquotedValue();

  @Nullable
  ParadoxValueType getValueType();

}
