// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.psi.*;
import org.jetbrains.annotations.*;
import icu.windea.pls.core.references.ParadoxScriptedVariablePsiReference;
import javax.swing.Icon;

public interface ParadoxScriptInlineMathScriptedVariableReference extends ParadoxScriptInlineMathFactor,
	ParadoxScriptedVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptInlineMathScriptedVariableReference setName(@NotNull String name);

  @NotNull
  ParadoxScriptedVariablePsiReference getReference();

}
