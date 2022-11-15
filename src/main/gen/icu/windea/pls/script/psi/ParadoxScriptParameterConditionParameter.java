// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.script.references.ParadoxParameterReference;
import javax.swing.Icon;

public interface ParadoxScriptParameterConditionParameter extends ParadoxArgument {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptParameterConditionParameter setName(@NotNull String name);

  int getTextOffset();

  @NotNull
  ParadoxParameterReference getReference();

}
