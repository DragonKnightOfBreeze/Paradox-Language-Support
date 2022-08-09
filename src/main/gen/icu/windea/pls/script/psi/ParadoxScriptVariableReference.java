// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptedVariableReference;
import javax.swing.Icon;

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
  ParadoxValueType getValueType();

}
