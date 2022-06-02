// This is a generated file. Not intended for manual editing.
package icu.windea.pls.cwt.psi;

import java.util.List;
import org.jetbrains.annotations.*;
import com.intellij.psi.PsiElement;

public interface CwtRootBlock extends ICwtBlock {

  @NotNull
  List<CwtDocumentationComment> getDocumentationCommentList();

  @NotNull
  List<CwtOptionComment> getOptionCommentList();

  @NotNull
  List<CwtProperty> getPropertyList();

  @NotNull
  List<CwtValue> getValueList();

  @NotNull
  String getValue();

  boolean isEmpty();

  boolean isNotEmpty();

  @NotNull
  List<PsiElement> getComponents();

}
