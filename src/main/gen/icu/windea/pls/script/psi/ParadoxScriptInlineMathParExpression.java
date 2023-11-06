// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

public interface ParadoxScriptInlineMathParExpression extends ParadoxScriptInlineMathExpression {

  @Nullable
  ParadoxScriptInlineMathAbsExpression getInlineMathAbsExpression();

  @Nullable
  ParadoxScriptInlineMathBiExpression getInlineMathBiExpression();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @Nullable
  ParadoxScriptInlineMathParExpression getInlineMathParExpression();

  @Nullable
  ParadoxScriptInlineMathUnaryExpression getInlineMathUnaryExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
