// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.expression.*;
import org.jetbrains.annotations.*;

import java.util.*;

public interface ParadoxScriptInlineMath extends ParadoxScriptValue {

  @NotNull
  List<ParadoxScriptInlineMathExpression> getInlineMathExpressionList();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @NotNull
  String getValue();

  @NotNull
  ParadoxDataType getExpressionType();

  @NotNull
  String getExpression();

}
