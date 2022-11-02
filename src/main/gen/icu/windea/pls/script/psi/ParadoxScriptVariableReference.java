// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.expression.*;
import icu.windea.pls.script.reference.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptVariableReference extends ParadoxScriptValue, IParadoxScriptVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariableReference getReference();

  @NotNull
  ParadoxDataType getExpressionType();

}
