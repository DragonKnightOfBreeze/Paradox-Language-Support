// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptValue extends ParadoxScriptTypedElement, ParadoxScriptConfigAwareElement {

  @Nullable
  ParadoxScriptString getString();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  @NotNull
  ParadoxValueType getValueType();

  @Nullable
  String getConfigExpression();

}
