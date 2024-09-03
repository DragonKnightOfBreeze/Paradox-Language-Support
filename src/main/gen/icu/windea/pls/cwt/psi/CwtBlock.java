// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;
import javax.swing.Icon;

public interface CwtBlock extends CwtValue, CwtBlockElement {

  @NotNull
  List<CwtDocumentationComment> getDocumentationCommentList();

  @NotNull
  List<CwtOption> getOptionList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  CwtBlock setName(@NotNull String name);

  @NotNull
  String getValue();

  @NotNull
  CwtValue setValue(@NotNull String value);

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

  @NotNull
  CwtType getType();

  @Nullable
  CwtConfigType getConfigType();

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  SearchScope getUseScope();

}
