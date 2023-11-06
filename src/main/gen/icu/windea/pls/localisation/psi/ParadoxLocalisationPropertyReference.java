// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

public interface ParadoxLocalisationPropertyReference extends ParadoxLocalisationRichText, NavigatablePsiElement {

  @Nullable
  ParadoxLocalisationCommand getCommand();

  @Nullable
  ParadoxLocalisationScriptedVariableReference getScriptedVariableReference();

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationPropertyReference setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationPropertyPsiReference getReference();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
