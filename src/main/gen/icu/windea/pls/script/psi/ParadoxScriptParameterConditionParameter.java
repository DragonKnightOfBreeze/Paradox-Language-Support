// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.psi.*;
import org.jetbrains.annotations.*;
import icu.windea.pls.core.references.ParadoxParameterPsiReference;
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
  ParadoxParameterPsiReference getReference();

}
