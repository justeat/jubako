package com.justeat.jubako

import com.justeat.jubako.extensions.add
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Test
import org.mockito.Mockito.mock

class SimpleJubakoAssemblerTest {
    @Test
    @Throws(Exception::class)
    fun assemble_returns_list_from_constructor_delegate() {
        // act
        val assembler = SimpleJubakoAssembler {
            add(mock(ContentDescriptionProvider::class.java))
            add(mock(ContentDescriptionProvider::class.java))
            add(mock(ContentDescriptionProvider::class.java))
        }

        // assert
        runBlocking {
            assertEquals(3, assembler.assemble().size)
        }
    }
}