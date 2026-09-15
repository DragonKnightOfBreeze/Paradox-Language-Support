// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface ParadoxLocalisationConceptCommand extends ParadoxLocalisationRichText {

  @Nullable
  ParadoxLocalisationConceptName getConceptName();

  @Nullable
  ParadoxLocalisationConceptString getConceptString();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getName();

  @NotNull ParadoxLocalisationConceptCommand setName(@NotNull String name);

  @NotNull String getPresentableText();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
