package com.syscom.fep.batch.base.task;

import com.ibm.db2.jcc.am.DisconnectNonTransientConnectionException;
import com.syscom.fep.batch.base.enums.BatchReturnCode;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.frmcommon.esapi.ESAPIConfiguration;
import com.syscom.fep.frmcommon.ref.RefBase;
import com.syscom.fep.frmcommon.ref.RefString;
import com.zaxxer.hikari.HikariConfig;
import com.zaxxer.hikari.HikariDataSource;
import com.zaxxer.hikari.pool.HikariPool;
import org.apache.commons.collections4.CollectionUtils;
import org.apache.commons.lang3.StringUtils;
import org.jasypt.encryption.StringEncryptor;
import org.jasypt.encryption.pbe.PooledPBEStringEncryptor;
import org.jasypt.encryption.pbe.config.SimpleStringPBEConfig;

import java.io.*;
import java.net.URL;
import java.nio.charset.StandardCharsets;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.sql.SQLException;
import java.sql.SQLNonTransientConnectionException;
import java.sql.SQLTransientConnectionException;
import java.text.SimpleDateFormat;
import java.time.Instant;
import java.time.ZoneId;
import java.time.ZonedDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.logging.*;

/**
 * 單獨執行Task的父類, 除必須的第三方API外, 盡量使用純JAVA程式撰寫
 *
 * @author Richard
 */
public abstract class TaskBase implements TaskConstant, Task {
    protected final String ProgramName = this.getClass().getSimpleName();
    protected BatchReturnCode rtnCode = BatchReturnCode.Succeed;
    protected BatchJobLibrary job;
    protected boolean callBatchJob;
    private Map<String, HikariDataSource> dsMap;
    private Map<String, String> arguments;
    private Logger logger;
    private final Properties properties = new Properties();
    private StringEncryptor encryptor;
    private List<Object[]> initLogs = new ArrayList<>();
    private boolean forceToLoadProperties = false;

    static {
        // 禁用ESAPI的log
        ESAPIConfiguration.init();
    }

    /**
     * 執行
     *
     * @return
     * @throws Exception
     */
    protected abstract boolean process(String[] args, RefString resultMessage) throws Exception;

    /**
     * 實作Task的方法, 兼容Task程式, 通過批次平台呼叫
     *
     * @param args
     * @return
     */
    @Override
    public BatchReturnCode execute(String[] args) {
        this.execute(args, true);
        return this.rtnCode;
    }

    /**
     * 通過java指令直接啟動
     *
     * @param args
     */
    protected void executeMain(String[] args) {
        this.execute(args, false);
    }

