// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.localisation.reference.ParadoxLocalisationPropertyReferenceReference;

public interface ParadoxLocalisationPropertyReference extends ParadoxLocalisationRichText {

  @Nullable
  ParadoxLocalisationCommand getCommand();

  @Nullable
  PsiElement getPropertyReferenceId();

  @Nullable
  PsiElement getPropertyReferenceParameter();

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationPropertyReference setName(@NotNull String name);

  int getTextOffset();

  @Nullable
  ParadoxLocalisationPropertyReferenceReference getReference();

}
