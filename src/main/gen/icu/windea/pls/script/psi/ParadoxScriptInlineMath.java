// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.expression.ParadoxScriptExpressionType;

public interface ParadoxScriptInlineMath extends ParadoxScriptValue {

  @NotNull
  List<ParadoxScriptInlineMathExpression> getInlineMathExpressionList();

  @Nullable
  ParadoxScriptInlineMathFactor getInlineMathFactor();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptExpressionType getExpressionType();

  @NotNull
  String getExpression();

}
