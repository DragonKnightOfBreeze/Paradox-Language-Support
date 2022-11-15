// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.localisation.references.ParadoxLocalisationCommandFieldReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandField extends ParadoxLocalisationCommandIdentifier {

  @Nullable
  ParadoxLocalisationPropertyReference getPropertyReference();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationCommandField setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationCommandFieldReference getReference();

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
