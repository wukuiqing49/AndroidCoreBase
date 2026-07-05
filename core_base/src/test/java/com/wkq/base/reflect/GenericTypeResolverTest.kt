package com.wkq.base.reflect

import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Test

class GenericTypeResolverTest {

    @Test
    fun resolvesDirectGenericArgument() {
        val resolved = resolveGenericClass<Number>(
            instance = DirectChild(),
            genericBaseClass = GenericBase::class.java,
            index = 1,
            expectedSupertype = Number::class.java
        )

        assertEquals(Integer::class.java, resolved)
    }

    @Test
    fun resolvesTypeVariableAcrossIntermediateGenericClass() {
        val resolved = resolveGenericClass<Number>(
            instance = IntermediateChild(),
            genericBaseClass = GenericBase::class.java,
            index = 1,
            expectedSupertype = Number::class.java
        )

        assertEquals(java.lang.Long::class.java, resolved)
    }

    @Test
    fun resolvesAcrossNonGenericIntermediateClass() {
        val resolved = resolveGenericClass<Number>(
            instance = FixedChild(),
            genericBaseClass = GenericBase::class.java,
            index = 1,
            expectedSupertype = Number::class.java
        )

        assertEquals(java.lang.Double::class.java, resolved)
    }

    @Test
    fun resolvesRawClassFromParameterizedArgument() {
        val resolved = resolveGenericClass<List<*>>(
            instance = ParameterizedChild(),
            genericBaseClass = GenericBase::class.java,
            index = 0,
            expectedSupertype = List::class.java
        )

        assertEquals(List::class.java, resolved)
    }

    @Test
    fun rejectsATypeOutsideExpectedHierarchy() {
        val error = runCatching {
            resolveGenericClass<CharSequence>(
                instance = DirectChild(),
                genericBaseClass = GenericBase::class.java,
                index = 1,
                expectedSupertype = CharSequence::class.java
            )
        }.exceptionOrNull()

        assertTrue(error is IllegalStateException)
        assertTrue(error?.message.orEmpty().contains("Signature may have been removed or rewritten"))
    }

    private open class GenericBase<A, B>

    private class DirectChild : GenericBase<String, Int>()

    private open class GenericIntermediate<T> : GenericBase<String, T>()

    private class IntermediateChild : GenericIntermediate<Long>()

    private open class FixedIntermediate : GenericBase<String, Double>()

    private class FixedChild : FixedIntermediate()

    private class ParameterizedChild : GenericBase<List<String>, Int>()
}