    /**
     * 開始執行
     *
     * @param args         傳入的參數
     * @param callBatchJob 是否從批次平台呼叫
     */
    private void execute(String[] args, boolean callBatchJob) {
        if (args == null || args.length == 0 || "?".equals(args[0])) {
            displayUsage();
            return;
        }
        this.callBatchJob = callBatchJob;
        try {
            this.loadProperties(false);
            this.loadPkiProperties(false);
            String batchLogPath = this.configurationLogger(args);
            this.initComponent();
            // 如果是批次平台呼叫, 則初始化BatchJobLibrary
            if (callBatchJob) {
                this.initialBatch(args, batchLogPath);
                job.writeLog("------------------------------------------------------------------");
                job.writeLog(ProgramName + "開始");
                job.startTask();
            } else {
                this.arguments = new HashMap<>();
                this.arguments.put("BatchLogPath", batchLogPath);
                this.extractBatchParameter(args);
            }
            this.log("開始執行 ", ProgramName, ", batchLogPath:", batchLogPath);
            this.log("傳入參數 ", append(args));
            RefString resultMessage = new RefString(null);
            boolean result = this.process(args, resultMessage);
            this.log("執行完成 ", ProgramName, StringUtils.isBlank(resultMessage.get()) ? StringUtils.EMPTY : StringUtils.join(", ", resultMessage.get()));
            // 如果是批次平台呼叫, 則通知批次作業管理系統工作正常結束
            if (callBatchJob) {
                if (result) {
                    job.writeLog(ProgramName, "正常結束!!");
                    job.writeLog("------------------------------------------------------------------");
                    job.endTask();
                } else {
                    job.writeLog(ProgramName, "不正常結束，停止此批次作業!!");
                    job.writeLog("------------------------------------------------------------------");
                    job.abortTask();
                }
            }
            rtnCode = result ? BatchReturnCode.Succeed : BatchReturnCode.Failed;
        } catch (Exception e) {
            this.log(Level.SEVERE, e, "執行", ProgramName + "出現異常, ", e.getMessage());
            if (callBatchJob) {
                if (job != null) {
                    job.writeErrorLog(e, e.getMessage());
                    job.writeLog(ProgramName, "失敗!!");
                    job.writeLog("------------------------------------------------------------------");
                    // 通知批次作業管理系統工作失敗,暫停後面流程
                    try {
                        job.abortTask();
                    } catch (Exception ex) {
                        job.writeErrorLog(e, e.getMessage());
                    }
                }
            }
            if (e instanceof HikariPool.PoolInitializationException
                    || e instanceof SQLTransientConnectionException
                    || e instanceof SQLNonTransientConnectionException
                    || e.getCause() instanceof DisconnectNonTransientConnectionException
                    || e.getCause() instanceof SQLTransientConnectionException
                    || e.getCause() instanceof SQLNonTransientConnectionException) {
                rtnCode = BatchReturnCode.DbConnectionException;
            } else if (e instanceof SQLException) {
                rtnCode = BatchReturnCode.SqlException;
            } else {
                rtnCode = BatchReturnCode.JavaException;
            }
        } finally {
            if (logger != null) {
                for (Handler handler : logger.getHandlers()) {
                    try {
                        handler.close();
                    } catch (SecurityException e) {
                        this.log(Level.WARNING, e, e.getMessage());
                    }
                }
            }
        }
        // 非批次平台下運行, 不要執行System.exit, 否則會將批次平台程式直接shutdown了
        if (!callBatchJob) {
            this.log(rtnCode == BatchReturnCode.Succeed ? Level.INFO : Level.SEVERE, "System.exit(", rtnCode.getValue(), ")");
            System.exit(rtnCode.getValue());
        }
    }

    /**
     * 初始化元件
     */
    private void initComponent() {
        if (!this.callBatchJob) {
            this.encryptor = createJasyptStringEncryptor();
        }
    }

    /**
     * 將傳入的參數存入map中
     *
     * @param args
     */
    private void extractBatchParameter(String[] args) {
        for (String s : args) {
            this.log(ProgramName, " extract parameter = [", s, "]");
            String[] arg = s.split(":", 2);
            if (arg.length > 1) {
                if (StringUtils.isNotBlank(arg[0]))
                    this.arguments.put(arg[0].substring(1), arg[1]);
            } else {
                if (StringUtils.isNotBlank(arg[0]))
                    this.arguments.put(arg[0].substring(1), StringUtils.EMPTY);
            }
        }
    }

    /**
     * 獲取存入的參數map
     *
     * @return
     */
    public Map<String, String> getArguments() {
        return job == null ? arguments : job.getArguments();
    }

    /**
     * 載入配置檔task.properties
     *
     * @param forceLoad
     * @throws Exception
     */
    private void loadProperties(boolean forceLoad) throws Exception {
        if (!forceLoad && this.callBatchJob)
            return;
        String configFile = System.getProperty("task.properties.file", CONFIGURATION_FILE);
        URL url = TaskBase.class.getClassLoader().getResource(configFile);
        if (url != null) {
            this.log("start to load properties from classpath:", configFile, ", which fileSystemPath:", url.getPath());
        } else {
            this.log(Level.SEVERE, "cannot load properties from classpath for configFile:", configFile);
        }
        try (InputStream in = TaskBase.class.getClassLoader().getResourceAsStream(configFile)) {
            this.properties.load(in);
        } catch (IOException e) {
            this.log(Level.SEVERE, e, "載入配置檔", configFile, "出現異常, ", e.getMessage());
            throw e;
        }
    }

