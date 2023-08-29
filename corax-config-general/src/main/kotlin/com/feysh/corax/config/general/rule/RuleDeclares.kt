package com.feysh.corax.config.general.rule

import kotlinx.serialization.Required
import kotlinx.serialization.SerialName
import kotlinx.serialization.Serializable


interface IMethodSignature {
    val signature: String
    val subtypes: Boolean
    val provenance: String
    val ext: String
}

interface ISelectable : IMethodSignature {
    val enable: Boolean
}

interface IMethodGrouped : IMethodSignature {
    @Required
    @SerialName("kind")
    val group: String
}

interface IMethodAccessPath : IMethodSignature {
    @Required
    val arg: String
}

interface IMethodAccessPathGrouped: IMethodGrouped, IMethodAccessPath

@Serializable
data class MethodAccessPath(
    override val enable: Boolean = true,
    @Required
    @SerialName("kind")
    override val group: String,
    @Required
    override val signature: String,
    @Required
    override val subtypes: Boolean,
    override val arg: String = "",
    override val provenance: String,
    override val ext: String
) : IMethodAccessPathGrouped, ISelectable

