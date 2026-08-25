package com.syscom.fep.frmcommon.log.appender;

import ch.qos.logback.ext.loggly.LogglyBatchAppender;
import ch.qos.logback.ext.loggly.io.IoUtils;
import com.syscom.fep.frmcommon.log.LogHelper;

import java.io.BufferedOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.HttpURLConnection;
import java.net.URL;

public class MyLogglyBatchAppender<E> extends LogglyBatchAppender<E> {
    private static final String PROGRAM_NAME = MyLogglyBatchAppender.class.getSimpleName();
    private LogHelper logger = new LogHelper();

    /* Store Connection Read Timeout */
    private int connReadTimeoutSeconds = 1;

    @Override
    protected void processLogEntries(InputStream in) {
        long nanosBefore = System.nanoTime();
        HttpURLConnection conn = null;
        OutputStream os = null;
        BufferedOutputStream out = null;
        InputStream is = null;

        try {
            conn = getHttpConnection(new URL(endpointUrl));
            /* Set connection Read Timeout */
            conn.setReadTimeout(connReadTimeoutSeconds * 1000);
            os = conn.getOutputStream();
            out = new BufferedOutputStream(os);

            long len = IoUtils.copy(in, out);
            sentBytes.addAndGet(len);

            out.flush();

            int responseCode = conn.getResponseCode();
            is = conn.getInputStream();
            String response = super.readResponseBody(is);
            switch (responseCode) {
                case HttpURLConnection.HTTP_OK:
                case HttpURLConnection.HTTP_ACCEPTED:
                    sendSuccessCount.incrementAndGet();
                    break;
                default:
                    sendExceptionCount.incrementAndGet();
                    addError(PROGRAM_NAME + " server-side exception: " + responseCode + ": " + response);
            }
        } catch (Exception e) {
            logger.error(e, PROGRAM_NAME, ".processLogEntries with exception occur, ", e.getMessage());
            sendExceptionCount.incrementAndGet();
            addError(PROGRAM_NAME + " client-side exception", e);
        } finally {
            if (out != null) {
                try {
                    out.close();
                } catch (IOException e) {
                    logger.error("Error occurred while closing output stream", e);
                }
            }
            if (os != null) {
                try {
                    os.close();
                } catch (IOException e) {
                    logger.error("Error occurred while closing output stream", e);
                }
            }
            if (is != null) {
                try {
                    is.close();
                } catch (IOException e) {
                    logger.error("Error occurred while closing input stream", e);
                }
            }
            if (conn != null) {
                try {
                    conn.disconnect();
                } catch (Exception e) {
                    logger.error("Error occurred while disconnecting HttpURLConnection", e);
                }
            }
            sendDurationInNanos.addAndGet(System.nanoTime() - nanosBefore);
        }
    }

    /**
     * set method for Logback to allow Connection Read Timeout to be exposed
     */
    public void setConnReadTimeoutSeconds(int connReadTimeoutSeconds) {
        this.connReadTimeoutSeconds = connReadTimeoutSeconds;
    }
}
