// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.psi.*;
import icu.windea.pls.lang.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxScriptParameterConditionParameter extends ParadoxConditionParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxScriptParameterConditionParameter setName(@NotNull String name);

  @NotNull
  String getValue();

  int getTextOffset();

  @NotNull
  ParadoxConditionParameterPsiReference getReference();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
