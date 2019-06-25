package com.justeat.jubako

import android.view.ViewGroup
import androidx.arch.core.executor.testing.InstantTaskExecutorRule
import androidx.lifecycle.MutableLiveData
import com.justeat.jubako.extensions.add
import com.justeat.jubako.extensions.descriptionProvider
import com.nhaarman.mockitokotlin2.capture
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.runBlocking
import org.junit.Assert.assertEquals
import org.junit.Assert.assertTrue
import org.junit.Before
import org.junit.Rule
import org.junit.Test
import org.junit.rules.TestRule
import org.mockito.ArgumentCaptor
import org.mockito.Captor
import org.mockito.Mock
import org.mockito.Mockito.*
import org.mockito.MockitoAnnotations.initMocks

class ContentTest {

    @Rule
    @JvmField
    var rule: TestRule = InstantTaskExecutorRule()

    @Captor
    internal lateinit var assembleCaptor: ArgumentCaptor<Jubako.State>

    @Mock
    internal lateinit var mockLoadingState: MutableLiveData<Jubako.State>

    private lateinit var content: Jubako

    @Before
    fun setup() {
        initMocks(this)

        content = Jubako()
    }

    @Test
    fun loadPostsAssembledValue() {
        // given
        content.IO = Dispatchers.Unconfined
        content.loadingState = mockLoadingState

        // when
        content.load(MOCK_ASSEMBLER)

        // then
        verify(content.loadingState).postValue(capture(assembleCaptor))

        (assembleCaptor.value as Jubako.State.Assembled).let {
            val data = it.data
            assertEquals(MOCK_CONTENT_DESCRIPTION, data.contentDescriptions[0])
            assertEquals(MOCK_HOLDER_FACTORY, data.viewHolderFactories[0])
            assertEquals(MOCK_CONTENT_DESCRIPTION_ID, data.viewTypes[0])
        }
    }

    @Test
    fun loadSetsAssemblingValue() {
        // when
        content.load(MOCK_ASSEMBLER)

        // then
        assertTrue(content.loadingState.value is Jubako.State.Assembling)
    }

    @Test
    fun loadPostsAssembledErrorValue() {
        // given
        content.IO = Dispatchers.Unconfined
        val assembler = mock(JubakoAssembler::class.java)
        runBlocking {
            `when`(assembler.assemble()).thenThrow(RuntimeException())
        }

        // when
        content.load(assembler)

        // then
        assertTrue(content.loadingState.value is Jubako.State.AssembleError)
    }

    @Test
    fun loadWhenCalledAgainPostsAssembledValueOnlyOnce() {
        // given
        content.IO = Dispatchers.Unconfined
        content.loadingState = mockLoadingState

        // when
        content.load(MOCK_ASSEMBLER)
        content.load(MOCK_ASSEMBLER)

        // then
        verify(content.loadingState, times(1)).postValue(any())
    }

    @Test
    fun loadAfterResetPostsAssembledValueAgain() {
        // given
        content.IO = Dispatchers.Unconfined
        content.loadingState = mockLoadingState

        // when
        content.load(MOCK_ASSEMBLER)

        // then
        verify(content.loadingState, times(1)).postValue(any())

        // when
        content.reset()
        content.load(MOCK_ASSEMBLER)

        // then
        verify(content.loadingState, times(2)).postValue(any())
    }
}

private val MOCK_ASSEMBLER = SimpleJubakoAssembler {
    add(descriptionProvider { MOCK_CONTENT_DESCRIPTION })
}

private const val MOCK_CONTENT_DESCRIPTION_ID = "abc-123"
private val MOCK_HOLDER_FACTORY = object : JubakoAdapter.HolderFactory<String> {
    override fun createViewHolder(parent: ViewGroup) = null!!
}
private val MOCK_CONTENT_DESCRIPTION = ContentDescription(
    id = MOCK_CONTENT_DESCRIPTION_ID,
    viewHolderFactory = MOCK_HOLDER_FACTORY
)