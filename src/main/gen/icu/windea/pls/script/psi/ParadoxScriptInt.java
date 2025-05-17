// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.ContributedReferenceHost;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.model.ParadoxType;

public interface ParadoxScriptInt extends ParadoxScriptValue, ContributedReferenceHost, PsiLiteralValue {

  @NotNull String getValue();

  int getIntValue();

  @NotNull ParadoxType getType();

  @Nullable PsiReference getReference();

  @NotNull PsiReference @NotNull [] getReferences();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
