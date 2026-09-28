package jdk7

/**
 * User: muthu
 * Date: 3/27/12
 * Time: 5:33 PM
 */
class StringSwitchDemo {
    companion object {
        @JvmStatic
        fun getMonthNumber(month: String?): Int {
            var monthNumber = 0
            if (month == null) {
                return monthNumber
            }
            monthNumber = when (month.lowercase()) {
                "january" -> 1
                "february" -> 2
                "march" -> 3
                "april" -> 4
                "may" -> 5
                "june" -> 6
                "july" -> 7
                "august" -> 8
                "september" -> 9
                "october" -> 10
                "november" -> 11
                "december" -> 12
                else -> 0
            }
            return monthNumber
        }

        @JvmStatic
        fun main(args: Array<String>) {
            val month = "August"
            val returnedMonthNumber = getMonthNumber(month)
            if (returnedMonthNumber == 0) {
                println("Invalid month")
            } else {
                println(returnedMonthNumber)
            }
        }
    }
}

class SwitchDemo {
    companion object {
        @JvmStatic
        fun main(args: Array<String>) {
            val month = 8
            val monthString = when (month) {
                1 -> "January"
                2 -> "February"
                3 -> "March"
                4 -> "April"
                5 -> "May"
                6 -> "June"
                7 -> "July"
                8 -> "August"
                9 -> "September"
                10 -> "October"
                11 -> "November"
                12 -> "December"
                else -> "Invalid month"
            }
            println(monthString)
        }
    }

    fun switchDemo() {
        val futureMonths = ArrayList<String>()
        val month = 8
        when (month) {
            1 -> {
                futureMonths.add("January")
                futureMonths.add("February")
                futureMonths.add("March")
                futureMonths.add("April")
                futureMonths.add("May")
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            2 -> {
                futureMonths.add("February")
                futureMonths.add("March")
                futureMonths.add("April")
                futureMonths.add("May")
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            3 -> {
                futureMonths.add("March")
                futureMonths.add("April")
                futureMonths.add("May")
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            4 -> {
                futureMonths.add("April")
                futureMonths.add("May")
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            5 -> {
                futureMonths.add("May")
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            6 -> {
                futureMonths.add("June")
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            7 -> {
                futureMonths.add("July")
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            8 -> {
                futureMonths.add("August")
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            9 -> {
                futureMonths.add("September")
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            10 -> {
                futureMonths.add("October")
                futureMonths.add("November")
                futureMonths.add("December")
            }
            11 -> {
                futureMonths.add("November")
                futureMonths.add("December")
            }
            12 -> {
                futureMonths.add("December")
            }
            else -> {}
        }
        if (futureMonths.isEmpty()) {
            println("Invalid month number")
        } else {
            for (monthName in futureMonths) {
                println(monthName)
            }
        }
    }
}
