// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

import java.util.*;

public interface ParadoxScriptInlineMathBiExpression extends ParadoxScriptInlineMathExpression {

  @NotNull
  List<ParadoxScriptInlineMathExpression> getInlineMathExpressionList();

  @NotNull
  List<ParadoxScriptInlineMathFactor> getInlineMathFactorList();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
