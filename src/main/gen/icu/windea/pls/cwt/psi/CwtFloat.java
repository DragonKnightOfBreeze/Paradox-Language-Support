// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.psi.PsiLiteralValue;
import com.intellij.navigation.ItemPresentation;
import com.intellij.psi.search.SearchScope;
import icu.windea.pls.config.CwtConfigType;
import icu.windea.pls.model.CwtType;

public interface CwtFloat extends CwtValue, PsiLiteralValue {

  float getFloatValue();

  @NotNull CwtType getType();

  @Nullable CwtConfigType getConfigType();

  @NotNull ItemPresentation getPresentation();

  @NotNull SearchScope getUseScope();

}
