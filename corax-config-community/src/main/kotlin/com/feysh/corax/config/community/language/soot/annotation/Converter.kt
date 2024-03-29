package com.feysh.corax.config.community.language.soot.annotation

import com.feysh.corax.config.community.language.soot.annotation.Converter.convertAnnotations
import com.feysh.corax.config.community.language.soot.annotation.Converter.convertParamAnnotations
import com.feysh.corax.config.community.language.soot.annotation.Converter.convertParamNames
import com.feysh.corax.config.general.utils.classTypeToSootTypeDesc
import soot.SootClass
import soot.SootMethod
import soot.tagkit.*

object Converter {
    /**
     * @return an annotation holder that contains all annotations in `tag`.
     * @see VisibilityAnnotationTag
     */
    fun convertAnnotations(
        tag: VisibilityAnnotationTag?
    ): AnnotationHolder {
        // in Soot, each VisibilityAnnotationTag may contain multiple annotations
        // (named AnnotationTag, which is a bit confusing).
        return if (tag == null || tag.annotations == null) AnnotationHolder.emptyHolder() else  // converts all annotations in tag
            AnnotationHolder.make(tag.annotations.map(Converter::convertAnnotation))
    }

    /**
     * @return an annotation holder that contains all annotations in `host`.
     * @see AbstractHost
     */
    fun convertAnnotations(host: AbstractHost): AnnotationHolder {
        val tag = host.getTag(VisibilityAnnotationTag.NAME) as? VisibilityAnnotationTag
        return convertAnnotations(tag)
    }

    /**
     * Converts all annotations of parameters of `sootMethod` to a list
     * of [AnnotationHolder], one for annotations of each parameter.
     *
     * @see VisibilityParameterAnnotationTag
     */
    fun convertParamAnnotations(
        sootMethod: SootMethod
    ): List<AnnotationHolder>? {
        // in Soot, each VisibilityParameterAnnotationTag contains
        // the annotations for all parameters in the SootMethod
        val tag = sootMethod.getTag(VisibilityParameterAnnotationTag.NAME) as? VisibilityParameterAnnotationTag
        return tag?.visibilityAnnotations?.map(Converter::convertAnnotations)
    }

    /**
     * Converts all names of parameters of `sootMethod` to a list.
     *
     * @see ParamNamesTag
     */
    fun convertParamNames(
        sootMethod: SootMethod
    ): List<String>? {
        // in Soot, each ParamNamesTag contains the names of all parameters in the SootMethod
        val tag = sootMethod.getTag(ParamNamesTag.NAME) as? ParamNamesTag
        return if (tag == null || tag.names.isEmpty()) null else tag.names
    }


    private fun convertAnnotation(tag: AnnotationTag): Annotation {
        // AnnotationTag is the class that represent an annotation in Soot
        val annotationType: String = classTypeToSootTypeDesc(tag.type)
        // converts all elements in tag
        val elements: Map<String, Element> = tag.elems.associate { it.name to convertAnnotationElement(it) }
        return Annotation(annotationType, elements)
    }


    private fun convertAnnotationElement(elem: AnnotationElem): Element {
        return when (elem) {
            is AnnotationStringElem -> {
                StringElement(elem.value)
            }

            is AnnotationClassElem -> {
                var className: String = elem.desc
                // Soot's .java front end has different representation from .class
                // front end for AnnotationClassElem, and here we need to remove
                // extra characters generated by .java frontend
                val iBracket = className.indexOf('<')
                if (iBracket != -1) {
                    className = className.replace("java/lang/Class<", "")
                        .replace(">", "")
                }
                ClassElement(classTypeToSootTypeDesc(className))
            }

            is AnnotationAnnotationElem -> {
                AnnotationElement(convertAnnotation(elem.value))
            }

            is AnnotationArrayElem -> {
                ArrayElement(elem.values.map(Converter::convertAnnotationElement))
            }

            is AnnotationEnumElem -> {
                EnumElement(classTypeToSootTypeDesc(elem.typeName), elem.constantName)
            }

            is AnnotationIntElem -> {
                IntElement(elem.value)
            }

            is AnnotationBooleanElem -> {
                BooleanElement(elem.value)
            }

            is AnnotationFloatElem -> {
                FloatElement(elem.value)
            }

            is AnnotationDoubleElem -> {
                DoubleElement(elem.value)
            }

            is AnnotationLongElem -> {
                LongElement(elem.value)
            }

            else -> {
                throw IllegalStateException("Unable to handle AnnotationElem: $elem")
            }
        }
    }
}

fun AbstractHost.getAnnotations(): MutableCollection<Annotation> = convertAnnotations(this).annotations
fun AbstractHost.getAnnotation(annotationType: String) = convertAnnotations(this).getAnnotation(annotationType)

fun SootMethod.getParamAnnotations(index: Int): Collection<Annotation> =
    convertParamAnnotations(this)?.getOrNull(index)?.annotations ?: emptyList()

fun SootMethod.getParamName(index: Int): String? =
    convertParamNames(this)?.getOrNull(index)


val SootClass.isAnonymousClass: Boolean get() = this.name.matches("\\$\\d+".toRegex())
val SootClass.isMemberClass : Boolean get() = this.hasOuterClass()