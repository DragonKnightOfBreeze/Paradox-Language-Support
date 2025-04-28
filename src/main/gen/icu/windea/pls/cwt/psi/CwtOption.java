// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.CwtSeparatorType;
import javax.swing.Icon;

public interface CwtOption extends CwtNamedElement {

  @NotNull
  CwtOptionKey getOptionKey();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  CwtOption setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

  @Nullable
  String getValue();

  @NotNull
  CwtSeparatorType getSeparatorType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  SearchScope getUseScope();

}
