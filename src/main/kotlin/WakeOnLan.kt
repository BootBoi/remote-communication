import java.io.ByteArrayOutputStream
import java.net.DatagramPacket
import java.net.DatagramSocket
import java.net.InetAddress

class WakeOnLan(macAddress: String, private val port: Int = 9) {
    private val bytes: ByteArray = getMagicBytes(macAddress)
    private val address: InetAddress = getMulticastAddress()

    fun wakeUp() {
        val packet = DatagramPacket(bytes, bytes.size, address, port)
        val socket = DatagramSocket()
        socket.send(packet)
    }

    private fun getMagicBytes(macAddress: String): ByteArray {
        val bytes = ByteArrayOutputStream()

        for (i in 0..5)
            bytes.write(0xff)

        val macAddressBytes: ByteArray = parseHexString(macAddress)
        for (i in 0..15)
            bytes.write(macAddressBytes)

        bytes.flush()

        return bytes.toByteArray()
    }

    private fun parseHexString(string: String): ByteArray {
        val length: Int = string.length / 2
        val bytes = ByteArray(length)
        for (j in 0..length-1) {
            val i = j * 2
            bytes[j] = string.substring(i, i + 2).toInt(16).toByte()
        }
        return bytes
    }

    private fun getMulticastAddress(): InetAddress {
        return InetAddress.getByAddress(byteArrayOf(-1, -1, -1, -1))
    }
}
