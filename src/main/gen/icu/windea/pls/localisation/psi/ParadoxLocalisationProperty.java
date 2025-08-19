// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.StubBasedPsiElement;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyStub;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.model.ParadoxLocalisationType;
import javax.swing.Icon;

public interface ParadoxLocalisationProperty extends ParadoxLocalisationNamedElement, StubBasedPsiElement<ParadoxLocalisationPropertyStub> {

  @NotNull
  ParadoxLocalisationPropertyKey getPropertyKey();

  @Nullable
  ParadoxLocalisationPropertyValue getPropertyValue();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationProperty setName(@NotNull String name);

  @NotNull PsiElement getNameIdentifier();

  int getTextOffset();

  @Nullable ParadoxLocalisationType getType();

  @Nullable String getValue();

  @NotNull PsiElement setValue(@NotNull String value);

  @NotNull IElementType getIElementType();

  boolean isEquivalentTo(@NotNull PsiElement another);

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
