// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.model.ParadoxValueType;
import javax.swing.Icon;

public interface ParadoxScriptValue extends ParadoxScriptPsiExpression {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @Nullable
  String getConfigExpression();

  @NotNull
  ParadoxValueType getValueType();

}
