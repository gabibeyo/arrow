package arrow.optics

import arrow.common.Package
import arrow.common.utils.ClassOrPackageDataWrapper
import arrow.common.utils.fullName
import me.eugeniomarletti.kotlin.metadata.escapedClassName
import me.eugeniomarletti.kotlin.metadata.plusIfNotBlank
import javax.lang.model.element.Element
import javax.lang.model.element.ExecutableElement
import javax.lang.model.element.TypeElement

typealias AnnotatedSumType = AnnotatedType.AnnotatedClass.Sum
typealias AnnotatedProductType = AnnotatedType.AnnotatedClass.Product

sealed class AnnotatedType {

  abstract val element: Element
  abstract val `package`: String

  sealed class AnnotatedClass : AnnotatedType() {
    abstract val classData: ClassOrPackageDataWrapper.Class
    abstract val foci: List<Focus>

    inline val sourceClassName inline get() = classData.fullName.escapedClassName
    inline val sourceName inline get() = element.simpleName.toString().decapitalize()
    inline val packageName inline get() = classData.`package`.escapedClassName

    override val `package`: String get() = classData.`package`

    data class Sum(override val element: TypeElement, override val classData: ClassOrPackageDataWrapper.Class, override val foci: List<Focus>) : AnnotatedClass()

    data class Product(override val element: TypeElement, override val classData: ClassOrPackageDataWrapper.Class, override val foci: List<Focus>) : AnnotatedClass()
  }


}

typealias NonNullFocus = Focus.NonNull
typealias OptionFocus = Focus.Option
typealias NullableFocus = Focus.Nullable

sealed class Focus {

  companion object {
    operator fun invoke(fullName: String, paramName: String): Focus = when {
      fullName.endsWith("?") -> Nullable(fullName, paramName)
      fullName.startsWith("`arrow`.`core`.`Option`") -> Option(fullName, paramName)
      else -> NonNull(fullName, paramName)
    }
  }

  abstract val className: String
  abstract val paramName: String

  data class Nullable(override val className: String, override val paramName: String) : Focus() {
    val nonNullClassName = className.dropLast(1)
  }

  data class Option(override val className: String, override val paramName: String) : Focus() {
    val nestedClassName = Regex("`arrow`.`core`.`Option`<(.*)>$").matchEntire(className)!!.groupValues[1]
  }

  data class NonNull(override val className: String, override val paramName: String) : Focus()

}

sealed class Optic {
  companion object {
    val values = listOf(Lens, Iso, Optional, Prism, Getter, Setter, Traversal, Fold)
  }
}

object Lens : Optic() {
  override fun toString() = "arrow.optics.Lens"
}

object Iso : Optic() {
  override fun toString() = "arrow.optics.Iso"
}

object Optional : Optic() {
  override fun toString() = "arrow.optics.Optional"
}

object Prism : Optic() {
  override fun toString() = "arrow.optics.Prism"
}

object Getter : Optic() {
  override fun toString() = "arrow.optics.Getter"
}

object Setter : Optic() {
  override fun toString() = "arrow.optics.Setter"
}

object Traversal : Optic() {
  override fun toString() = "arrow.optics.Traversal"
}

object Fold : Optic() {
  override fun toString() = "arrow.optics.Fold"
}

sealed class POptic {
  fun monomorphic(): Optic = when (this) {
    PLens -> Lens
    PIso -> Iso
    POptional -> Optional
    PPrism -> Prism
    PSetter -> Setter
    PTraversal -> Traversal
  }

  companion object {
    val values = listOf(PLens, PIso, POptional, PPrism, PSetter, PTraversal)
  }
}

object PLens : POptic() {
  override fun toString() = "arrow.optics.PLens"
}

object PIso : POptic() {
  override fun toString() = "arrow.optics.PIso"
}

object POptional : POptic() {
  override fun toString() = "arrow.optics.POptional"
}

object PPrism : POptic() {
  override fun toString() = "arrow.optics.PPrism"
}

object PSetter : POptic() {
  override fun toString() = "arrow.optics.PSetter"
}

object PTraversal : POptic() {
  override fun toString() = "arrow.optics.PTraversal"
}

const val Tuple = "arrow.core.Tuple"

data class Snippet(val imports: Set<String> = emptySet(), val content: String) {
  companion object {
    val EMPTY = Snippet(content = "")
  }

  fun asFileText(packageName: Package): String = """
            |package $packageName
            |${imports.joinToString(prefix = "\n", separator = "\n", postfix = "\n")}
            |$content
            """.trimMargin()

  operator fun plus(snippet: Snippet): Snippet = Snippet(
    this.imports + snippet.imports,
    content.plusIfNotBlank(postfix = "\n") + snippet.content
  )

}
