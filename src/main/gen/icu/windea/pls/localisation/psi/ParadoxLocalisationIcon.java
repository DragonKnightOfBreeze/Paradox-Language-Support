// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import icu.windea.pls.localisation.references.ParadoxLocalisationIconReference;
import javax.swing.Icon;

public interface ParadoxLocalisationIcon extends ParadoxLocalisationRichText {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxLocalisationIcon setName(@NotNull String name);

  int getFrame();

  @Nullable
  ParadoxLocalisationIconReference getReference();

}
