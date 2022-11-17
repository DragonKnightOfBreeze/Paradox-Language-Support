// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;

public interface ParadoxLocalisationCommandScope extends ParadoxLocalisationCommandIdentifier {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationCommandScope setName(@NotNull String name);

  @NotNull
  ParadoxLocalisationCommandScopePsiReference getReference();

  @Nullable
  ParadoxLocalisationCommandScope getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
