// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.model.*;
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
  ParadoxType getType();

  @NotNull
  String getExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
