// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.script.expression.ParadoxScriptExpressionType;

public interface ParadoxScriptInt extends ParadoxScriptValue, PsiLiteralValue {

  @NotNull
  String getValue();

  int getIntValue();

  @NotNull
  ParadoxScriptExpressionType getExpressionType();

}
