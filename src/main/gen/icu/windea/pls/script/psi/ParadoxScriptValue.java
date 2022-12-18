// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.core.expression.ParadoxDataType;
import javax.swing.Icon;

public interface ParadoxScriptValue extends ParadoxScriptExpressionElement, ParadoxScriptMemberElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @NotNull
  ParadoxDataType getType();

  @NotNull
  String getExpression();

  @Nullable
  String getConfigExpression();

}
