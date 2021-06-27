// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface ParadoxLocalisationProperty extends ParadoxLocalisationNamedElement, StubBasedPsiElement<ParadoxLocalisationPropertyStub> {

  @NotNull
  ParadoxLocalisationPropertyKey getPropertyKey();

  @Nullable
  ParadoxLocalisationPropertyValue getPropertyValue();

  @Nullable
  PsiElement getNumber();

  @NotNull
  String getName();

  @NotNull
  ParadoxLocalisationProperty setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getValue();

}
