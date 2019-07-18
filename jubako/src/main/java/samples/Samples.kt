import com.justeat.jubako.data.PaginatedLiveData
import kotlinx.coroutines.delay

internal object Samples {
    internal fun samplePaginatedLiveData() {
        PaginatedLiveData<String> {
            hasMore = { loaded.size < 100 }
            nextPage = {
                delay(100)
                listOf("Hello")
            }
        }
    }
}