// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface CwtBlock extends CwtValue, CwtMemberContainer, CwtBoundMemberContainer {

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

    @NotNull List<@NotNull CwtStatement> getComponents();

    @NotNull Icon getIcon(@IconFlags int flags);

    @NotNull String getValue();

    @NotNull String getPresentableText();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