    /**
     * 載入PKI檔
     *
     * @param forceLoad
     * @throws IOException
     */
    private void loadPkiProperties(boolean forceLoad) throws IOException {
        if (!forceLoad && this.callBatchJob)
            return;
        String pkiPath = properties.getProperty(PKI_PROPERTIES_KEY);
        this.log(PKI_PROPERTIES_KEY, ":", pkiPath);
        if (pkiPath != null && !pkiPath.trim().isEmpty()) {
            StringTokenizer st = new StringTokenizer(pkiPath, ",");
            while (st.hasMoreTokens()) {
                String path = st.nextToken().trim();
                this.log("start to load properties from PKI path:", path);
                try (InputStream in = Files.newInputStream(Paths.get(path))) {
                    Properties props = new Properties();
                    props.load(in);
                    this.properties.putAll(props);
                } catch (Exception e) {
                    this.log(Level.SEVERE, e, "載入PKI檔", path, "出現異常, ", e.getMessage());
                    throw e;
                }
            }
        }
    }

    /**
     * 初始化logger
     *
     * @param args
     * @return
     */
    private String configurationLogger(String[] args) {
        // batchLogPath優先抓傳入的參數
        final RefBase<String> batchLogPath = new RefBase<>(findArg(args, "BatchLogPath", StringUtils.EMPTY));
        // 如果傳入的參數沒有, 則從配置檔中取
        if (StringUtils.isBlank(batchLogPath.get())) {
            batchLogPath.set(properties.getProperty(PROPERTIES_KEY_LOG_PATH));
        }
        // 如果配置檔中也沒有, 則從db中取
        if (StringUtils.isBlank(batchLogPath.get())) {
            String sql = "SELECT SYSCONF_VALUE FROM SYSCONF WHERE SYSCONF_NAME = 'BatchLogPath' AND SYSCONF_SUBSYSNO='9'";
            try {
                this.executeQuery(DBName.FEPDB, sql, rs -> {
                    if (rs.next()) {
                        batchLogPath.set(rs.getString("SYSCONF_VALUE"));
                    }
                });
            } catch (Throwable t) {
                this.log(Level.SEVERE, t, "查詢BatchLogPath出現異常");
            }
        }
        // 如果從DB中取不到, 則預設取jar檔所在資料夾的logs資料夾
        if (StringUtils.isBlank(batchLogPath.get())) {
            batchLogPath.set(new File(this.getCurrentDir().getParentFile(), "logs").getAbsolutePath());
        }
        if (!callBatchJob) {
            String today = new SimpleDateFormat("yyyy-MM-dd").format(Calendar.getInstance().getTime());
            // log檔命名規則為, $batchLogPath/yyyy-MM-dd/BatchTask/$ProgramName.log
            Path path = Paths.get(batchLogPath.get(), today, "BatchTask", ProgramName + ".log");
            File batchLog = path.toAbsolutePath().toFile();
            // 這裡要判斷一下, 如果log目錄不存在, 則需要先創建
            if (!batchLog.getParentFile().exists()) {
                batchLog.getParentFile().mkdirs();
            }
            // 處理過程中的相關動作, 需呼叫WriteLog寫入batchLogPath的log檔中以便追查問題, batchLog檔名為XXXXX_yyyyMMdd.log,若該批次同一天執行多次則一直append同一log檔,不可overwrite
            try {
                logger = Logger.getLogger(this.getClass().getName());
                logger.setUseParentHandlers(false);
                logger.addHandler(this.createConsoleHandler());
                logger.addHandler(this.createFileHandler(batchLog));
                logger.setLevel(Level.ALL);
                this.log("初始化FileHandler成功, Log檔為:", batchLog.getAbsolutePath());
            } catch (IOException e) {
                this.log(Level.SEVERE, e, "初始化FileHandler出現異常");
            }
        }
        return batchLogPath.get();
    }

