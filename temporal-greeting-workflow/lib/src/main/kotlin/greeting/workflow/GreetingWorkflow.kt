package greeting.workflow

import io.temporal.workflow.WorkflowInterface
import io.temporal.workflow.WorkflowMethod

@WorkflowInterface
interface GreetingWorkflow {

    @WorkflowMethod
    fun greet(name: String): String
}
