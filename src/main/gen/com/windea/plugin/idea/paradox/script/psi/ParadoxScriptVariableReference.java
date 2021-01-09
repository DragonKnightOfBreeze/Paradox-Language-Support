// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.paradox.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.windea.plugin.idea.paradox.script.reference.ParadoxScriptVariablePsiReference;

public interface ParadoxScriptVariableReference extends ParadoxScriptValue {

  @NotNull
  PsiElement getVariableReferenceId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  ParadoxScriptVariablePsiReference getReference();

  @Nullable
  ParadoxScriptValue getReferenceValue();

}
