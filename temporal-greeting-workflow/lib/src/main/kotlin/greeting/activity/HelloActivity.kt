package greeting.activity

import io.temporal.activity.ActivityInterface
import io.temporal.activity.ActivityMethod

@ActivityInterface
interface HelloActivity {

    @ActivityMethod
    fun hello(name: String): String
}