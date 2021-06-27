// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CwtString extends CwtValue, CwtNamedElement {

  @NotNull
  PsiElement getStringToken();

  @NotNull
  String getValue();

  @NotNull
  String getTruncatedValue();

  @NotNull
  String getStringValue();

  @NotNull
  String getName();

  @NotNull
  CwtString setName(@NotNull String name);

  @NotNull
  PsiElement getNameIdentifier();

}
