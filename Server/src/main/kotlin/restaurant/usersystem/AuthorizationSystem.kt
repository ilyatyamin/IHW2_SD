package restaurant.usersystem

import kotlinx.serialization.encodeToString
import restaurant.Logger
import restaurant.Serializer
import restaurant.order.OrderSystem

class AuthorizationSystem(private val system : OrderSystem) {
    private var users = mutableSetOf<User>()
    private var userIncrement = 0

    init {
        tryToDeserialize()
        for (user in users) {
            user.setOS(system)
        }
    }

    private val getUserId : Int
        get() {
            userIncrement += 1
            return userIncrement
        }

    /**
     * Register user (admin / visitor, in dependent of role parameter) with login and password
     *
     * @param login
     * @param encryptedPassw it is already in encrypted style
     * @param role: enum of UserRole (Visitor, Admin)
     * @return boolean variable: is adding was succesful.
     * @throws SecurityException if this user already exists in the system
     */
    fun addUserToSystem(login: String, encryptedPassw: String, role: UserRole): Boolean {
        // If this user already exists...
        val resultOfSearch = users.find { it.compareData(login, encryptedPassw) }
        if (resultOfSearch != null) {
            Logger.writeToLog("Attempt for register a new user with login $login. ERROR")
            throw SecurityException("This user already exists")
        }

        if (role == UserRole.Visitor) {
            users.add(Visitor(getUserId, login, encryptedPassw).setOS(system))
        } else if (role == UserRole.Admin) {
            users.add(Admin(getUserId, login, encryptedPassw).setOS(system))
        }
        Logger.writeToLog("Attempt for register a new user with login $login. OK")
        serialize()
        return true
    }

    /**
     * Authentication method
     *
     * @param login login of user
     * @param encryptedPassw password in encrypted style
     * @return User object if auth is successful or null in other case
     */
    fun tryAuth(login: String, encryptedPassw: String): User? {
        val result = users.find { it.compareData(login, encryptedPassw) }
        if (result != null) {
            result.isLoggedNow = true
            Logger.writeToLog("Attempt for auth with login $login. Result: OK")
        } else {
            Logger.writeToLog("Attempt for auth with login $login. Result: ERROR")
        }
        return result
    }

    /**
     * Exit from system
     */
    fun exitFromSystem(user: User?) {
        if (user == null) {
            Logger.writeToLog("Attempt for exit with login NULL. Result: ERROR")
            return
        }
        Logger.writeToLog("Attempt for exit for user. Result: OK")
        user.isLoggedNow = false
        serialize()
    }

    private fun serialize() {
        Serializer.write(Serializer.json.encodeToString(users), Serializer.usersFile)
        Serializer.write(Serializer.json.encodeToString(userIncrement), Serializer.userIdGetterFile)
    }

    private fun tryToDeserialize() {
        try {
            users = Serializer.json.decodeFromString(Serializer.read(Serializer.usersFile)!!)
            Logger.writeToLog("Users in AuthSystem has deserialized successfully!")
        } catch(ex : Exception) {
            Logger.writeToLog("Users deserializator: ${ex.message.toString()}")
        }

        try {
            userIncrement = Serializer.json.decodeFromString(Serializer.read(Serializer.userIdGetterFile)!!)
            Logger.writeToLog("UserIdGetter in AuthSystem has deserialized successfully!")
        } catch(ex : Exception) {
            Logger.writeToLog("UserIdGetter deserializator: ${ex.message.toString()}")
        }
    }


 }