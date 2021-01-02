import com.example.crosswordToLearn.BadCrosswordDataTest
import com.example.crosswordToLearn.GameActivity
import com.example.crosswordToLearn.getContext
import com.example.crosswordToLearn.getTestContext
import org.junit.Test
import java.io.File
import java.io.FileOutputStream

class BadCrosswordStateInstrumentedTest : BadCrosswordDataTest() {

    override fun spoil() {
        File(getContext().filesDir, "${crossword.title}${GameActivity.STATE_SUFFIX}").apply {
            FileOutputStream(this).use {
                getTestContext().resources.assets.open("tooLongWordsData.json")
            }
        }
    }

    @Test
    fun testData() {
        test()
    }
}