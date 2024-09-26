/*
 *  CoraxJava - a Java Static Analysis Framework
 *  Copyright (C) 2024.  Feysh-Tech Group
 *
 *  This library is free software; you can redistribute it and/or
 *  modify it under the terms of the GNU Lesser General Public
 *  License as published by the Free Software Foundation; either
 *  version 2.1 of the License, or (at your option) any later version.
 *
 *  This library is distributed in the hope that it will be useful,
 *  but WITHOUT ANY WARRANTY; without even the implied warranty of
 *  MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the GNU
 *  Lesser General Public License for more details.
 *
 *  You should have received a copy of the GNU Lesser General Public
 *  License along with this library; if not, write to the Free Software
 *  Foundation, Inc., 51 Franklin Street, Fifth Floor, Boston, MA  02110-1301  USA
 */

package com.feysh.corax.config.community.language.classes

/**
 * Provides names of special classes.
 */
object ClassNames {
    // Names of special important classes
    const val OBJECT = "java.lang.Object"
    const val SERIALIZABLE = "java.io.Serializable"
    const val CLONEABLE = "java.lang.Cloneable"
    const val CLASS = "java.lang.Class"
    const val ARRAY = "java.lang.reflect.Array"
    const val CONSTRUCTOR = "java.lang.reflect.Constructor"
    const val METHOD = "java.lang.reflect.Method"
    const val FIELD = "java.lang.reflect.Field"
    const val STRING = "java.lang.String"
    const val STRING_BUILDER = "java.lang.StringBuilder"
    const val STRING_BUFFER = "java.lang.StringBuffer"
    const val BOOLEAN = "java.lang.Boolean"
    const val BYTE = "java.lang.Byte"
    const val SHORT = "java.lang.Short"
    const val CHARACTER = "java.lang.Character"
    const val INTEGER = "java.lang.Integer"
    const val LONG = "java.lang.Long"
    const val FLOAT = "java.lang.Float"
    const val DOUBLE = "java.lang.Double"
    const val VOID = "java.lang.Void"
    const val THREAD = "java.lang.Thread"
    const val THREAD_GROUP = "java.lang.ThreadGroup"
    const val THROWABLE = "java.lang.Throwable"
    const val ERROR = "java.lang.Error"
    const val EXCEPTION = "java.lang.Exception"

    // Names of invokedynamic-related classes
    const val CALL_SITE = "java.lang.invoke.CallSite"
    const val METHOD_HANDLE = "java.lang.invoke.MethodHandle"
    const val LOOKUP = "java.lang.invoke.MethodHandles\$Lookup"
    const val VAR_HANDLE = "java.lang.invoke.VarHandle"
    const val METHOD_TYPE = "java.lang.invoke.MethodType"

    // Names of special exceptions
    const val ABSTRACT_METHOD_ERROR = "java.lang.AbstractMethodError"
    const val ARITHMETIC_EXCEPTION = "java.lang.ArithmeticException"
    const val ARRAY_INDEX_OUT_OF_BOUNDS_EXCEPTION = "java.lang.ArrayIndexOutOfBoundsException"
    const val ARRAY_STORE_EXCEPTION = "java.lang.ArrayStoreException"
    const val CLASS_CAST_EXCEPTION = "java.lang.ClassCastException"
    const val CLASS_NOT_FOUND_EXCEPTION = "java.lang.ClassNotFoundException"
    const val CLONE_NOT_SUPPORTED_EXCEPTION = "java.lang.CloneNotSupportedException"
    const val EXCEPTION_IN_INITIALIZER_ERROR = "java.lang.ExceptionInInitializerError"
    const val ILLEGAL_ACCESS_ERROR = "java.lang.IllegalAccessError"
    const val ILLEGAL_MONITOR_STATE_EXCEPTION = "java.lang.IllegalMonitorStateException"
    const val INCOMPATIBLE_CLASS_CHANGE_ERROR = "java.lang.IncompatibleClassChangeError"
    const val INDEX_OUT_OF_BOUNDS_EXCEPTION = "java.lang.IndexOutOfBoundsException"
    const val INSTANTIATION_ERROR = "java.lang.InstantiationError"
    const val INTERNAL_ERROR = "java.lang.InternalError"
    const val INTERRUPTED_EXCEPTION = "java.lang.InterruptedException"
    const val LINKAGE_ERROR = "java.lang.LinkageError"
    const val NEGATIVE_ARRAY_SIZE_EXCEPTION = "java.lang.NegativeArraySizeException"
    const val NO_CLASS_DEF_FOUND_ERROR = "java.lang.NoClassDefFoundError"
    const val NO_SUCH_FIELD_ERROR = "java.lang.NoSuchFieldError"
    const val NO_SUCH_METHOD_ERROR = "java.lang.NoSuchMethodError"
    const val NULL_POINTER_EXCEPTION = "java.lang.NullPointerException"
    const val OUT_OF_MEMORY_ERROR = "java.lang.OutOfMemoryError"
    const val RUNTIME_EXCEPTION = "java.lang.RuntimeException"
    const val STACK_OVERFLOW_ERROR = "java.lang.StackOverflowError"
    const val UNKNOWN_ERROR = "java.lang.UnknownError"
    const val UNSATISFIED_LINK_ERROR = "java.lang.UnsatisfiedLinkError"
    const val VERIFY_ERROR = "java.lang.VerifyError"
    const val ANNOTATION = "java.lang.annotation.Annotation"
}
