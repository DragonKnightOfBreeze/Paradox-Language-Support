// This is a generated file. Not intended for manual editing.
package icu.windea.pls.localisation.psi;

import com.intellij.navigation.*;
import com.intellij.psi.search.*;
import org.jetbrains.annotations.*;

import javax.swing.*;
import java.util.*;

public interface ParadoxLocalisationCommand extends ParadoxLocalisationRichText {

  @NotNull
  List<ParadoxLocalisationCommandIdentifier> getCommandIdentifierList();

  @Nullable
  ParadoxLocalisationConcept getConcept();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  ItemPresentation getPresentation();

  @NotNull
  GlobalSearchScope getResolveScope();

  @NotNull
  SearchScope getUseScope();

}
