// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.localisation.reference.ParadoxLocalisationIconReference;
import javax.swing.Icon;

public interface ParadoxLocalisationIcon extends ParadoxLocalisationRichText, ParadoxLocalisationNamedElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable
  PsiElement getIconId();

  @Nullable
  PsiElement getIconParameter();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationIcon setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable
  ParadoxLocalisationIconReference getReference();

}
