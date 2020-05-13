package com.newrelic.jfr.toevent;

import com.newrelic.telemetry.Attributes;
import com.newrelic.telemetry.events.Event;
import jdk.jfr.consumer.RecordedEvent;

import java.util.List;

// Need to handle both jdk.ExecutionSample and jdk.NativeMethodSample...

//jdk.NativeMethodSample {
//        startTime = 10:37:26.131
//        sampledThread = "JFR Periodic Tasks" (javaThreadId = 12)
//        state = "STATE_IN_OBJECT_WAIT_TIMED"
//        stackTrace = [
//        java.lang.Object.wait(long)
//        jdk.jfr.internal.PlatformRecorder.takeNap(long) line: 449
//        jdk.jfr.internal.PlatformRecorder.periodicTask() line: 442
//        jdk.jfr.internal.PlatformRecorder.lambda$startDiskMonitor$1() line: 387
//        jdk.jfr.internal.PlatformRecorder$$Lambda$50.1866850137.run()
//        ...
//        ]
//        }
public class MethodSampleMapper implements EventToEvent {
    public static final String EVENT_NAME = "jdk.ExecutionSample";
    public static final String NATIVE_EVENT_NAME = "jdk.NativeMethodSample";

    @Override
    public List<Event> apply(RecordedEvent ev) {
        var trace = ev.getStackTrace();
        if (trace == null) {
            var timestamp = ev.getStartTime().toEpochMilli();

            var attr = new Attributes();
            attr.put("threadName", ev.getThread("sampledThread").getJavaName());
            attr.put("threadState", ev.getString("state"));
            // FIXME Handle stack trace

            return List.of(new Event("jfr:MethodSample", attr, timestamp));
        }

        return List.of();
    }

    @Override
    public String getEventName() {
        return EVENT_NAME;
    }
}