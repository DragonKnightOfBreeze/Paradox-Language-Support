// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxLocalisationLocale extends ParadoxLocalisationNamedElement {

  @NotNull
  PsiElement getLocaleId();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationLocale setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

}
