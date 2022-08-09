// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.script.reference.ParadoxScriptedVariableReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathVariableReference extends ParadoxScriptInlineMathFactor, IParadoxScriptVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptInlineMathVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariableReference getReference();

}
