// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import icu.windea.pls.localisation.references.*;
import org.jetbrains.annotations.*;

import java.util.*;

public interface ParadoxLocalisationColorfulText extends ParadoxLocalisationRichText {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable
  String getName();

  @NotNull
  ParadoxLocalisationColorfulText setName(@NotNull String name);

  @Nullable
  ParadoxLocalisationColorPsiReference getReference();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
