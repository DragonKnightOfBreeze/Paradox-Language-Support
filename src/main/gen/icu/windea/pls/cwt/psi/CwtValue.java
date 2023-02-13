// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.NavigatablePsiElement;
import com.intellij.navigation.ItemPresentation;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtValue extends NavigatablePsiElement {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  String getValue();

  @NotNull
  CwtValue setValue(@NotNull String value);

  @NotNull
  ItemPresentation getPresentation();

}
