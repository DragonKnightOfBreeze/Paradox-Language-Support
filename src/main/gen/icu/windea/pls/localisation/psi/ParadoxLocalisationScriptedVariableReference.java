// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.lang.psi.*;
import icu.windea.pls.lang.references.*;
import icu.windea.pls.model.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxLocalisationScriptedVariableReference extends ParadoxScriptedVariableReference {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxLocalisationScriptedVariableReference setName(@NotNull String name);

  @Nullable
  ParadoxScriptedVariablePsiReference getReference();

  @NotNull
  ParadoxType getType();

  @NotNull
  String getExpression();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
