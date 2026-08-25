package com.syscom.fep.frmcommon.io;

import com.syscom.fep.frmcommon.log.LogHelper;

import java.io.BufferedReader;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.nio.charset.Charset;
import java.nio.charset.StandardCharsets;
import java.util.function.Consumer;

public class StreamGobbler implements Runnable {
    private final LogHelper logger = new LogHelper();
    private InputStream in;
    private Charset cs;
    private Consumer<String> consumer;

    public StreamGobbler() {}

    public StreamGobbler(InputStream in) {
        this(in, StandardCharsets.UTF_8, null);
    }

    public StreamGobbler(InputStream in, Consumer<String> consumer) {
        this(in, StandardCharsets.UTF_8, consumer);
    }

    public StreamGobbler(InputStream in, Charset cs, Consumer<String> consumer) {
        this.in = in;
        this.cs = cs;
        this.consumer = consumer;
    }

    public StreamGobbler(InputStream in, String charsetName, Consumer<String> consumer) {
        this.in = in;
        this.cs = Charset.forName(charsetName);
        this.consumer = consumer;
    }

    @Override
    public void run() {
        if (in != null) {
            try (BufferedReader br = new BufferedReader(new InputStreamReader(in, cs))) {
                if (consumer != null) {
                    br.lines().forEach(consumer);
                } else {
                    while (br.readLine() != null) {}
                }
            } catch (Throwable t) {
                logger.error(t, t.getMessage());
            }
        }
    }
}
