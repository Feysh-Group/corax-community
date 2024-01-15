package com.feysh.corax.config.community.checkers.frameworks.persistence.ibatis.io

import soot.RefType
import soot.Scene

object Resources {
    fun classForName(scene: Scene, className: String): RefType? {
        return scene.getSootClassUnsafe(className, false)?.type
    }
}
