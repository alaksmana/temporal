package greeting.workflow

import greeting.activity.HelloActivity
import io.temporal.activity.ActivityOptions
import io.temporal.workflow.Workflow
import java.time.Duration

class GreetingWorkflowImpl: GreetingWorkflow {

    private var activityOptions = ActivityOptions
        .newBuilder()
        .setScheduleToCloseTimeout(Duration.ofSeconds(2))
        .build()

    private var format = Workflow.newActivityStub(HelloActivity::class.java, activityOptions)

    override fun greet(name: String): String {
        return format.hello(name)
    }
}