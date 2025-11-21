package mobappdev.example.nback_cimpl

class NBackHelper {

    private external fun createNBackString(size: Int, combinations: Int, percentMatch: Int, nBack: Int): IntArray

    fun generateNBackString(size: Int, combinations: Int, percentMatch: Int, nBack: Int): IntArray {
        return createNBackString(size, combinations, percentMatch, nBack)
    }

    companion object {
        init {
            System.loadLibrary("JniBridge")
        }
    }
}
