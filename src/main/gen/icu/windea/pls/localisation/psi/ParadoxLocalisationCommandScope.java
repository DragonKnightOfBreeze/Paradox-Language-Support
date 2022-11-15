// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import org.jetbrains.annotations.*;
import icu.windea.pls.localisation.references.ParadoxLocalisationCommandScopeReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandScope extends ParadoxLocalisationCommandIdentifier {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationCommandScope setName(@NotNull String name);

  @NotNull
  ParadoxLocalisationCommandScopeReference getReference();

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
