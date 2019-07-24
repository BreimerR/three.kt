package info.laht.threekt

import info.laht.threekt.input.KeyAction
import info.laht.threekt.input.KeyEvent
import info.laht.threekt.input.MouseEvent
import info.laht.threekt.input.MouseWheelEvent
import org.lwjgl.glfw.GLFW.*
import org.lwjgl.opengl.GL
import java.io.Closeable
import org.lwjgl.glfw.GLFWErrorCallback
import org.lwjgl.glfw.GLFW.glfwSetWindowShouldClose
import org.lwjgl.glfw.GLFW.GLFW_RELEASE
import org.lwjgl.glfw.GLFW.GLFW_KEY_ESCAPE
import org.lwjgl.glfw.GLFW.glfwSetKeyCallback
import org.lwjgl.glfw.GLFW.glfwSetMouseButtonCallback
import org.lwjgl.glfw.GLFW.glfwSetCursorPosCallback
import org.lwjgl.glfw.GLFW.glfwSetScrollCallback

class CanvasOptions {
    var width: Int = 800
    var height: Int = 600

    var antialiasing: Int = 0

    var title: String = "Three.kt"
}

class Canvas @JvmOverloads constructor(
    options: CanvasOptions = CanvasOptions()
) : Closeable {

    val width: Int = options.width
    val height: Int = options.height

    private val pointer: Long

    val aspect: Float
        get() = width.toFloat() / height

    var onKeyPressed: ((KeyEvent) -> Unit)? = null
    var onMouseWheel: ((MouseWheelEvent) -> Unit)? = null
    var onMouseDown: ((MouseEvent) -> Unit)? = null
    var onMouseUp: ((MouseEvent) -> Unit)? = null
    var onMouseMove: ((MouseEvent) -> Unit)? = null

    private val mouseEvent = MouseEvent()

    init {
        val errorCallback = GLFWErrorCallback.createPrint(System.err)
        glfwSetErrorCallback(errorCallback)
        if (!glfwInit()) {
            throw IllegalStateException("Unable to initialize GLFW")
        }
        pointer = createWindow(options.title, options.antialiasing)
    }

    fun shouldClose(): Boolean {
        return glfwWindowShouldClose(pointer)
    }

    override fun close() {
        glfwTerminate()
    }

    private fun createWindow(title: String, antialias: Int): Long {

        if (antialias > 0) {
            glfwWindowHint(GLFW_SAMPLES, antialias);
        }

        // In order to see anything, we createShader a new pointer using GLFW's glfwCreateWindow().
        glfwWindowHint(GLFW_RESIZABLE, GLFW_FALSE)
        val window = glfwCreateWindow(width, height, title, 0, 0)

        // Setup a key callback. It will be called every time a key is pressed, repeated or released.
        glfwSetKeyCallback(window) { window, key, _, action, _ ->
            if (key == GLFW_KEY_ESCAPE && action == GLFW_RELEASE) {
                glfwSetWindowShouldClose(window, true) // We will detect this in the rendering loop
            } else {
                onKeyPressed?.invoke(KeyEvent(key, action.toKeyAction()))
            }
        }

        glfwSetMouseButtonCallback(window) { window, button, action, mods ->
            mouseEvent.button = button
            when (action) {
                0 -> onMouseUp?.invoke(mouseEvent)
                1 -> onMouseDown?.invoke(mouseEvent)
            }
        }

        glfwSetCursorPosCallback(window) { window, xpos, ypos ->
            mouseEvent.updateCoordinates(xpos.toInt(), ypos.toInt())
            onMouseMove?.invoke(mouseEvent)
        }

        glfwSetScrollCallback(window){ window, xoffset, yoffset ->
            onMouseWheel?.invoke(MouseWheelEvent(xoffset.toFloat(), yoffset.toFloat()))
        }

        // Tell GLFW to make the OpenGL context current so that we can make OpenGL calls.
        glfwMakeContextCurrent(window)

        // Enable v-sync
        glfwSwapInterval(1);

        // Tell LWJGL 3 that an OpenGL context is current in this thread. This will result in LWJGL 3 querying function
        // pointers for various OpenGL functions.
        GL.createCapabilities()

        // Return the handle to the created pointer.
        return window
    }

    internal fun tick() {
        glfwSwapBuffers(pointer)
        glfwPollEvents()
    }

}

private fun Int.toKeyAction(): KeyAction {
    return when (this) {
        GLFW_RELEASE -> KeyAction.RELEASE
        GLFW_PRESS -> KeyAction.PRESS
        GLFW_REPEAT -> KeyAction.REPEAT
        else -> throw IllegalArgumentException()
    }
}