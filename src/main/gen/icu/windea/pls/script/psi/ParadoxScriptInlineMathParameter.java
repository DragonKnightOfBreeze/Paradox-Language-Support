// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import icu.windea.pls.script.reference.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptInlineMathParameter extends ParadoxScriptInlineMathFactor, ParadoxParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptInlineMathParameter setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable
  String getDefaultValue();

  @Nullable
  ParadoxParameterReference getReference();

}
