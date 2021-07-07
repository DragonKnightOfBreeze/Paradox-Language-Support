// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.localisation.reference.ParadoxLocalisationCommandFieldReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandField extends ParadoxLocalisationCommandIdentifier, ParadoxLocalisationNamedElement {

  @Nullable
  ParadoxLocalisationPropertyReference getPropertyReference();

  @Nullable
  PsiElement getCommandFieldId();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationCommandField setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  ParadoxLocalisationCommandFieldReference getReference();

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
