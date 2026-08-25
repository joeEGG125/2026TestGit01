package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.aa.ATMData;
import com.syscom.fep.base.aa.MessageBase;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.configuration.ATMPConfig;
import com.syscom.fep.frmcommon.net.http.HttpClient;
import com.syscom.fep.frmcommon.net.http.HttpClientConfiguration;
import com.syscom.fep.frmcommon.ssl.SslContextFactory;
import com.syscom.fep.frmcommon.util.FormatUtil;
import com.syscom.fep.server.common.business.atm.ATM;
import org.apache.commons.collections4.MapUtils;
import org.apache.commons.lang3.ArrayUtils;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;
import org.springframework.http.*;
import org.springframework.web.client.HttpClientErrorException;
import org.springframework.web.client.RestTemplate;

import java.util.Calendar;
import java.util.HashMap;
import java.util.Map;

public class FIDOAdapter extends AdapterBase{

    private static final String ProgramName = StringUtils.join(FIDOAdapter.class.getSimpleName(), ".");
    /**
     * 從FIDO回來的電文
     */
    public String messageFromFIDO;
    /**
     * 送給FIDO的電文
     */
    public String messageToFIDO;

    private MessageBase txData;

    private HttpMethod httpMethod = HttpMethod.POST;

    private int httpStatusValue;

    private Map<String,Object> requestHeader = new HashMap<>();

    private String fidoApi = "/portal/fisc/notifyVerifyResult";  //API: /portal/fisc/notifyVerifyResult
    
    private ATM fATMBusiness;

    //測試區 https://fidoadm.tcbt.com:9443
    //正式區 https://fidoadm.tcb.com:9443
    private String fidoUrl = ATMPConfig.getInstance().getFIDOServiceUrl();

    public FIDOAdapter(ATMData txData) throws Exception {
        this.txData = txData;
        fATMBusiness = new ATM(txData);
    }
    
    public String getMessageFromFIDO() {
        return messageFromFIDO;
    }

    public void setMessageFromFIDO(String messageFromFIDO) {
        this.messageFromFIDO = messageFromFIDO;
    }

    public String getMessageToFIDO() {
        return messageToFIDO;
    }

    public void setMessageToFIDO(String messageToFIDO) {
        this.messageToFIDO = messageToFIDO;
    }

    public HttpMethod getHttpMethod() {
        return httpMethod;
    }

    public void setHttpMethod(HttpMethod httpMethod) {
        this.httpMethod = httpMethod;
    }

    public int getHttpStatusValue() {
        return httpStatusValue;
    }

    public void setHttpStatusValue(int httpStatusValue) {
        this.httpStatusValue = httpStatusValue;
    }

    public String getFidoUrl() {
        return fidoUrl;
    }

    public void setFidoUrl(String fidoUrl) {
        this.fidoUrl = fidoUrl;
    }

    public String getFidoApi() {
        return fidoApi;
    }

    public void setFidoApi(String fidoApi) {
        this.fidoApi = fidoApi;
    }
    
    public ATM getATMBusiness() {
		return fATMBusiness;
	}