    /**
     * 創建FileHandler
     *
     * @return
     * @throws IOException
     */
    private ConsoleHandler createConsoleHandler() throws IOException {
        // 預設是BIG5編碼
        String encoding = properties.getProperty(PROPERTIES_KEY_LOG_CONSOLE_ENCODING);
        if (StringUtils.isNotBlank(encoding))
            encoding = "BIG5";
        ConsoleHandler consoleHandler = new ConsoleHandler();
        consoleHandler.setEncoding(encoding);
        consoleHandler.setLevel(Level.ALL);
        consoleHandler.setFormatter(this.createSimpleFormatter());
        return consoleHandler;
    }

    /**
     * 創建FileHandler
     *
     * @param batchLog
     * @return
     * @throws IOException
     */
    private FileHandler createFileHandler(File batchLog) throws IOException {
        // 預設是UTF_8編碼
        String encoding = properties.getProperty(PROPERTIES_KEY_LOG_FILE_ENCODING);
        if (StringUtils.isNotBlank(encoding))
            encoding = StandardCharsets.UTF_8.name();
        FileHandler fileHandler = new FileHandler(batchLog.getAbsolutePath(), true);
        fileHandler.setEncoding(encoding);
        fileHandler.setLevel(Level.ALL);
        fileHandler.setFormatter(this.createSimpleFormatter());
        return fileHandler;
    }

