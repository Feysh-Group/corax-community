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

@file:Suppress("MemberVisibilityCanBePrivate")

package com.feysh.corax.config.general.model.javaee

import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.SAOptions
import kotlinx.serialization.Serializable


@Suppress("PropertyName")
object JavaeeFrameworkConfigs : CheckerUnit() {
    @Serializable
    class Options : SAOptions {
        val REQUEST_MAPPING_ANNOTATION_TYPES_SPRING = setOf(
            "Lorg/springframework/web/bind/annotation/RequestMapping;",
            "Lorg/springframework/web/bind/annotation/GetMapping;",
            "Lorg/springframework/web/bind/annotation/PostMapping;",
            "Lorg/springframework/web/bind/annotation/PutMapping;",
            "Lorg/springframework/web/bind/annotation/DeleteMapping;",
            "Lorg/springframework/web/bind/annotation/PatchMapping;"
        )

        val REQUEST_PARAM_ANNOTATION_TYPES_SPRING = setOf(
            "Lorg/springframework/web/bind/annotation/RequestParam;",
            "Lorg/springframework/web/bind/annotation/PathVariable;",
        )

        val REQUEST_MAPPING_ANNOTATION_JAVAX_WS_RS = setOf(
            "Ljavax/ws/rs/GET;",
            "Ljavax/ws/rs/POST;",
            "Ljavax/ws/rs/PUT;",
            "Ljavax/ws/rs/DELETE;",
            "Ljavax/ws/rs/HEAD;",
            "Ljavax/ws/rs/OPTIONS;",
        )

        val REQUEST_PARAM_ANNOTATION_TYPES_JAVAX_WS_RS = setOf(
            "Ljavax/ws/rs/QueryParam;",
            "Ljavax/ws/rs/PathParam;"
        )

        val REQUEST_MAPPING_ANNOTATION_TYPES get() = REQUEST_MAPPING_ANNOTATION_TYPES_SPRING + REQUEST_MAPPING_ANNOTATION_JAVAX_WS_RS
    }

    var option: Options = Options()
}