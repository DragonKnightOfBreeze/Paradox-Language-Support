// This is a generated file. Not intended for manual editing.
package com.windea.plugin.idea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtProperty extends CwtNamedElement {

  @NotNull
  CwtKey getKey();

  @Nullable
  CwtValue getValue();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getName();

  @NotNull
  PsiElement setName(@NotNull String name);

  @NotNull
  String getPropertyKey();

  @NotNull
  String getPropertyValue();

  //WARNING: getPropertyTruncatedValue(...) is skipped
  //matching getPropertyTruncatedValue(CwtProperty, ...)
  //methods are not found in CwtPsiImplUtil

}
