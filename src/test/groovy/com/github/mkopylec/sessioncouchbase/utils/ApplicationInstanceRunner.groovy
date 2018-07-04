package com.github.mkopylec.sessioncouchbase.utils

import com.github.mkopylec.sessioncouchbase.TestApplication
import org.springframework.boot.SpringApplication
import org.springframework.boot.web.servlet.context.ServletWebServerApplicationContext

import static org.springframework.boot.SpringApplication.exit

class ApplicationInstanceRunner {

    private final Object monitor = new Object()
    private ServletWebServerApplicationContext context
    private boolean shouldWait
    private String activeProfiles
    private int port

    void run() {
        if (context != null) {
            throw new IllegalStateException('Application context must be null to run this instance')
        }
        runInstance()
        waitInstanceIsStarted()
    }

    void stop() {
        exit(context)
        context = null
    }

    void setActiveProfiles(String activeProfiles) {
        this.activeProfiles = activeProfiles
    }

    int getPort() {
        return port
    }

    private void waitInstanceIsStarted() {
        synchronized (monitor) {
            if (shouldWait) {
                monitor.wait()
            }
        }
    }

    private void runInstance() {
        def runnerThread = new InstanceRunningThread()
        shouldWait = true
        runnerThread.contextClassLoader = TestApplication.classLoader
        runnerThread.start()
    }

    private class InstanceRunningThread extends Thread {

        @Override
        void run() {
            context = SpringApplication.run(TestApplication, '--server.port=0', "--spring.profiles.active=$activeProfiles") as ServletWebServerApplicationContext
            port = context.webServer.port
            synchronized (monitor) {
                shouldWait = false
                monitor.notify()
            }
        }
    }
}
