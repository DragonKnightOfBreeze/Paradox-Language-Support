// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.exp.ParadoxDataType;

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
