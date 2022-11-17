// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.script.exp.*;
import icu.windea.pls.script.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptScriptedVariableReference extends ParadoxScriptValue, ParadoxScriptedVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptScriptedVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariablePsiReference getReference();

  @NotNull
  ParadoxDataType getExpressionType();

}
