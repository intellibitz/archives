/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.util

/**
 * @author jyothsna
 */
class EncryptionDecryption {
    fun EncryptionDecryption(enteredText: String, key: String): String {
        var key = key
        if (key == "") {
            return enteredText
        }
        if (key.contains(" ")) {
            key.replace(" ", "")
        }
        var len = key.length
        val textLength = enteredText.length
        if (len < 8) {
            return ""
        }
        if (len > 32) {
            len = 32
        }
        val keyChar = CharArray(len)
        val textChar = CharArray(textLength)
        for (i in 0 until textLength) {
            textChar[i] = enteredText[i]
        }
        for (i in 0 until len) {
            keyChar[i] = key[i]
        }
        //conversion of key character into bytes
        val keyByte = key.toByteArray()
        for (i in 0 until len) {
            keyByte[i] = (keyChar[i].code and 0x1F).toByte()
        }

        var j = 0
        for (i in 0 until textLength) {
            val e = textChar[i].code
            val f = e.toByte()
            //boolean bool = (boolean)enteredTextByte[i];
            val b = (f.toInt() and 0xE0).toByte()
            if (b.toInt() != 0) {
                textChar[i] = (f.toInt() xor keyByte[j].toInt()).toChar()
            }
            j = (j + 1) % len
        }
        return String(textChar)
    }
}
