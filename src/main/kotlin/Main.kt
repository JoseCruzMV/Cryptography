package cryptography

import java.awt.Color
import java.awt.image.BufferedImage
import java.io.File
import java.io.IOException
import javax.imageio.ImageIO

enum class MenuOptions(val option: String) {
    HIDE("hide"),
    SHOW("show"),
    EXIT("exit")
}

fun main() {
    printMenu()
}

const val ESCAPE_PATRON = "000000000000000000000011"
/* Show options to user and gets election */
fun printMenu() {
    while (true) {
        println("Task (hide, show, exit):")
        when(val input = readLine()!!) {
            MenuOptions.HIDE.option -> hideMessage()
            MenuOptions.SHOW.option -> showMessage()
            MenuOptions.EXIT.option -> break
            else -> println("Wrong task: $input")
        }
    }

    println("Bye!")
}

/* Shows the message for a given image */
fun showMessage() {
    println("Input image file:")
    val fileName = readLine()!!

    println("Password:")
    val password = readLine()!!
    val image: BufferedImage
    try {
        image = ImageIO.read(File(fileName))
    } catch (e: IOException) {
        println("Can't read input file!")
        return
    }
    readMessageInImage(image = image, password = password)
}

/* Look for the bits in the blue number of each pixel */
fun readMessageInImage(image: BufferedImage, password: String) {
    val passwordAsArrayOfBits = messageToBits(message = password)
    val bitsFromImageAsArray: String = bitsFromImage(image = image)
    // Make a substring until the first occurrence of ESCAPE_PATRON
    val messageAsBits = bitsFromImageAsArray.substring(0, bitsFromImageAsArray.indexOf(ESCAPE_PATRON))
    val message = bitsToMessage(messageAsBits = messageAsBits, passwordAsArrayOfBits = passwordAsArrayOfBits)
    println("Message:")
    println(message)
}

/* It will traduce the array of bits to a message */
fun bitsToMessage(messageAsBits: String, passwordAsArrayOfBits: List<Int>): String {
    val message = StringBuilder()
    val messageEncrypted = messageAsBits.map { it.toString().toInt() } // Creates a int collection
    val auxMessageList = mutableListOf<Int>()
    var j = 0
    for (i in messageEncrypted.indices) {
        if (j == passwordAsArrayOfBits.size) j = 0
        auxMessageList.add(messageEncrypted[i].xor(passwordAsArrayOfBits[j]))
        j++
    }
    val auxMessage = auxMessageList.joinToString("")
    // Translate bytes to message
    for (i in auxMessage.indices step 8) {
        val byte = auxMessage.substring(i, i + 8) // Creates a group of 8 bits
        message.append(byte.toInt(2).toChar()) // It cast a binary number to its char form
    }
    return message.toString()
}

/* It will show all the bits from blues int in the image */
fun bitsFromImage(image: BufferedImage): String {
    val bitsFromImageAsArray = mutableListOf<Int>()
    for (y in 0 until image.height) {
        for (x in 0 until image.width) {
            val color = Color(image.getRGB(x, y))
            bitsFromImageAsArray.add(color.blue.and(1)) // It will give the last digit of the byte
        }
    }
    return bitsFromImageAsArray.joinToString("")
}

/* Ask for file name and opens it */
fun hideMessage() {
    println("Input image file:")
    val fileName = readLine()!!
    println("Output image file:")
    val newFileName = readLine()!!

    val image: BufferedImage
    try {
        image = ImageIO.read(File(fileName))
    } catch (e: IOException) {
        println("Can't read input file!")
        return
    }
    println("Input Image: $fileName")
    println("Output Image: $newFileName")

    println("Message to hide:")
    val message = readLine()!!
    println("Password:")
    val password = readLine()!!

    // Creates a password that repeats itself the necessary times to match with message length
    val passwordUpdated = password.repeat(message.length / password.length) + password.substring(0, message.length.rem(password.length))

    val hidden = hideMessageInImage(image = image, message = message, password = passwordUpdated)

    if (hidden) {
        ImageIO.write(image, "png", File(newFileName))
        println("Message saved in $newFileName image.")
    } else {
        println("The input image is not large enough to hold this message.")
    }
}

/* It goes into the pixel image and calls the setLeastSignificantBitToOne
for each color in each pixel */
fun hideMessageInImage(image: BufferedImage, message: String, password: String): Boolean{
    val messageAsArrayOfBytes: MutableList<Int> = messageToBits(message = message).toMutableList()
    val escapePatron = listOf(0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 0, 1, 1)
    messageAsArrayOfBytes.addAll(escapePatron) // adds the escape patron to message array
    val passwordAsArrayOfBytes: List<Int> = messageToBits(message = password)


    if (messageAsArrayOfBytes.size < image.width * image.height) {
        var count = 0
        loop@for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                if (count < messageAsArrayOfBytes.size) {
                    val newColor = if (count < passwordAsArrayOfBytes.size) { // while count is in password length
                        Color(
                            color.red,
                            color.green,
                            setLastBitWithXor(pixel = color.blue,
                                bitMessage = messageAsArrayOfBytes[count],
                                bitPassword = passwordAsArrayOfBytes[count])
                        ).rgb
                    } else { // Out of password length, for the escape patron
                        Color(
                            color.red,
                            color.green,
                            setLastBit(pixel = color.blue, bit = messageAsArrayOfBytes[count])
                        ).rgb // Get the rgb value
                    }


                    image.setRGB(x, y, newColor)
                    count++
                } else {
                    // When all elements from message are in the image exits from image "scan"
                    break@loop
                }
            }
        }
        return true
    } else {
        return false
    }
}

fun setLastBitWithXor(pixel: Int, bitMessage: Int, bitPassword: Int): Int {
    val bitAfterXor = bitMessage.xor(bitPassword)
    return setLastBit(pixel = pixel, bit = bitAfterXor)
}

/* Change the last bit of the color following the bit input */
fun setLastBit(pixel: Int, bit: Int) = pixel.and(254).or(bit)

/* It will create a list of bits from a string and adds the escape patron */
fun messageToBits(message: String): List<Int> {
    val messageToInt = message.encodeToByteArray() // It will retrieve each char as int
    val messageToBits = mutableListOf<String>()
    // It will change each int in messageToInt to its bits form and puts zero before to reach 8 characters
    messageToInt.forEach { messageToBits.add(Integer.toBinaryString(it.toInt()).padStart(8, '0')) }
    // It will join all the bits in one string and add the escape patron
    val auxMessageToBits = messageToBits.joinToString("")

    // returns an integers list with all the bits from the message and escapePatron
    return auxMessageToBits.map {it.toString().toInt()}.toList()
}


/* Applies a or function to the number changing the last bit only if it is 0
*  Ex. 11010010 or 00000001 = 11010011
* */
//fun setLeastSignificantBitToOne(intColor: Int) = intColor or 1

