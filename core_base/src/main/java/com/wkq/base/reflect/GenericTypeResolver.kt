package com.wkq.base.reflect

import java.lang.reflect.ParameterizedType
import java.lang.reflect.Type
import java.lang.reflect.TypeVariable
import java.lang.reflect.WildcardType

/**
 * Resolve a generic argument declared by [genericBaseClass] from an instance's superclass chain.
 *
 * Type variables are substituted at every level, so hierarchies such as
 * `Page : FeaturePage<PageViewModel>` and
 * `FeaturePage<VM> : BaseVMActivity<PageBinding, VM>` resolve correctly.
 */
@Suppress("UNCHECKED_CAST")
internal fun <T : Any> resolveGenericClass(
    instance: Any,
    genericBaseClass: Class<*>,
    index: Int,
    expectedSupertype: Class<in T>
): Class<T> {
    require(index in genericBaseClass.typeParameters.indices) {
        "Generic type argument index $index is out of bounds for ${genericBaseClass.name}"
    }

    val resolvedType = findGenericArgument(
        currentClass = instance.javaClass,
        genericBaseClass = genericBaseClass,
        index = index,
        substitutions = emptyMap()
    )
    val resolvedClass = resolvedType.toRawClass()
        ?: throw IllegalStateException(
            "Unsupported generic type argument $resolvedType at index $index of " +
                "${genericBaseClass.name} for ${instance.javaClass.name}"
        )

    check(expectedSupertype.isAssignableFrom(resolvedClass)) {
        "Resolved ${resolvedClass.name} at index $index of ${genericBaseClass.name} for " +
            "${instance.javaClass.name}, but expected a subtype of ${expectedSupertype.name}. " +
            "The generic Signature may have been removed or rewritten by R8."
    }

    return resolvedClass as Class<T>
}

/**
 * Walk to the requested generic base while carrying type-variable substitutions from child to
 * parent. Resolving the first ParameterizedType is insufficient when an app introduces an
 * intermediate generic base class.
 */
private tailrec fun findGenericArgument(
    currentClass: Class<*>,
    genericBaseClass: Class<*>,
    index: Int,
    substitutions: Map<TypeVariable<*>, Type>
): Type {
    if (currentClass == genericBaseClass) {
        return resolveType(currentClass.typeParameters[index], substitutions)
    }

    val genericSuperclass = currentClass.genericSuperclass
        ?: throw IllegalStateException(
            "${genericBaseClass.name} is not a superclass of ${currentClass.name}"
        )

    val parentClass: Class<*>
    val parentSubstitutions: Map<TypeVariable<*>, Type>
    when (genericSuperclass) {
        is ParameterizedType -> {
            parentClass = genericSuperclass.rawType as? Class<*>
                ?: throw IllegalStateException(
                    "Unsupported raw superclass ${genericSuperclass.rawType} for ${currentClass.name}"
                )
            parentSubstitutions = parentClass.typeParameters
                .zip(genericSuperclass.actualTypeArguments)
                .associate { (variable, argument) ->
                    variable to resolveType(argument, substitutions)
                }
        }

        is Class<*> -> {
            parentClass = genericSuperclass
            parentSubstitutions = emptyMap()
        }

        else -> throw IllegalStateException(
            "Unsupported generic superclass $genericSuperclass for ${currentClass.name}"
        )
    }

    if (!genericBaseClass.isAssignableFrom(parentClass)) {
        throw IllegalStateException(
            "${genericBaseClass.name} is not a superclass of ${currentClass.name}"
        )
    }

    return findGenericArgument(
        currentClass = parentClass,
        genericBaseClass = genericBaseClass,
        index = index,
        substitutions = parentSubstitutions
    )
}

private tailrec fun resolveType(
    type: Type,
    substitutions: Map<TypeVariable<*>, Type>
): Type {
    val substituted = if (type is TypeVariable<*>) substitutions[type] else null
    return if (substituted != null && substituted != type) {
        resolveType(substituted, substitutions)
    } else {
        type
    }
}

private fun Type.toRawClass(): Class<*>? {
    return when (this) {
        is Class<*> -> this
        is ParameterizedType -> rawType as? Class<*>
        is WildcardType -> upperBounds.firstOrNull()?.toRawClass()
        else -> null
    }
}
