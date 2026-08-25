package com.syscom.fep.frmcommon.util;

import com.syscom.fep.frmcommon.log.LogHelper;
import org.apache.commons.io.IOUtils;
import org.apache.commons.lang3.StringUtils;
import org.apache.commons.net.DefaultSocketFactory;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.io.OutputStream;
import java.net.Socket;
import java.util.function.Function;

/**
 * Socket工具類
 *
 * @author Richard
 */
public class SocketUtil {
    private static final LogHelper logger = new LogHelper();
    private static final DefaultSocketFactory defaultSocketFactory = new DefaultSocketFactory();

    private SocketUtil() {}

    /**
     * 判斷TCP連線是否可用
     *
     * @param host
     * @param port
     * @return
     */
    public static boolean isTcpAvailable(String host, int port) {
        return isTcpAvailable(host, port, -1);
    }

    /**
     * 判斷TCP連線是否可用
     *
     * @param host
     * @param port
     * @param soTimeout
     * @return
     */
    public static boolean isTcpAvailable(String host, int port, int soTimeout) {
        try (Socket socket = DefaultSocketFactory.getDefault().createSocket(host, port)) {
            if (soTimeout > 0)
                socket.setSoTimeout(soTimeout);
            return socket.isConnected();
        } catch (IOException e) {
            return false;
        }
    }

    /**
     * 從Socket物件中獲取本地IP和Port
     *
     * @param socket
     * @return
     */
    public static String getLocalAddress(Socket socket) {
        return socket == null ? StringUtils.EMPTY : StringUtils.join(socket.getLocalAddress().getHostAddress(), ":", socket.getLocalPort());
    }

    /**
     * 從Socket物件中獲取遠程IP和Port
     *
     * @param socket
     * @return
     */
    public static String getRemoteAddress(Socket socket) {
        return socket == null ? StringUtils.EMPTY : StringUtils.join(socket.getInetAddress().getHostAddress(), ":", socket.getPort());
    }

    /**
     * Returns true if the client is currently connected to a server.
     * <p>
     * Delegates to {@link Socket#isConnected()}
     *
     * @return True if the client is currently connected to a server,
     * false otherwise.
     */
    public static boolean isConnected(Socket socket) {
        if (socket == null) {
            return false;
        }
        return socket.isConnected();
    }

    /**
     * Make various checks on the socket to test if it is available for use.
     * Note that the only sure test is to use it, but these checks may help
     * in some cases.
     *
     * @return {@code true} if the socket appears to be available for use
     * @see <a href="https://issues.apache.org/jira/browse/NET-350">NET-350</a>
     * @since 3.0
     */
    public static boolean isAvailable(Socket socket) {
        if (isConnected(socket)) {
            try {
                if (socket.getInetAddress() == null) {
                    return false;
                }
                if (socket.getPort() == 0) {
                    return false;
                }
                if (socket.getRemoteSocketAddress() == null) {
                    return false;
                }
                if (socket.isClosed()) {
                    return false;
                }
                /* these aren't exact checks (a Socket can be half-open),
                   but since we usually require two-way data transfer,
                   we check these here too: */
                if (socket.isInputShutdown()) {
                    return false;
                }
                if (socket.isOutputShutdown()) {
                    return false;
                }
                /* ignore the result, catch exceptions: */
                // No need to close
                socket.getInputStream();
                // No need to close
                socket.getOutputStream();
            } catch (final IOException e) {
                return false;
            }
            return true;
        }
        return false;
    }


    /**
     * 通過Socket連線發送並接收十六進制格式訊息。
     *
     * @param host 目標伺服器主機位址
     * @param port 目標伺服器埠號
     * @param messageOut 要發送的十六進制格式訊息
     * @param timeout Socket讀取逾時時間（毫秒）。若大於0則會等待並讀取回應訊息；若小於等於0則僅發送不等待回應
     * @return 接收到的十六進制格式訊息；若未設定逾時或無資料回傳則回傳 null
     * @throws Exception 當建立連線、發送訊息、接收訊息或關閉連線發生錯誤時拋出
     */
    public static String sendReceive(String host, int port, String messageOut, int timeout) throws Exception {
        return sendReceive(host, port, messageOut, timeout, ConvertUtil::hexToBytes, StringUtil::toHex);
    }

    /**
     * 通過Socket連線發送並接收訊息。支援自訂的發送與接收轉換函數。
     *
     * @param host 目標伺服器主機位址
     * @param port 目標伺服器埠號
     * @param messageOut 要發送的訊息物件
     * @param timeout Socket讀取逾時時間（毫秒）。若大於0則會等待並讀取回應訊息；若小於等於0則僅發送不等待回應
     * @param messageOutConverter 用於將發送訊息物件轉換為位元組陣列的函數
     * @param messageInConverter 用於將接收到的位元組陣列轉換為目標訊息物件的函數
     * @param <MessageOut> 發送訊息的類型
     * @param <MessageIn> 接收訊息的類型
     * @return 轉換後的接收訊息物件；若未設定逾時或無資料回傳則回傳 null
     * @throws Exception 當建立連線、發送訊息、接收訊息或關閉連線發生錯誤時拋出
     */
    public static <MessageOut, MessageIn> MessageIn sendReceive(String host, int port, MessageOut messageOut, int timeout, Function<MessageOut, byte[]> messageOutConverter, Function<byte[], MessageIn> messageInConverter) throws Exception {
        String logPrefix = StringUtils.join("[Remote:", host, ":", port, "]");
        Socket client = null;
        try {
            logger.debug(logPrefix, "Try to build connection...");
            client = defaultSocketFactory.createSocket(host, port);
            client.setSoLinger(false, 0);
            client.setTcpNoDelay(true);
            client.setSoTimeout(timeout);
            logger.debug(logPrefix, "Build connection successfully!!!");
            logPrefix = StringUtils.join("[Local:", getLocalAddress(client), "]<==>", logPrefix);
            byte[] send = messageOutConverter.apply(messageOut);
            logger.debug(logPrefix, ">>>>>>", StringUtil.toHex(send));
            OutputStream out = client.getOutputStream();
            out.write(send);
            out.flush();
            logger.debug(logPrefix, "Send message successfully!!! messageOut:", messageOut);
            MessageIn messageIn = null;
            if (timeout > 0) {
                ByteArrayOutputStream bos = new ByteArrayOutputStream();
                byte[] buffer = new byte[1024];
                InputStream in = client.getInputStream();
                int length = in.read(buffer);
                bos.write(buffer, 0, length);
                byte[] recv = bos.toByteArray();
                logger.debug(logPrefix, "<<<<<<", StringUtil.toHex(recv));
                messageIn = messageInConverter.apply(recv);
                logger.debug(logPrefix, "Receive message successfully!!! messageIn:", messageIn);
            }
            return messageIn;
        } catch (Exception e) {
            logger.error(e, logPrefix, "Socket Client send and receive occurred exception!!!");
            throw e;
        } finally {
            try {
                IOUtils.close(client);
            } catch (IOException e) {
                logger.warn(e, logPrefix, "Socket Client close occurred exception!!!");
            }
        }
    }
}
