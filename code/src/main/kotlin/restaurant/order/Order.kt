package restaurant.order

import restaurant.Logger
import restaurant.dish.Dish
import restaurant.dish.Menu

class Order(
    private var list : MutableMap<Dish, Int>,
    internal var level: ImportanceLevel,
    private var menu: Menu,
    private var userId : Int,
    var orderId : Int
) {
    private var status = OrderStatus.New
    private lateinit var associatedThread : Thread
    private lateinit var review : Review

    init {
        acceptOrder()
        startOrder()
    }

    fun addDish(newDish: Dish, amount: Int = 1) {
        if (status == OrderStatus.Accepted) {
            if (list[newDish] != null) {
                Logger.writeToLogResult("Trying to add dish to order $orderId: ${newDish.name}", Logger.Status.OK)
                list[newDish] = list[newDish]!! + amount
            }
        } else {
            Logger.writeToLogResult("Trying to add dish to order $orderId: ${newDish.name}. It's not accepted!", Logger.Status.ERROR)
        }
    }

    fun acceptOrder() : Boolean {
        val result = menu.acceptOrder(list)
        return if (!result) {
            status = OrderStatus.Canceled
            false
        } else {
            status = OrderStatus.Accepted
            true
        }
    }

    private fun getMaxTime() : Long {
        return list.keys.maxBy { it.timeProduction }.timeProduction.seconds
    }

    private fun startOrder() {
        if (status == OrderStatus.Accepted) {
            associatedThread = Thread {
                try {
                    Logger.writeToLog("Start cooking order $orderId...")
                    status = OrderStatus.Cooking
                    Thread.sleep(getMaxTime() * 1000)
                    Logger.writeToLogResult("Order $orderId is ready!", Logger.Status.OK)
                    status = OrderStatus.Ready
                } catch (_ : Exception) {
                    Logger.writeToLogResult("Cancel the order $orderId.", Logger.Status.OK)
                    status = OrderStatus.Canceled
                }
            }
            associatedThread.start()
        } else {
            Logger.writeToLogResult("Order $orderId is not accepted!", Logger.Status.ERROR)
        }
    }

    fun cancelOrder() {
        try {
            if (status == OrderStatus.Cooking) {
                associatedThread.interrupt()
            } else {
                Logger.writeToLogResult("Order $orderId is not cooking!", Logger.Status.ERROR)
            }
        } catch (_ : Exception) {

        }
    }

    fun payOrder() {
        if (status == OrderStatus.Ready) {
            var sum = 0
            for (dish in list.keys) {
                sum += dish.price
            }

            Logger.writeToLogResult("Order $orderId for sum = $sum has paid.", Logger.Status.OK)
            status = OrderStatus.Payed
        } else {
            Logger.writeToLogResult("You cannot pay the order $orderId now, now order is $status", Logger.Status.ERROR)
        }
    }

    fun getStatus() : OrderStatus {
        return status
    }

    fun setReview(stars : Int, comment : String) {
        if (status == OrderStatus.Payed || status == OrderStatus.Ready) {
            Logger.writeToLog("Visitor left the review with $stars stars for the order $orderId")
            review = Review(stars, comment)
        }
    }
}