// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxLocalisationPropertyList extends PsiListLikeElement {

  @Nullable
  ParadoxLocalisationLocale getLocale();

  @NotNull
  List<ParadoxLocalisationProperty> getPropertyList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  List<ParadoxLocalisationProperty> getComponents();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
