// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.script.reference.ParadoxParameterReference;
import javax.swing.Icon;

public interface ParadoxParameterConditionParameter extends ParadoxInputParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxParameterConditionParameter setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  int getTextOffset();

  @NotNull
  ParadoxParameterReference getReference();

}