    /**
     * 創建Log日誌記錄的Formatter
     *
     * @return
     */
    private SimpleFormatter createSimpleFormatter() {
        return new SimpleFormatter() {
            @Override
            public String format(LogRecord record) {
                ZonedDateTime zdt = ZonedDateTime.ofInstant(Instant.ofEpochMilli(record.getMillis()), ZoneId.systemDefault());
                String dateTime = zdt.format(DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss.SSS"));
                String message = formatMessage(record);
                String throwable = StringUtils.EMPTY;
                if (record.getThrown() != null) {
                    StringWriter sw = new StringWriter();
                    PrintWriter pw = new PrintWriter(sw);
                    pw.println();
                    record.getThrown().printStackTrace(pw);
                    pw.close();
                    throwable = sw.toString();
                }
                return String.format("[%s]%s%s%s", dateTime, message, throwable, throwable.trim().isEmpty() ? System.lineSeparator() : StringUtils.EMPTY);
            }
        };
    }

    /**
     * 初始化相關批次物件及拆解傳入參數初始化相關批次物件及拆解傳入參數
     *
     * @param args
     * @param batchLogPath
     */
    protected void initialBatch(String[] args, String batchLogPath) {
        job = new BatchJobLibrary(this, args, batchLogPath);
    }

    /**
     * 根據DBName取得DataSource
     *
     * @param dbName
     * @return
     * @throws IOException
     */
    @Override
    public synchronized HikariDataSource getDataSource(DBName dbName) {
        if (this.callBatchJob) {
            // 有些DataSource的定義並沒有在FEP專案中, 故這裡會取不到, 則後面強制載入task.properties檔, 載入DataSource設定
            HikariDataSource ds = Task.super.getDataSource(dbName);
            if (ds != null) {
                return ds;
            }
            // 強制載入task.properties檔, 載入DataSource設定, 避免重複載入
            if (!forceToLoadProperties) {
                forceToLoadProperties = true;
                try {
                    this.log(Level.INFO, "Force to load properties...");
                    this.loadProperties(true);
                    this.loadPkiProperties(true);
                } catch (Exception e) {
                    this.log(Level.SEVERE, e, "Force to load properties failed");
                }
            }
        }
        if (dsMap == null)
            dsMap = new HashMap<>();
        HikariDataSource ds = dsMap.get(dbName.name());
        if (ds == null) {
            ds = this.initHikariDataSource(dbName, properties);
            dsMap.put(dbName.name(), ds);
        }
        return ds;
    }

    /**
     * 初始化資料庫連接池
     *
     * @param dbName
     * @param properties
     * @return
     */
    private HikariDataSource initHikariDataSource(DBName dbName, Properties properties) {
        String dsName = dbName.getDataSourceNameProperties();
        HikariConfig hikariConfig = new HikariConfig();
        hikariConfig.setPoolName(dsName + "Pool");
        hikariConfig.setMinimumIdle(Integer.parseInt(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".minimum-idle", "2")));
        hikariConfig.setMaximumPoolSize(Integer.parseInt(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".maximum-pool-size", "10")));
        hikariConfig.setIdleTimeout(Long.parseLong(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".idle-timeout", "540000")));
        hikariConfig.setMaxLifetime(Long.parseLong(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".max-lifetime", "600000")));
        hikariConfig.setConnectionTimeout(Long.parseLong(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".connection-timeout", "60000")));
        hikariConfig.setConnectionTestQuery(properties.getProperty(HIKARI_PROPERTIES_KEY_PREFIX + ".connection-test-query", "SELECT 1 FROM SYSIBM.SYSDUMMY1"));
        hikariConfig.setUsername(properties.getProperty(DATA_SOURCE_PROPERTIES_KEY_PREFIX + "." + dsName + ".username"));
        hikariConfig.setPassword(decrypt(properties.getProperty(DATA_SOURCE_PROPERTIES_KEY_PREFIX + "." + dsName + ".password")));
        hikariConfig.setDriverClassName(properties.getProperty(DATA_SOURCE_PROPERTIES_KEY_PREFIX + "." + dsName + ".driver-class-name"));
        hikariConfig.setJdbcUrl(properties.getProperty(DATA_SOURCE_PROPERTIES_KEY_PREFIX + "." + dsName + ".jdbc-url"));
        return new HikariDataSource(hikariConfig);
    }

    /**
     * 記錄日誌訊息
     *
     * @param level
     * @param t
     * @param messages
     * @return
     */
    @Override
    public String log(Level level, Throwable t, Object... messages) {
        if (CollectionUtils.isNotEmpty(initLogs) && (logger != null || job != null)) {
            Object[][] logs = new Object[initLogs.size()][3];
            initLogs.toArray(logs);
            initLogs.clear();
            initLogs = null;
            for (Object[] log : logs) {
                log((Level) log[0], (Throwable) log[1], log[2]);
            }
        }
        StringBuilder sb = new StringBuilder();
        for (Object message : messages) {
            sb.append(message);
        }
        String message = sb.toString();
        if (logger != null) {
            logger.log(level, message, t);
        } else if (job != null) {
            if (level == Level.SEVERE) {
                job.writeErrorLog(t, message);
                com.syscom.fep.base.vo.LogData logContext = new com.syscom.fep.base.vo.LogData();
                logContext.setProgramException(t);
                logContext.setProgramName(ProgramName);
                logContext.setChannel(com.syscom.fep.base.enums.FEPChannel.BATCH);
                logContext.setSubSys(com.syscom.fep.base.enums.SubSystem.CMN);
                BatchJobLibrary.sendEMS(logContext);
            } else if (level == Level.WARNING) {
                job.writeWarnLog(t, message);
            } else {
                job.writeLog(message);
            }
        } else if (initLogs != null) {
            initLogs.add(new Object[] {level, t, message});
        }
        return message;
    }

    /**
     * 初始化解碼器
     *
     * @return
     */
    private StringEncryptor createJasyptStringEncryptor() {
        SimpleStringPBEConfig config = new SimpleStringPBEConfig();
        config.setPassword("Syscom@123");
        config.setAlgorithm("PBEWITHHMACSHA512ANDAES_256");
        config.setKeyObtentionIterations("1000");
        config.setPoolSize("1");
        config.setSaltGeneratorClassName("org.jasypt.salt.RandomSaltGenerator");
        config.setIvGeneratorClassName("org.jasypt.iv.RandomIvGenerator");
        config.setStringOutputType("base64");
        PooledPBEStringEncryptor encryptor = new PooledPBEStringEncryptor();
        encryptor.setConfig(config);
        return encryptor;
    }

    /**
     * 進行jasypt解碼
     *
     * @param input
     * @return
     */
    public String decrypt(String input) {
        if (encryptor == null) {
            return Task.super.decrypt(input);
        }
        if (input.startsWith("ENC(") && input.endsWith(")")) {
            return encryptor.decrypt(input.substring(4, input.lastIndexOf(")")));
        }
        return encryptor.decrypt(input);
    }
}
