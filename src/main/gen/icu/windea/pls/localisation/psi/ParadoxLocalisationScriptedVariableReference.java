// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import icu.windea.pls.lang.psi.ParadoxScriptedVariableReference;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import com.intellij.psi.PsiReference;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.lang.references.ParadoxScriptedVariablePsiReference;
import icu.windea.pls.model.ParadoxType;
import javax.swing.Icon;

public interface ParadoxLocalisationScriptedVariableReference extends NavigatablePsiElement, ParadoxScriptedVariableReference {

  @Nullable PsiElement getIdElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @Nullable String getName();

  @NotNull ParadoxLocalisationScriptedVariableReference setName(@NotNull String name);

  @Nullable ParadoxScriptedVariablePsiReference getReference();

  @NotNull ParadoxType getType();

  @NotNull String getExpression();

  @NotNull ItemPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
