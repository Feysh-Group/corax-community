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

@file:Suppress("NOTHING_TO_INLINE")

package com.feysh.corax.config.general.common.collect

import com.google.common.collect.Sets
import kotlinx.collections.immutable.PersistentSet
import kotlinx.collections.immutable.persistentHashSetOf

object Sets {
    inline fun <E> newHybridSet(): MutableSet<E> {
        return HashSet()
    }

    inline fun <E> newPersistentHashSetBuilder(): PersistentSet.Builder<E> {
        return persistentHashSetOf<E>().builder()
    }

    inline fun <E> newSet(): MutableSet<E> {
        return HashSet()
    }

    inline fun <E> newConcurrentHashSet(): MutableSet<E> {
        return Sets.newConcurrentHashSet()
    }
}