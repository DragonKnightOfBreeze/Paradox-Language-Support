// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.localisation.reference.ParadoxLocalisationCommandFieldPsiReference;
import javax.swing.Icon;

public interface ParadoxLocalisationCommandField extends ParadoxLocalisationCommandIdentifier, ParadoxLocalisationNamedElement {

  @Nullable
  ParadoxLocalisationPropertyReference getPropertyReference();

  @Nullable
  PsiElement getCommandFieldId();

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @Nullable
  PsiElement getNameIdentifier();

  @Nullable
  ParadoxLocalisationCommandFieldPsiReference getReference();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  ParadoxLocalisationCommandIdentifier getPrevIdentifier();

  @Nullable
  ParadoxLocalisationCommandIdentifier getNextIdentifier();

}
