package com.feysh.corax.config.community.language.soot.annotation;

import javax.annotation.Nullable;
import java.util.Collection;

/**
 * Represents objects that can be attached annotations.
 * <p>
 * Currently, only {@code JClass}, {@code JMethod}, and {@code JField}
 * implements this interface. Besides, annotations on parameters
 * are supported, and they are stored in {@code JMethod} instead of
 * parameters in IR.
 * <p>
 * TODO: let other program elements (e.g., {@code Var} implements
 *  this interface.
 */
public interface Annotated {

    /**
     * @return {@code true} if this annotated object has an annotation
     * of {@code annotationType}.
     */
    boolean hasAnnotation(String annotationType);

    /**
     * @return the {@link Annotation} of type {@code annotationType} if
     * it is present in this annotated; otherwise, {@code null} is returned.
     */
    @Nullable
    Annotation getAnnotation(String annotationType);

    /**
     * @return all annotations in this annotated object.
     */
    Collection<Annotation> getAnnotations();
}
