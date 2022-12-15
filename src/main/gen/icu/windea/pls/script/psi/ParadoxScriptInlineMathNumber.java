// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import icu.windea.pls.core.psi.*;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.core.expression.ParadoxDataType;

public interface ParadoxScriptInlineMathNumber extends ParadoxScriptInlineMathFactor, PsiLiteralValue,
    ParadoxTypedElement {

  @NotNull
  String getValue();

  @NotNull
  ParadoxDataType getType();

  @NotNull
  String getExpression();

}
