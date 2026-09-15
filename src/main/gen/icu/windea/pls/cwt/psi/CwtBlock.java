// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import javax.swing.Icon;

public interface CwtBlock extends CwtValue, PsiListLikeElement, CwtMemberContainer, CwtBoundMemberContainer {

  @NotNull
  List<CwtDocComment> getDocCommentList();

  @NotNull
  List<CwtOption> getOptionList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull CwtBlock getMemberContainer();

  @NotNull List<@NotNull CwtMember> getMembers();

  @Nullable PsiElement getLeftBound();

  @Nullable PsiElement getRightBound();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getValue();

  @NotNull CwtElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
