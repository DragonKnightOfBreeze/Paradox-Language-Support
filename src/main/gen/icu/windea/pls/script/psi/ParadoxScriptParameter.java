// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.psi.*;
import org.jetbrains.annotations.*;
import icu.windea.pls.core.references.ParadoxParameterPsiReference;
import javax.swing.Icon;

public interface ParadoxScriptParameter extends ParadoxParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptParameter setName(@NotNull String name);

  int getTextOffset();

  @NotNull
  String getValue();

  @Nullable
  String getDefaultValue();

  @Nullable
  ParadoxParameterPsiReference getReference();

}
