/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.mobeegal.android.util;

/**
 * @author jyothsna
 */
public class EncryptionDecryption
{
    public String EncryptionDecryption(String enteredText, String key)
    {
        if (key == "")
        {
            return enteredText;
        }
        if (key.contains(" "))
        {
            key.replace(" ", "");
        }
        int len = key.length();
        int textLength = enteredText.length();
        if (len < 8)
        {
            return "";
        }
        if (len > 32)
        {
            len = 32;
        }
        char[] keyChar = new char[len];
        char[] textChar = new char[textLength];
        for (int i = 0; i < textLength; i++)
        {
            textChar[i] = enteredText.charAt(i);
        }
        for (int i = 0; i < len; i++)
        {
            keyChar[i] = key.charAt(i);
        }
        //conversion of key character into bytes
        byte[] keyByte = key.getBytes();
        for (int i = 0; i < len; i++)
        {
            keyByte[i] = (byte) (((int) keyChar[i]) & 0x1F);
        }

        for (int i = 0, j = 0; i < textLength; i++)
        {
            int e = (int) textChar[i];
            byte f = (byte) e;
            //boolean bool = (boolean)enteredTextByte[i];
            byte b = (byte) (f & 0xE0);
            if (b != 0)
            {
                textChar[i] = (char) (f ^ keyByte[j]);
            }
            j = (j + 1) % len;
        }
        return (new String(textChar));
    }
}


