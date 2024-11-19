package com.bopr.android.smailer.util

import com.bopr.android.smailer.util.Bits.Companion.bit
import org.junit.Assert.assertEquals
import org.junit.Assert.assertFalse
import org.junit.Assert.assertTrue
import org.junit.Test

class BitsTest {

    @Test
    fun `bit() function`() {
        assertEquals(Bits(0b0001), bit(0))
        assertEquals(Bits(0b0010), bit(1))
        assertEquals(Bits(0b0100), bit(2))
        assertEquals(Bits(0b1000), bit(3))
    }

    @Test
    fun `set by index operator`() {
        val bits = Bits()
        bits[0] = true
        bits[3] = true

        assertEquals(Bits(0b1001), bits)

        bits[0] = false
        assertEquals(Bits(0b1000), bits)

        bits[3] = false
        assertEquals(Bits(0b0000), bits)
    }

    @Test
    fun `get by index operator`() {
        assertTrue(Bits(0b1001)[0])
        assertTrue(Bits(0b1001)[3])
        assertFalse(Bits(0b1001)[2])
    }

    @Test
    fun `+ operator`() {
        assertEquals(Bits(0b0001), Bits(0b0000) + Bits(0b0001))
        assertEquals(Bits(0b0011), Bits(0b0010) + Bits(0b0001))
        assertEquals(Bits(0b0111), Bits(0b0111) + Bits(0b0100))
    }

    @Test
    fun `+= operator`() {
        var bits = Bits(0b0000)
        bits += Bits(0b0001)
        assertEquals(Bits(0b0001), bits)

        bits += Bits(0b0100)
        assertEquals(Bits(0b0101), bits)

        bits += Bits(0b0010)
        assertEquals(Bits(0b0111), bits)
    }

    @Test
    fun `- operator`() {
        assertEquals(Bits(0b1000), Bits(0b1001) - Bits(0b0001))
        assertEquals(Bits(0b1000), Bits(0b1000) - Bits(0b0001))
        assertEquals(Bits(0b0111), Bits(0b1111) - Bits(0b1000))
    }

    @Test
    fun `-= operator`() {
        var bits = Bits(0b1111)
        bits -= Bits(0b0001)
        assertEquals(Bits(0b1110), bits)

        bits -= Bits(0b0100)
        assertEquals(Bits(0b1010), bits)

        bits -= Bits(0b0010)
        assertEquals(Bits(0b1000), bits)
    }

    @Test
    fun `in operator`() {
        assertTrue(Bits(0b0001) in Bits(0b0001))
        assertFalse(Bits(0b0010) in Bits(0b0001))
        assertTrue(Bits(0b0010) in Bits(0b0011))
        assertTrue(Bits(0b0100) in Bits(0b0111))
        assertTrue(Bits(0b0110) in Bits(0b1111))
        assertTrue(Bits(0b1111) in Bits(0b1111))
        assertFalse(Bits(0b0110) in Bits(0b1001))
    }

}