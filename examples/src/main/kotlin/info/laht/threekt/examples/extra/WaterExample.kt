package info.laht.threekt.examples.extra

import info.laht.threekt.Canvas
import info.laht.threekt.CanvasOptions
import info.laht.threekt.TextureWrapping
import info.laht.threekt.cameras.PerspectiveCamera
import info.laht.threekt.controls.OrbitControls
import info.laht.threekt.core.Clock
import info.laht.threekt.examples.textures.TextureExample
import info.laht.threekt.extras.objects.Water
import info.laht.threekt.geometries.BoxBufferGeometry
import info.laht.threekt.geometries.PlaneGeometry
import info.laht.threekt.lights.DirectionalLight
import info.laht.threekt.loaders.TextureLoader
import info.laht.threekt.materials.MeshPhongMaterial
import info.laht.threekt.math.Color
import info.laht.threekt.objects.Mesh
import info.laht.threekt.renderers.GLRenderer
import info.laht.threekt.scenes.Scene
import java.io.File
import kotlin.math.PI

object WaterExample {

    @JvmStatic
    fun main(args: Array<String>) {

        Canvas(CanvasOptions().apply {
            antialiasing = 4
        }).use { canvas ->

            canvas.enableDebugCallback()

            val scene = Scene().apply {
                setBackground(Color(Color.aliceblue).multiplyScalar(0.8f))
            }
            val renderer = GLRenderer(canvas)

            val camera = PerspectiveCamera(45, canvas.aspect, 1, 2000).apply {
                position.set(0f, 7.5f, 16.0f)
            }
            val controls = OrbitControls(camera, canvas)

            val planeGeometry = PlaneGeometry(10f, 10f)

            val texture = TextureLoader.load(File(TextureExample::class.java.classLoader.getResource("textures/waternormals.jpg").file)).also {
                it.wrapS = TextureWrapping.Repeat
                it.wrapT = TextureWrapping.Repeat
            }

            val light = DirectionalLight(0xffffff, 0.8).also {
                scene.add(it)
            }

            val water = Water(
                planeGeometry, Water.Options(
                    alpha = 1f,
                    waterNormals = texture,
                    waterColor = Color(0x001e0f),
                    sunColor = Color(0xffffff),
                    textureWidth = 512,
                    textureHeight = 512,
                    sunDirection = light.position.clone().normalize(),
                    distortionScale = 3.7f
                )
            ).also {
                it.rotateX(-PI.toFloat() / 2)
                scene.add(it)
            }

            val box = Mesh(BoxBufferGeometry(1f), MeshPhongMaterial().apply {
                color.set(0x00ff00)
//                emissive.set(0x333333)
//                flatShading = true
            }).also {
                it.position.y = 3f
                scene.add(it)
            }



            val clock = Clock()
            while (!canvas.shouldClose()) {


                val wTime = water.uniforms["time"]!!.value as Float
                water.uniforms["time"]!!.value = wTime + (0.001f*clock.getDelta())

                renderer.render(scene, camera)

            }

        }

    }

}