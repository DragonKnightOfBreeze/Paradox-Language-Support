// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.reference.ParadoxScriptVariablePsiReference;

public interface ParadoxScriptVariableReference extends ParadoxScriptValue {

  @NotNull
  PsiElement getVariableReferenceId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @NotNull
  ParadoxScriptVariablePsiReference getReference();

  @Nullable
  ParadoxScriptValue getReferenceValue();

}
