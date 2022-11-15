// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.localisation.references.ParadoxLocalisationLocaleReference;
import javax.swing.Icon;

public interface ParadoxLocalisationLocale extends NavigatablePsiElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationLocale setName(@NotNull String name);

  @NotNull
  ParadoxLocalisationLocaleReference getReference();

}
