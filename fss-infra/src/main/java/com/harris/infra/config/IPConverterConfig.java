package com.harris.infra.config;

import ch.qos.logback.classic.pattern.ClassicConverter;
import ch.qos.logback.classic.spi.ILoggingEvent;

import java.net.InetAddress;
import java.net.UnknownHostException;

public class IPConverterConfig extends ClassicConverter {
    /**
     * Converts a logging event to the IP address of the machine where the log is generated.
     * This converter retrieves and returns the local host IP address.
     *
     * @param iLoggingEvent The logging event
     * @return The IP address of the local host
     */
    @Override
    public String convert(ILoggingEvent iLoggingEvent) {
        try {
            return InetAddress.getLocalHost().getHostAddress();
        } catch (UnknownHostException e) {
            e.printStackTrace();
        }
        return null;
    }
}
