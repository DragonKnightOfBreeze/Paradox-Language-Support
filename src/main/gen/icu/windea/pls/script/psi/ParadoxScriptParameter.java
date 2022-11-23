// This is a generated file. Not intended for manual editing.
package icu.windea.pls.script.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import icu.windea.pls.core.psi.ParadoxParameter;
import com.intellij.openapi.util.Iconable.IconFlags;
import icu.windea.pls.core.references.ParadoxParameterPsiReference;
import javax.swing.Icon;

public interface ParadoxScriptParameter extends ParadoxParameter {

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @Nullable
  String getName();

  @NotNull
  ParadoxScriptParameter setName(@NotNull String name);

  int getTextOffset();

  @NotNull
  String getValue();

  @Nullable
  String getDefaultValue();

  @Nullable
  ParadoxParameterPsiReference getReference();

}
