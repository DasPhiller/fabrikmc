package net.axay.fabrik.nbt

import io.kotest.core.spec.style.StringSpec
import io.kotest.matchers.shouldBe
import io.kotest.matchers.types.shouldBeInstanceOf
import io.kotest.property.Arb
import io.kotest.property.Exhaustive
import io.kotest.property.arbitrary.*
import io.kotest.property.checkAll
import io.kotest.property.exhaustive.enum
import kotlinx.serialization.Serializable
import net.minecraft.nbt.*

class NbtEncodingTest : StringSpec({
    "class should encode to compound" {
        val value = TestClass(
            10,
            -1248,
            "Taito",
            listOf("abc", "äèðßèäßð", "1 2"),
            setOf(5, 34, 9, 3443),
            InnerTestClass(true)
        )
        val element = Nbt.encodeToNbtElement(value)
        element.shouldBeInstanceOf<NbtCompound>()
        element.size shouldBe 6
        element.getInt("x") shouldBe value.x
        element.getLong("y") shouldBe value.y
        element.getString("name") shouldBe value.name
        element.getList("stringList", NbtElement.STRING_TYPE.toInt()).map { it.asString() } shouldBe value.stringList
        element.getLongArray("longSet") shouldBe value.longSet.toLongArray()
        with(element.getCompound("inner")) {
            size shouldBe 1
            getBoolean("test") shouldBe value.inner.test
        }
    }

    "byte collections should encode to byte array" {
        checkAll(Arb.byteArrays(Arb.int(0..0x1000), Arb.byte())) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtByteArray>()
            element.byteArray shouldBe it
        }
        checkAll(Arb.list(Arb.byte(), 0..0x1000)) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtByteArray>()
            element.byteArray shouldBe it
        }
        checkAll(Arb.set(Arb.byte(), 0..Byte.MAX_VALUE)) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtByteArray>()
            element.byteArray shouldBe it.toByteArray()
        }
    }

    "int collections should encode to int array" {
        checkAll(Arb.list(Arb.int(), 0..0x1000)) {
            val array = it.toIntArray()
            val elements = listOf(Nbt.encodeToNbtElement(it), Nbt.encodeToNbtElement(array))
            for (element in elements) {
                element.shouldBeInstanceOf<NbtIntArray>()
                element.intArray shouldBe array
            }
        }
        checkAll(Arb.set(Arb.int(), 0..0x1000)) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtIntArray>()
            element.intArray shouldBe it.toIntArray()
        }
    }

    "long collections should encode to long array" {
        checkAll(Arb.list(Arb.long(), 0..0x1000)) {
            val array = it.toLongArray()
            val elements = listOf(Nbt.encodeToNbtElement(it), Nbt.encodeToNbtElement(array))
            for (element in elements) {
                element.shouldBeInstanceOf<NbtLongArray>()
                element.longArray shouldBe array
            }
        }
        checkAll(Arb.set(Arb.long(), 0..0x1000)) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtLongArray>()
            element.longArray shouldBe it.toLongArray()
        }
    }

    "enums should encode to strings" {
        checkAll(Exhaustive.enum<TestEnum>()) {
            val element = Nbt.encodeToNbtElement(it)
            element.shouldBeInstanceOf<NbtString>()
            element.asString() shouldBe it.name
        }
    }
})

@Serializable
class TestClass(
    val x: Int,
    val y: Long,
    val name: String,
    val stringList: List<String>,
    val longSet: Set<Long>,
    val inner: InnerTestClass,
)

@Serializable
class InnerTestClass(val test: Boolean)

enum class TestEnum {
    VARIANT_1, VARIANT_2, LastVariant,
}
