// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.*;
import icu.windea.pls.script.reference.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptParameterConditionParameter extends ParadoxInputParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptParameterConditionParameter setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  ParadoxParameterReference getReference();

}
