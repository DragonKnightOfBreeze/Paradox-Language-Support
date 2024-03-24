// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

public interface ParadoxScriptInlineMathUnaryExpression extends ParadoxScriptInlineMathExpression {

  @Nullable
  ParadoxScriptInlineMathAbsExpression getInlineMathAbsExpression();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @Nullable
  ParadoxScriptInlineMathParExpression getInlineMathParExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
