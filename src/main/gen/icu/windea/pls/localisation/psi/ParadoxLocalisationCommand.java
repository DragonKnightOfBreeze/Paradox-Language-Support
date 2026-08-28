// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;

public interface ParadoxLocalisationCommand extends ParadoxLocalisationRichText, ParadoxLocalisationInterpolation, ParadoxLocalisationArgumentAwareElement {

    @Nullable
    ParadoxLocalisationCommandText getCommandText();

    @Nullable ParadoxLocalisationCommandArgument getArgumentElement();

    @NotNull Icon getIcon(@IconFlags int flags);

    @NotNull String getPresentableText();

    @NotNull GlobalSearchScope getResolveScope();

    @NotNull SearchScope getUseScope();

    @NotNull ItemPresentation getPresentation();

}
