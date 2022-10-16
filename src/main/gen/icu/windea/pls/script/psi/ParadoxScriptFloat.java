// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.core.model.ParadoxValueType;

public interface ParadoxScriptFloat extends ParadoxScriptValue, PsiLiteralValue {

  @NotNull
  String getValue();

  float getFloatValue();

  @NotNull
  ParadoxValueType getValueType();

}
