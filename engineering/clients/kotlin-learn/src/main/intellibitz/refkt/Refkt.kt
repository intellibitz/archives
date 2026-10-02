package refkt

import kotlin.reflect.full.memberProperties

fun main() {
    pRef()
    kfun.call(9)
    kprop.setter.call(1)
    println(kprop.getter.call())
}

class Refkt(val name: String, val age: Int)

private fun pRef() {
    val refkt = Refkt("Apple", 99)
    val kClass = refkt.javaClass.kotlin
    val rname = Refkt::name
    println("${rname.get(refkt)} - ${refkt.age}")
    println(kClass.simpleName)
    kClass.memberProperties.forEach { println(it.name) }
}

var counter = 0
val kprop = ::counter

fun foo(x: Int) = println(x)
val kfun = ::foo
//val kfun: KFunction1<Int, Unit> = ::foo

/*
Note: KFunction1, KFunction2.. et al...
These function types are synthetic compiler-generated types, and you
won’t find their declarations in the kotlin.reflect package. That means
you can use an interface for a function with any number of parameters.
The synthetic types approach reduces the size of kotlin-runtime.jar
and avoids artificial restrictions on the possible number of function type
parameters.
 */

/*
The syntax for applying annotations in Kotlin is almost the same as in Java;
Kotlin allows to apply annotations to a broader range of targets than Java, including files
and expressions;
Annotation argument can be a primitive value, string, enum, class reference, instance of
other annotation class or array thereof;
Specifying the use-site target for annotation like in @get:Rule allows to choose howthe
annotation is applied if a single Kotlin declaration produces multiple bytecode elements;
You declare an annotation class as a class with a primary constructor where all
parameters are marked as val properties and without a body;
Meta-annotations can be used to specify the target, retention mode and other attributes of
annotations;
The reflection API allows to enumerate and access the methods and properties of an
object dynamically at runtime. It has interfaces representing different kinds of
declarations, such as classes (KClass), functions (KFunction) and so on;
To obtain a KClass instance, you can use ClassName::class if the class is statically
known and obj.javaClass.kotlin to get the class from an object instance;
KFunction and KProperty interfaces both extend KCallable which provide the generic
call method;
KFunction0, KFunction1 etc. represent functions with different number of parameters
and can be called using the invoke method;
KProperty0 and KProperty1 represent properties with different number of receivers and
support the get method for retrieving the value;
The KCallable.callBy() method can be used to invoke methods with default
parameter values.
 */