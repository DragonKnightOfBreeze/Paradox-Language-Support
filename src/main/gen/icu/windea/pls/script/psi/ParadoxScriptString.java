// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import icu.windea.pls.core.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptValueReference;

public interface ParadoxScriptString extends ParadoxScriptValue, PsiLiteralValue {

  @Nullable
  ParadoxScriptParameter getParameter();

  @Nullable
  ParadoxScriptValueStringTemplate getValueStringTemplate();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptString setValue(@NotNull String name);

  @Nullable
  ParadoxScriptValueReference getReference();

  @NotNull
  String getStringValue();

  @NotNull
  ParadoxValueType getValueType();

}
