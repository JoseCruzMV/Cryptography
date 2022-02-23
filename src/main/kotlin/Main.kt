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

/* Show options to user and gets election */
fun printMenu() {
    while (true) {
        println("Task (hide, show, exit):")
        when(val input = readLine()!!) {
            MenuOptions.HIDE.option -> hideMessage()
            MenuOptions.SHOW.option -> println("Obtaining message from image.")
            MenuOptions.EXIT.option -> break
            else -> println("Wrong task: $input")
        }
    }

    println("Bye!")
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

    hideMessageInImage(image = image)
    ImageIO.write(image, "png", File(newFileName))
    println("Image image.png is saved.")
}

/* It goes into the pixel image and calls the setLeastSignificantBitToOne
for each color in each pixel */
fun hideMessageInImage(image: BufferedImage){
    for (i in 0 until image.width) {
        for (j in 0 until image.height) {
            val color = Color(image.getRGB(i, j))
            val newColor = Color(
                setLeastSignificantBitToOne(intColor = color.red),
                setLeastSignificantBitToOne(intColor = color.green),
                setLeastSignificantBitToOne(intColor = color.blue)
            ).rgb // Get the rgb value
            image.setRGB(i, j, newColor)
        }
    }
}

/* Applies a or function to the number changing the last bit only if it is 0
*  Ex. 11010010 or 00000001 = 11010011
* */
fun setLeastSignificantBitToOne(intColor: Int) = intColor or 1

