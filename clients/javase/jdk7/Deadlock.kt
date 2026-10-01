package jdk7

class Deadlock {
    companion object {
        @JvmStatic
        fun main(vararg asArgs: String) {
            val alphonse = Friend("Alphonse")
            val gaston = Friend("Gaston")
            Thread { alphonse.bow(gaston) }.start()
            Thread { gaston.bow(alphonse) }.start()
        }
    }

    class Friend(private val name: String) {
        fun getName(): String = name

        @Synchronized
        fun bow(bower: Friend) {
            System.out.format(
                "%s: %s  has bowed to me!%n",
                this.name, bower.getName()
            )
            bower.bowBack(this)
        }

        @Synchronized
        fun bowBack(bower: Friend) {
            System.out.format(
                "%s: %s has bowed back to me!%n",
                this.name, bower.getName()
            )
        }
    }
}
