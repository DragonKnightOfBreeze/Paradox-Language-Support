// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.localisation.references.ParadoxLocalisationPropertyPsiReference;

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

}