	@Override
    public FEPReturnCode sendReceive() {
        FEPReturnCode rtnCode = FEPReturnCode.FIDOAPIError;
        //同 MobileAdapter 的 timeout 時間
        timeout = 60 + 5;

        //判斷是否有httpMethod、fidoApiUrl
        if (null == httpMethod || StringUtils.isBlank(fidoUrl) || StringUtils.isBlank(fidoApi)) {
            return rtnCode;
        }

        try {
            return sendAndReceive();
        } catch (Exception e) {
            this.txData.getLogContext().setProgramException(e);
            sendEMS(this.txData.getLogContext());
            rtnCode = CommonReturnCode.ProgramException;
        } finally {
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterOut);
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendReceive"));
            this.txData.getLogContext().setMessage(this.messageFromFIDO);
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Response);
            this.txData.getLogContext().setRemark(StringUtils.join("[", getHttpStatusValue(),"]","Get data from FIDO ", this.fidoUrl, this.fidoApi));
            logMessage(Level.INFO, this.txData.getLogContext());
        }
        // 呼叫FIDO所提供的Web Service
        return rtnCode;
    }


    public FEPReturnCode sendAndReceive() {
        FEPReturnCode rtnCode = FEPReturnCode.FIDOAPIError;
        try {
            //prepare Header
            HttpHeaders headers = new HttpHeaders();

            //set FIDO header
            getFidoHeader();
            headers.setContentType(MediaType.APPLICATION_JSON);
            if (MapUtils.isNotEmpty(requestHeader)){
                for (String k : requestHeader.keySet()){
                    headers.add(k, String.valueOf(requestHeader.get(k)));
                }
            }

            // HttpEntity
            HttpEntity<?> requestEntity = new HttpEntity<>(this.messageToFIDO, headers);
            RestTemplate restTemplate = new RestTemplate();
            //add for https
            if (HttpClient.isHttps(fidoUrl)) {
                restTemplate.setRequestFactory(HttpClientConfiguration.createTrustAnyHttpComponentsClientHttpRequestFactory(SslContextFactory.PROTOCOL, this.timeout * 1000));
            } else {
                restTemplate.setRequestFactory(HttpClientConfiguration.createSimpleClientHttpRequestFactory(this.timeout * 1000));
            }
            this.txData.getLogContext().setProgramFlowType(ProgramFlow.AdapterIn);
            this.txData.getLogContext().setMessage(StringUtils.join("[", ArrayUtils.toString(headers), "]",this.messageToFIDO));
            this.txData.getLogContext().setProgramName(StringUtils.join(ProgramName, "sendReceive"));
            this.txData.getLogContext().setMessageFlowType(MessageFlow.Request);
            this.txData.getLogContext().setRemark(StringUtils.join("HttpMethod [", httpMethod.name(), "], Ready Send data to FIDO ", this.fidoUrl, this.fidoApi));
            logMessage(Level.INFO, this.txData.getLogContext());
            ResponseEntity<String> responseEntity = null;

            responseEntity = restTemplate.exchange(StringUtils.join(this.fidoUrl, this.fidoApi), httpMethod, requestEntity, String.class);
            setMessageFromFIDO(responseEntity.getBody());
            setHttpStatusValue(responseEntity.getStatusCode().value());
            rtnCode = FEPReturnCode.Normal;

        }catch (HttpClientErrorException httpClientErrorException){
            setHttpStatusValue(httpClientErrorException.getRawStatusCode());
            setMessageFromFIDO(httpClientErrorException.getResponseBodyAsString());

            if (400 == getHttpStatusValue() || 500 == getHttpStatusValue()) {
                rtnCode = FEPReturnCode.Normal;
            }

        }catch (Exception exception) {
            setHttpStatusValue(HttpStatus.NOT_FOUND.value());
            exception.printStackTrace();

            rtnCode = FEPReturnCode.FIDOAPIError;
            getLogContext().setRemark(exception.getMessage());
            getLogContext().setProgramException(exception);
            getLogContext().setReturnCode(rtnCode);
            sendEMS(getLogContext());
        }

        return rtnCode;

    }

    private void getFidoHeader(){
        try {
            if (this.requestHeader == null) {
                this.requestHeader = new HashMap<>();
            }
            String time = FormatUtil.dateTimeFormat(Calendar.getInstance(), FormatUtil.FORMAT_DATE_YYYYMMDDHHMMSS_PLAIN);
            requestHeader.put("time",time);
            //code
            String code = getATMBusiness().encryptSHA512(StringUtils.join(ATMPConfig.getInstance().getATMFIDOSecretKey(), time));
            requestHeader.put("code", code);
            //channelId
            requestHeader.put("channelId", "atm_svr");

        }catch (Exception e) {
            getLogContext().setProgramName(ProgramName+"getFidoHeader");
            getLogContext().setRemark(e.getMessage());
            getLogContext().setProgramException(e);
            sendEMS(getLogContext());
        }
    }

    public void addRequestHeader(String name, Object value) {
       if (this.requestHeader == null) {
           this.requestHeader = new HashMap<>();
       }
        this.requestHeader.put(name, value);
   }
}
