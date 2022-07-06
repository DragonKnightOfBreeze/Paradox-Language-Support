// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiReference;
import icu.windea.pls.model.ParadoxValueType;
import icu.windea.pls.script.reference.ParadoxScriptKeyReference;

public interface ParadoxScriptPropertyKey extends ParadoxScriptPsiExpression, PsiLiteralValue, ContributedReferenceHost {

  @Nullable
  ParadoxScriptParameter getParameter();

  @NotNull
  String getValue();

  @NotNull
  ParadoxScriptPropertyKey setValue(@NotNull String value);

  @Nullable
  ParadoxScriptKeyReference getReference();

  @NotNull
  PsiReference[] getReferences();

  @Nullable
  String getConfigExpression();

  @NotNull
  ParadoxValueType getValueType();

}
