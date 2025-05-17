// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;

public interface CwtRootBlock extends CwtBlockElement {

  @NotNull
  List<CwtDocComment> getDocCommentList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull ItemPresentation getPresentation();

  @NotNull SearchScope getUseScope();

}
