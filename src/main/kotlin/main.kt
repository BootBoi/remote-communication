

fun main(args: Array<String>) {
    val wol = WakeOnLan(nasMac)
    wol.wakeUp()
    println("Sent WOL magic packet")
}
