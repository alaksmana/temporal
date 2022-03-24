package com.example.greeting.worker

import greeting.activity.HelloActivityImpl
import greeting.share.Queue
import greeting.workflow.GreetingWorkflowImpl
import io.temporal.client.WorkflowClient
import io.temporal.serviceclient.WorkflowServiceStubs
import io.temporal.serviceclient.WorkflowServiceStubsOptions
import io.temporal.worker.Worker
import io.temporal.worker.WorkerFactory
import org.springframework.boot.autoconfigure.SpringBootApplication
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration
import org.springframework.boot.context.event.ApplicationReadyEvent
import org.springframework.boot.runApplication
import org.springframework.context.event.EventListener

@SpringBootApplication(exclude = [DataSourceAutoConfiguration::class])
class Main {

    @EventListener(ApplicationReadyEvent::class)
    fun doSomethingAfterStartup() {
        println("hello world, I have just started up")
        val options = WorkflowServiceStubsOptions.newBuilder()
            .setTarget("temporal-frontend:7233")
            .build()
        val service = WorkflowServiceStubs.newInstance(options)
        val client = WorkflowClient.newInstance(service)
        val factory = WorkerFactory.newInstance(client)
        val worker: Worker = factory.newWorker(Queue.HELLO_TASK_QUEUE)
        worker.registerWorkflowImplementationTypes(GreetingWorkflowImpl::class.java)
        worker.registerActivitiesImplementations(HelloActivityImpl())
        factory.start()
    }
}

fun main(args: Array<String>) {
    runApplication<Main>(*args)
}