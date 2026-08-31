package cc.thevar.blukit.domain.protocol

import org.junit.Assert.assertNotNull
import org.junit.Assert.assertTrue
import org.junit.Test
import java.security.KeyPairGenerator
import java.security.spec.ECGenParameterSpec

class HandshakeStateMachineTest {

    @Test
    fun testHandshakeCreationAndParsing() {
        val kpg = KeyPairGenerator.getInstance("EC")
        kpg.initialize(ECGenParameterSpec("secp256r1"))
        val keyPair = kpg.generateKeyPair()

        val handshakeBytes = HandshakeProtocol.createHandshake(keyPair)
        
        assertTrue(HandshakeProtocol.isHandshake(handshakeBytes))
        
        val parsedPublicKeyBytes = HandshakeProtocol.parseHandshake(handshakeBytes)
        assertNotNull(parsedPublicKeyBytes)
        assertTrue(parsedPublicKeyBytes!!.contentEquals(keyPair.public.encoded))
    }
}
