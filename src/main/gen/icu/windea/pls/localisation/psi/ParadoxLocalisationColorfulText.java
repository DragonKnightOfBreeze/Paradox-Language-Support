// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface ParadoxLocalisationColorfulText extends ParadoxLocalisationRichText, ParadoxLocalisationNamedElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationColorfulText setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

}
