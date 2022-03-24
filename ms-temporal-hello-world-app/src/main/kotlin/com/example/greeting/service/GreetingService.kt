package com.example.greeting.service

import greeting.share.Queue
import greeting.workflow.GreetingWorkflow
import io.temporal.client.WorkflowClient
import io.temporal.client.WorkflowOptions
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import org.springframework.stereotype.Service

@Service
class GreetingService {
    fun greet(name: String): String {
        val stubOptions = WorkflowServiceStubsOptions.newBuilder()
            .setTarget("temporal-frontend:7233")
            .build()
        val service = WorkflowServiceStubs.newInstance(stubOptions)
        val client = WorkflowClient.newInstance(service)
        val options = WorkflowOptions.newBuilder()
            .setTaskQueue(Queue.HELLO_TASK_QUEUE)
            .build()

        val workflow = client.newWorkflowStub(GreetingWorkflow::class.java, options)

        // TODO: Maybe we have to handle error here
        //  (such as cannot connect to Temporal Server)
        return workflow.greet(name)
    }
}