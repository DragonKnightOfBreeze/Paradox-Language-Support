// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiReference;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptValueReference;

public interface ParadoxScriptString extends ParadoxScriptValue, PsiLiteralValue, ContributedReferenceHost {

  @Nullable
  ParadoxScriptParameter getParameter();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptString setValue(@NotNull String name);

  @Nullable
  ParadoxScriptValueReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @NotNull
  String getStringValue();

  @NotNull
  ParadoxValueType getValueType();

}
