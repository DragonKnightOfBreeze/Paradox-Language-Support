// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.psi.search.GlobalSearchScope;
import com.intellij.psi.search.SearchScope;
import org.jetbrains.annotations.NotNull;
import org.jetbrains.annotations.Nullable;

import javax.swing.*;
import java.util.List;

public interface ParadoxLocalisationCommand extends ParadoxLocalisationRichText, ParadoxLocalisationInterpolation, ParadoxLocalisationArgumentAwareElement {

  @Nullable
  ParadoxLocalisationCommandText getCommandText();

  @NotNull
  List<ParadoxLocalisationContextTagPart> getContextTagPartList();

  @Nullable
  ParadoxLocalisationTagSensitiveText getTagSensitiveText();

  @Nullable ParadoxLocalisationCommandArgument getArgumentElement();

  @NotNull Icon getIcon(@IconFlags int flags);

  @NotNull String getPresentableText();

  @NotNull ParadoxLocalisationElementPresentation getPresentation();

  @NotNull GlobalSearchScope getResolveScope();

  @NotNull SearchScope getUseScope();

}
