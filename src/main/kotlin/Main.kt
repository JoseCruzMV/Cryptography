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

fun showMessage() {
    println("Input image file:")
    val fileName = readLine()!!

    val image: BufferedImage
    try {
        image = ImageIO.read(File(fileName))
    } catch (e: IOException) {
        println("Can't read input file!")
        return
    }
    readMessageInImage(image = image)
}

/* Look for the bits in the blue number of each pixel */
fun readMessageInImage(image: BufferedImage) {
    val bitsFromImageAsArray: String = bitsFromImage(image = image)
    // Make a substring until the first occurrence of ESCAPE_PATRON
    val messageAsBits = bitsFromImageAsArray.substring(0, bitsFromImageAsArray.indexOf(ESCAPE_PATRON))
    val message = bitsToMessage(messageAsBits = messageAsBits)
    println("Message:")
    println(message)
}

/* It will traduce the array of bits to a message */
fun bitsToMessage(messageAsBits: String): String {
    val message = StringBuilder()
    for (i in messageAsBits.indices step 8) {
        val byte = messageAsBits.substring(i, i + 8) // Creates a group of 8 bits
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

    val hidden = hideMessageInImage(image = image, message = message)

    if (hidden) {
        ImageIO.write(image, "png", File(newFileName))
        println("Message saved in $newFileName image.")
    } else {
        println("The input image is not large enough to hold this message.")
    }
}

/* It goes into the pixel image and calls the setLeastSignificantBitToOne
for each color in each pixel */
fun hideMessageInImage(image: BufferedImage, message: String): Boolean{
    val messageAsArrayOfBytes: List<Int> = messageToBits(message = message)

    if (messageAsArrayOfBytes.size < image.width * image.height) {
        var count = 0
        loop@for (y in 0 until image.height) {
            for (x in 0 until image.width) {
                val color = Color(image.getRGB(x, y))
                if (count < messageAsArrayOfBytes.size) {

                    val newColor = Color(
                        color.red,
                        color.green,
                        setLastBit(pixel = color.blue, bit = messageAsArrayOfBytes[count])
                    ).rgb // Get the rgb value

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

/* Change the last bit of the color following the bit input */
fun setLastBit(pixel: Int, bit: Int) = pixel.and(254).or(bit)

/* It will create a list of bits from a string and adds the escape patron */
fun messageToBits(message: String): List<Int> {
    val messageToInt = message.encodeToByteArray() // It will retrieve each char as int
    val messageToBits = mutableListOf<String>()
    // It will change each int in messageToInt to its bits form and puts zero before to reach 8 characters
    messageToInt.forEach { messageToBits.add(Integer.toBinaryString(it.toInt()).padStart(8, '0')) }
    // It will join all the bits in one string and add the escape patron
    val auxMessageToBits = messageToBits.joinToString("") + ESCAPE_PATRON

    // returns an integers list with all the bits from the message and escapePatron
    return auxMessageToBits.map {it.toString().toInt()}.toList()
}


/* Applies a or function to the number changing the last bit only if it is 0
*  Ex. 11010010 or 00000001 = 11010011
* */
//fun setLeastSignificantBitToOne(intColor: Int) = intColor or 1

