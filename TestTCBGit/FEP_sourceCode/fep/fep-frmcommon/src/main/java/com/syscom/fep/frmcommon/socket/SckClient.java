package com.syscom.fep.frmcommon.socket;

import com.syscom.fep.frmcommon.scheduler.AbstractScheduledTask;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.net.SocketClient;

import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.concurrent.TimeUnit;

public class SckClient extends SocketClient {
    private final List<SckConnStateListener> sckConnStateListeners = Collections.synchronizedList(new ArrayList<>());
    private SckClientReestablishConnectionTask reestablishConnectionTask;
    private SckClientDetectConnectionTask detectConnectionTask;

    /**
     * 當初次建立連線, 以及連線中檢測到斷線, 每次嘗試重新建立連線的時間間隔, 如果小于等于0表示不嘗試重新建立連線
     */
    private long reestablishConnectionIntervalMilliseconds = 5000L;
    /**
     * 每隔多少毫秒檢測連線狀態, 如果小于等于0表示不檢測
     */
    private long detectConnectionIntervalMilliseconds = 5000L;
    /**
     * 是否通過呼叫Socket.sendUrgentData方法檢測連線狀態
     */
    private boolean detectConnectionBySendUrgentData = true;

    public SckClient() {
        super();
    }

    @Override
    public void connect(final InetAddress host, final int port) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(host, port);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(host, port);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    public void connect(final String hostname, final int port) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(hostname, port);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(hostname, port);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    public void connect(final InetAddress host, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(host, port, localAddr, localPort);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(host, port, localAddr, localPort);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    public void connect(final String hostname, final int port, final InetAddress localAddr, final int localPort) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(hostname, port, localAddr, localPort);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(hostname, port, localAddr, localPort);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    public void connect(final InetAddress host) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(host);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(host);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    public void connect(final String hostname) throws IOException {
        sckConnStateChanged(SckConnState.CLIENT_CONNECTING);
        if (reestablishConnectionIntervalMilliseconds > 0 && reestablishConnectionTask == null) {
            reestablishConnectionTask = new SckClientReestablishConnectionTask() {
                @Override
                public void execute() {
                    try {
                        connect(hostname);
                    } catch (IOException e) {
                    }
                }
            };
        }
        try {
            super.connect(hostname);
        } catch (IOException e) {
            sckConnStateChanged(SckConnState.CLIENT_CONNECT_FAILED, e);
            throw e;
        }
    }

    @Override
    protected void _connectAction_() throws IOException {
        super._connectAction_();
        sckConnStateChanged(SckConnState.CLIENT_CONNECTED);
    }

    @Override
    public void disconnect() throws IOException {
        try {
            super.disconnect();
        } finally {
            if (this.detectConnectionTask != null) {
                this.detectConnectionTask.cancel();
                this.detectConnectionTask = null;
            }
            if (this.reestablishConnectionTask != null) {
                this.reestablishConnectionTask.cancel();
                this.reestablishConnectionTask = null;
            }
        }
    }

    public OutputStream getOutputStream() {
        return this._output_;
    }

    public InputStream getInputStream() {
        return this._input_;
    }

    public void shutdownOutput() throws IOException {
        this._socket_.shutdownOutput();
    }

    public void shutdownInput() throws IOException {
        this._socket_.shutdownInput();
    }

    /**
     * 當初次建立連線, 以及連線中檢測到斷線, 設置每次嘗試重新建立連線的時間間隔, 如果小于等于0表示不嘗試重新建立連線
     *
     * @param reestablishConnectionIntervalMilliseconds
     */
    public void setReestablishConnectionIntervalMilliseconds(long reestablishConnectionIntervalMilliseconds) {
        this.reestablishConnectionIntervalMilliseconds = reestablishConnectionIntervalMilliseconds;
    }

    /**
     * 設置每隔多少毫秒檢測連線狀態, 如果小于等于0表示不檢測
     *
     * @param detectConnectionIntervalMilliseconds
     */
    public void setDetectConnectionIntervalMilliseconds(long detectConnectionIntervalMilliseconds) {
        this.detectConnectionIntervalMilliseconds = detectConnectionIntervalMilliseconds;
    }

    /**
     * 設置是否通過呼叫Socket.sendUrgentData方法檢測連線狀態
     *
     * @param detectConnectionBySendUrgentData
     */
    public void setDetectConnectionBySendUrgentData(boolean detectConnectionBySendUrgentData) {
        this.detectConnectionBySendUrgentData = detectConnectionBySendUrgentData;
    }

    /**
     * 增加SckConnStateListener實例用於監聽連線狀態
     *
     * @param listener
     */
    public void addSckConnStateListener(SckConnStateListener listener) {
        if (!this.sckConnStateListeners.contains(listener))
            this.sckConnStateListeners.add(listener);
    }

    /**
     * 檢測連線狀態
     *
     * @param state
     */
    private void sckConnStateChanged(SckConnState state, Throwable... t) {
        this.handelSckConnState(state);
        if (sckConnStateListeners.isEmpty())
            return;
        SckConnStateListener[] listeners = new SckConnStateListener[sckConnStateListeners.size()];
        sckConnStateListeners.toArray(listeners);
        if (ArrayUtils.isNotEmpty(listeners)) {
            if (ArrayUtils.isEmpty(t) || t[0] == null) {
                for (SckConnStateListener listener : listeners) {
                    listener.connStateChanged(state, null);
                }
            } else {
                for (SckConnStateListener listener : listeners) {
                    listener.connStateChanged(state, t[0]);
                }
            }
        }
    }

    private void handelSckConnState(SckConnState state) {
        switch (state) {
            case CLIENT_CONNECT_FAILED:
            case CLIENT_DISCONNECTED:
                if (detectConnectionTask != null)
                    detectConnectionTask.cancel();
                if (this.reestablishConnectionTask != null && this.reestablishConnectionTask.isCancelled())
                    this.reestablishConnectionTask.scheduleAtFixedRate(5000L, this.reestablishConnectionIntervalMilliseconds, TimeUnit.MILLISECONDS);
                break;
            case CLIENT_CONNECTED:
                if (this.reestablishConnectionTask != null)
                    this.reestablishConnectionTask.cancel();
                if (detectConnectionIntervalMilliseconds > 0 && detectConnectionTask == null)
                    detectConnectionTask = new SckClientDetectConnectionTask();
                if (detectConnectionTask != null && detectConnectionTask.isCancelled())
                    detectConnectionTask.scheduleAtFixedRate(5000L, detectConnectionIntervalMilliseconds, TimeUnit.MILLISECONDS);
                break;
        }
    }

    private abstract class SckClientReestablishConnectionTask extends AbstractScheduledTask {

        public SckClientReestablishConnectionTask() {
            super("SckClientReestablishConnection");
        }
    }

    private class SckClientDetectConnectionTask extends AbstractScheduledTask {

        public SckClientDetectConnectionTask() {
            super("SckClientDetectConnection");
        }

        /**
         * Execute Task
         */
        @Override
        public void execute() {
            if (_socket_ == null)
                return;
            boolean isConnected = isAvailable();
            if (isConnected && detectConnectionBySendUrgentData) {
                try {
                    _socket_.sendUrgentData(0);
                    isConnected = true;
                } catch (IOException e) {
                    isConnected = false;
                }
            }
            sckConnStateChanged(isConnected ? SckConnState.CLIENT_CONNECTED : SckConnState.CLIENT_DISCONNECTED);
        }
    }
}
