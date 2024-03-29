package restaurant.order

/**
 * Statistics module for creating statistics.
 *
 * @property allOrders the list of all orders existed in the OrderSystem
 * @constructor Create ready to use StatsModule
 */
internal class StatsModule(private val allOrders: MutableList<Order>) {
    private val averageNumberOfDishes: Double
        get() {
            val list = mutableListOf<Int>()
            for (order in allOrders) {
                list.add(order.getNumberOfDishes)
            }
            return list.sum().toDouble() / list.size
        }

    private val averageMark: Double
        get() {
            val list = mutableListOf<Int>()
            for (order in allOrders) {
                val value = order.getMarkIfItExists
                if (value != null) {
                    list.add(value)
                }
            }
            return list.sum().toDouble() / list.size
        }

    private val getSumOfBills : Int
        get() {
            var sum = 0
            for (order in allOrders) {
                sum += order.sumOfBill
            }
            return sum
        }

    /**
     * @return string of statistics module
     */
    fun getStatistics(): String {
        return try {
            """
                Statistics:
                1. The average mark for orders: $averageMark
                2. The average number of dishes in 1 order: $averageNumberOfDishes    
                3. Total revenue of restaurant: $getSumOfBills
            """.trimIndent()
        } catch (ex: Exception) {
            ex.message.toString()
        }
    }
}