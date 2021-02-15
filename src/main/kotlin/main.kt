fun main(args: Array<String>) {
    //val wol = WakeOnLan(nasMac)
    //wol.wakeUp()
    //println("Sent WOL magic packet")
    val command = SshCommand(ipAddress, sshPort, sshUser, sshPassword)
    println("executed command " + command.powerOff())
}
