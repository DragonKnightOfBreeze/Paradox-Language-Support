// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

public interface ParadoxLocalisationTaggedParameter extends ParadoxLocalisationMacro {

  @Nullable PsiElement getIdElement();

  @Nullable String getName();

  @NotNull ParadoxLocalisationTaggedParameter setName(@NotNull String name);

  @NotNull String getPresentableText();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
