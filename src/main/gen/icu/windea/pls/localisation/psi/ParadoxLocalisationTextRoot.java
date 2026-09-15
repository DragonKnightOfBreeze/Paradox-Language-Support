// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import java.util.List;

public interface ParadoxLocalisationTextRoot extends PsiElement {

  @NotNull
  List<ParadoxLocalisationRichText> getRichTextList();

  @Nullable
  ParadoxLocalisationStringVariantSet getStringVariantSet();

  @Nullable
  ParadoxLocalisationTagPart getTagPart();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
