// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiRootBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface CwtRootBlock extends PsiRootBlock, CwtMemberContainer {

  @NotNull
  List<CwtDocComment> getDocCommentList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull String getValue();

  @NotNull CwtRootBlock getMemberContainer();

  @NotNull List<@NotNull CwtMember> getMembers();

  @NotNull List<@NotNull CwtStatement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

  @NotNull ItemPresentation getPresentation();

}
