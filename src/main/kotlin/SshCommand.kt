import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.IOException

import java.net.Socket





class SshCommand(private val host: String, private val port: Int, private val user: String, private val password: String) {
    // Source: https://codeflex.co/java-run-sudo-command-on-remote-linux-host/
    private fun executeCommand(command: String): String {
        var session: Session? = null
        var channel: ChannelExec? = null
        var returnText = ""

        try {
            session = JSch().getSession(user, host, port)
            session.setPassword(password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()
            channel = session.openChannel("exec") as ChannelExec

            channel.setCommand("sudo -S -p '' $command")
            //channel.setErrStream(System.err)
            val output = channel.outputStream
            channel.setPty(true)
            channel.connect()
            val inputForSudoPrompt = password + "\n"
            output.write(inputForSudoPrompt.toByteArray())
            output.flush()

            val input = channel.inputStream
            val tmp = ByteArray(1024)
            while (true) {
                while (input.available() > 0) {
                    val i: Int = input.read(tmp, 0, 1024)
                    if (i < 0) break
                    returnText += String(tmp, 0, i)
                }
                if (channel.isClosed) {
                    returnText += "Exit status: " + channel.exitStatus
                    break
                }
            }
            returnText = returnText.replace("\r\n", "\n")
            val startIndex = returnText.indexOf(inputForSudoPrompt)
            if (startIndex != -1) {
                // Strip password that's returned from inputStream
                returnText = returnText.substring(startIndex + inputForSudoPrompt.length)
            }
        } finally {
            session?.disconnect()
            channel?.disconnect()
        }
        return returnText
    }

    fun whoami(): String {
        return executeCommand("whoami")
    }

    fun shutdown(): String {
        return executeCommand("shutdown")
    }

    fun reboot(): String {
        return executeCommand("reboot")
    }

    fun isReachable(): Boolean {
        var returnValue: Boolean
        try {
            Socket(ipAddress, port).use { returnValue = true }
        } catch (_: IOException) {
            returnValue = false
        }
        return returnValue
    }
}
