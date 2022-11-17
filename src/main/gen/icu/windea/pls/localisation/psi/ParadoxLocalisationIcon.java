// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

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
  ParadoxLocalisationIconPsiReference getReference();

}
