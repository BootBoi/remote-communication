import com.jcraft.jsch.ChannelExec
import com.jcraft.jsch.JSch
import com.jcraft.jsch.Session
import java.io.IOException
import java.net.Socket

public class SshCommand(
    private val host: String,
    private val port: Int,
    private val user: String,
    private val password: String
) {
    // Source: https://codeflex.co/java-run-sudo-command-on-remote-linux-host/
    @Throws(SshCommandException::class)
    private fun executeCommand(command: String): String {
        var session: Session? = null
        var channel: ChannelExec? = null
        var returnText = ""
        val sudoCommand = "sudo -S -p '' $command"
        val inputForSudoPrompt = "$password\n"

        try {
            session = JSch().getSession(user, host, port)
            session.setPassword(password)
            session.setConfig("StrictHostKeyChecking", "no")
            session.connect()
            channel = session.openChannel("exec") as ChannelExec

            channel.setCommand(sudoCommand)
            //channel.setErrStream(System.err)
            val output = channel.outputStream
            channel.setPty(true)
            channel.connect()
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
                    if (channel.exitStatus != 0) {
                        throw SshCommandException(
                            "The SSH command $sudoCommand failed to execute and exited with status ${channel.exitStatus} and response ${
                                prepareReturnText(
                                    returnText,
                                    inputForSudoPrompt
                                )
                            }"
                        )
                    }
                    break
                }
            }
        } catch (exception: SshCommandException) {
            throw exception
        } catch (exception: Exception) {
            throw SshCommandException("An exception was thrown when executing the SSH command $sudoCommand: ${exception.message}")
        } finally {
            session?.disconnect()
            channel?.disconnect()
        }
        return prepareReturnText(returnText, inputForSudoPrompt)
    }

    private fun prepareReturnText(t: String, inputForSudoPrompt: String): String {
        val text = t.replace("\r\n", "\n")
        val startIndex = text.indexOf(inputForSudoPrompt)
        if (startIndex != -1) {
            // Strip password that's returned from inputStream
            return text.substring(startIndex + inputForSudoPrompt.length)
        }
        return text
    }

    @Throws(SshCommandException::class)
    fun whoAmI(): String {
        return executeCommand("whoami")
    }

    @Throws(SshCommandException::class)
    fun canExecuteAsRoot(): Boolean {
        return whoAmI().contains("root")
    }

    @Throws(SshCommandException::class)
    fun powerOff(): String {
        return executeCommand("poweroff")
    }

    @Throws(SshCommandException::class)
    fun reboot(): String {
        return executeCommand("reboot")
    }

    fun isReachable(): Boolean {
        var returnValue = false
        try {
            Socket(host, port).use { returnValue = true }
        } catch (_: IOException) {
            returnValue = false
        }
        return returnValue
    }
}
