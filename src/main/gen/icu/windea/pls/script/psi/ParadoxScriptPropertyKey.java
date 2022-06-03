// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptPropertyKeyReference;

public interface ParadoxScriptPropertyKey extends ParadoxScriptExpression {

  @Nullable
  ParadoxScriptParameter getParameter();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @Nullable
  ParadoxScriptPropertyKeyReference getReference();

  @Nullable
  String getConfigExpression();

  @NotNull
  ParadoxValueType getValueType();

}
