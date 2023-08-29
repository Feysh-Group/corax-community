package com.feysh.corax.config.general.checkers

import com.feysh.corax.config.api.IViaType


// Feature: 暂时无作用
@Suppress("unused")
enum class ViaType(val desc: String) : IViaType {
    Str2int("string convert to integer"),
    Int2str("integer convert to string")
    ;
    override fun toString(): String = "via_$name"
}