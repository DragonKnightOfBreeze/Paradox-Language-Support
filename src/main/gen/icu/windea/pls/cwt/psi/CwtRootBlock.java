// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.PsiRootBlock;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;

public interface CwtRootBlock extends CwtMemberContainer, PsiRootBlock, PsiListLikeElement {

  @NotNull
  List<CwtDocComment> getDocCommentList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull CwtRootBlock getMemberContainer();

  @NotNull List<@NotNull CwtMember> getMembers();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
