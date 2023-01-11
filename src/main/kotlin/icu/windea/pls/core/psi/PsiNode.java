package icu.windea.pls.core.psi;

import com.intellij.openapi.editor.colors.*;
import com.intellij.openapi.util.*;
import com.intellij.psi.*;
import org.jetbrains.annotations.*;

/**
 * @see PsiElement
 * @see PsiNodeReference
 */
public interface PsiNode {
	/**
	 * Returns the underlying (referencing) element of the reference.
	 *
	 * @return the underlying element of the reference.
	 */
	@NotNull
	PsiElement getElement();

	/**
	 * Returns the part of the underlying element which serves as a reference, or the complete
	 * text range of the element if the entire element is a reference.
	 * <p/>
	 * Sample: PsiElement representing a fully qualified name with multiple dedicated PsiReferences, each bound
	 * to the range it resolves to (skipping the '.' separator).
	 * <pre>
	 * PsiElement text: qualified.LongName
	 * PsiReferences:   [Ref1---]X[Ref2--]
	 * </pre>
	 * where {@code Ref1} would resolve to a "namespace" and {@code Ref2} to an "element".
	 *
	 * @return Relative range in element
	 */
	@NotNull
	TextRange getRangeInElement();

	@Nullable
	default TextAttributesKey getTextAttributesKey() {
		return null;
	}
}
