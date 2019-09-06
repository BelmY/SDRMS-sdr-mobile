package space.sdrmaker.sdrmobile.dsp

class Upsampler (private val input: Iterator<Float>, val factor: Int) : Iterator<Float> {
    var state = -1

    override fun next(): Float {
        state++
        return if (state.rem(factor) == 0) {
            state = 0
            input.next()
        }
        else 0F
    }

    override fun hasNext(): Boolean = input.hasNext()
}

class Downsampler (val input: Iterator<Float>, val factor: Int) : Iterator<Float> {

    override fun next(): Float {
        val out = input.next()
        for (i in 1 until factor)
            input.next()

        return out
    }

    override fun hasNext(): Boolean = input.hasNext()
}