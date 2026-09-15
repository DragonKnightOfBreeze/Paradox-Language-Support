// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.StubBasedPsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import com.intellij.psi.tree.IElementType;
import icu.windea.pls.localisation.psi.stubs.ParadoxLocalisationPropertyListStub;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxLocalisationPropertyList extends PsiListLikeElement, StubBasedPsiElement<ParadoxLocalisationPropertyListStub> {

  @Nullable
  ParadoxLocalisationLocale getLocale();

  @NotNull
  List<ParadoxLocalisationProperty> getPropertyList();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull IElementType getIElementType();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
