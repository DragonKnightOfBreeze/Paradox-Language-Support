// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;
import com.intellij.openapi.util.Iconable.IconFlags;
import javax.swing.Icon;

public interface CwtBlock extends CwtValue, ICwtBlock {

  @NotNull
  List<CwtDocumentationComment> getDocumentationCommentList();

  @NotNull
  List<CwtOption> getOptionList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull
  Icon getIcon(@IconFlags int flags);

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

}
