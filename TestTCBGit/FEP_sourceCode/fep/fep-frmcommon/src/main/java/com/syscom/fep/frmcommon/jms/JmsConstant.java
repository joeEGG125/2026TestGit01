package com.syscom.fep.frmcommon.jms;

import com.syscom.fep.frmcommon.log.LogHelper;

import java.io.Serializable;

public interface JmsConstant {
    public static final String MESSAGE_IN = "<<<<<<<<<<";
    public static final String MESSAGE_OUT = ">>>>>>>>>>";

    /**
     * print in log
     *
     * @param payload
     */
    default void logIn(LogHelper logger, Serializable payload) {
        logger.info(MESSAGE_IN, payload);
    }

    /**
     * print in log
     *
     * @param logger
     * @param destination
     * @param payload
     */
    default void logIn(LogHelper logger, String destination, Serializable payload) {
        logger.info(MESSAGE_IN, "destination = [", destination, "], payload = [", payload, "]");
    }

    /**
     * print out log
     *
     * @param payload
     */
    default void logOut(LogHelper logger, Serializable payload) {
        logger.info(MESSAGE_OUT, payload);
    }

    /**
     * print out log
     *
     * @param logger
     * @param destination
     * @param payload
     */
    default void logOut(LogHelper logger, String destination, Serializable payload) {
        logger.info(MESSAGE_OUT, "destination = [", destination, "], payload = [", payload, "]");
    }
}
