// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiListLikeElement;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.core.psi.PsiRootBlock;
import org.jetbrains.annotations.NotNull;

import java.util.List;

public interface ParadoxScriptRootBlock extends ParadoxScriptMemberContainer, PsiRootBlock, PsiListLikeElement {

  @NotNull
  List<ParadoxScriptNormalConditionalBlock> getNormalConditionalBlockList();

  @NotNull
  List<ParadoxScriptProperty> getPropertyList();

  @NotNull
  List<ParadoxScriptScriptedVariable> getScriptedVariableList();

  @NotNull
  List<ParadoxScriptValue> getValueList();

  @NotNull ParadoxScriptRootBlock getMemberContainer();

  @NotNull List<@NotNull ParadoxScriptMember> getMembers();

  @NotNull List<@NotNull PsiElement> getComponents();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
