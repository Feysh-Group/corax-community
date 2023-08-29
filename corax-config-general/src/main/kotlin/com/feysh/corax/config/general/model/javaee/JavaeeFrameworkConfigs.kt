@file:Suppress("MemberVisibilityCanBePrivate")

package com.feysh.corax.config.general.model.javaee

import com.feysh.corax.config.api.CheckerUnit
import com.feysh.corax.config.api.SAOptions
import kotlinx.serialization.Serializable


@Suppress("PropertyName")
object JavaeeFrameworkConfigs: CheckerUnit() {
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

        val REQUEST_MAPPING_ANNOTATION_JAVAX_WS_RS = setOf(
            "Ljavax/ws/rs/GET;",
            "Ljavax/ws/rs/POST;",
            "Ljavax/ws/rs/PUT;",
            "Ljavax/ws/rs/DELETE;",
            "Ljavax/ws/rs/HEAD;",
            "Ljavax/ws/rs/OPTIONS;",
        )

        val REQUEST_PARAM_ANNOTATION_TYPES = setOf(
            "Lorg/springframework/web/bind/annotation/RequestParam;",
            "Lorg/springframework/web/bind/annotation/PathVariable;"
        )

        val REQUEST_MAPPING_ANNOTATION_TYPES get() = REQUEST_MAPPING_ANNOTATION_TYPES_SPRING + REQUEST_MAPPING_ANNOTATION_JAVAX_WS_RS
    }
    var option: Options = Options()
}