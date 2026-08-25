package com.syscom.fep.server.common.adapter;

import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.base.vo.LogData;
import com.syscom.fep.configuration.RMConfig;
import org.slf4j.event.Level;

import java.util.Dictionary;
import java.util.List;

public class AMLAdapter extends AdapterBase{

    private final String programName = AMLAdapter.class.getSimpleName();;

    private AMLRequest request;
    private AMLResponse response;
    public AMLRequest getRequest() { return request; }
    public void setRequest(AMLRequest request) { this.request = request; }
    public AMLResponse getResponse() { return response; }
    public void setResponse(AMLResponse response) { this.response = response; }

    @Override
    public FEPReturnCode sendReceive() {
        FEPReturnCode rtnCode = CommonReturnCode.CBSResponseError;
        LogData logData = new LogData();
        logData.setProgramFlowType(ProgramFlow.AdapterIn);
        logData.setProgramName(programName+"SendReceive");
        logData.setMessageFlowType(MessageFlow.Request);
        logData.setEj(ej);

        String serviceUrl = RMConfig.getInstance().getAMLServiceUrl();
        //var ws = new KYCService01PortTypeClient();
        AMLRequest.LoginData li = new AMLRequest.LoginData();
        li.setUserName(request.getUserName());
        li.setScode(request.getsCode());
        li.setType("TWTS");

        AMLRequest.Person[] persons = new AMLRequest.Person[request.getPersons().size()];
        for (int i = 0; i < persons.length; i++) {
            persons[i] = new AMLRequest.Person();
            persons[i].setPersonID(request.getPersons().get(i).getPersonID());
            persons[i].setLastName(request.getPersons().get(i).getLastName());
            persons[i].setZipCode(request.getPersons().get(i).getZipCode());
//         todo   if (request.getPersons().get(i).getCustomFields()!=null){
//                Dictionary<String, String> dictionaries =  new Hashtable(request.getPersons().get(i).getCustomFields().size());
//                int j = 0;
//                for (int k = 0; k <request.getPersons().get(i).getCustomFields().size() ; k++) {
//                    persons[i].setCustomFields(request.getPersons().get(i).getCustomFields());
//
//                }
//            }
        }
        logData.setMessage(String.format("serviceUrl:{0},userName:{1},pass:{2}, personData:{3}",
                    serviceUrl, li.getUserName(), li.getScode(),
                    serializeToXml(persons)));
        logData.setRemark("AMLAdapter prepare data");
        logMessage(Level.INFO,logData);
        return rtnCode;
    }
    public static class AMLResponse {
        private String errorText;
        private String exceptionText;
        private Integer returnCode;
        private ResultData[] resultData;

        public String getErrorText() {
            return errorText;
        }

        public void setErrorText(String errorText) {
            this.errorText = errorText;
        }

        public String getExceptionText() {
            return exceptionText;
        }

        public void setExceptionText(String exceptionText) {
            this.exceptionText = exceptionText;
        }

        public Integer getReturnCode() {
            return returnCode;
        }

        public void setReturnCode(Integer returnCode) {
            this.returnCode = returnCode;
        }

        public ResultData[] getResultData() {
//            return resultData;
            return resultData != null ? resultData.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】
        }

        public void setResultData(ResultData[] resultData) {
//            this.resultData = resultData;
            this.resultData = resultData != null ? resultData.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】
        }

        public static class ResultData {
            private kycAction[] kycActions;
            private String custNo;
            private kycRiskData kycRisk;

            public kycAction[] getKycActions() {
//                return kycActions;
                return kycActions != null ? kycActions.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】
            }

            public void setKycActions(kycAction[] kycActions) {
//                this.kycActions = kycActions;
                this.kycActions = kycActions != null ? kycActions.clone() : null; //2025-02-03 modified for 【Public Data Assigned to Private Array】
            }
            public String getCustNo() {
                return custNo;
            }
            public void setCustNo(String custNo) {
                this.custNo = custNo;
            }
            public kycRiskData getKycRisk() {
                return kycRisk;
            }
            public void setKycRisk(kycRiskData kycRisk) {
                this.kycRisk = kycRisk;
            }
        }
        public static class kycAction {
            private String longText;
            private Integer rang;
            private Boolean rangSpecified;
            private String shortText;

            public String getLongText() {
                return longText;
            }
            public void setLongText(String longText) {
                this.longText = longText;
            }
            public Integer getRang() {
                return rang;
            }
            public void setRang(Integer rang) {
                this.rang = rang;
            }
            public Boolean getRangSpecified() {
                return rangSpecified;
            }
            public void setRangSpecified(Boolean rangSpecified) {
                this.rangSpecified = rangSpecified;
            }
            public String getShortText() {
                return shortText;
            }
            public void setShortText(String shortText) {
                this.shortText = shortText;
            }
        }
        public static class kycRiskData {
            private Integer finalRisk;
            private String finalRiskLongText;
            private String finalRiskShortText;
            private Boolean finalRiskSpecified;
            private Integer inheritedRisk;
            private String inheritedRiskLongText;
            private String inheritedRiskShortText;
            private Boolean inheritedRiskSpecified;
            private Integer manualRisk;
            private String manualRiskLongText;
            private String manualRiskShortText;
            private Boolean manualRiskSpecified;
            private Integer maschineRisk;
            private String maschineRiskLongText;
            private String maschineRiskShortText;
            private Boolean maschineRiskSpecified;
            public Integer getFinalRisk() {
                return finalRisk;
            }
            public void setFinalRisk(Integer finalRisk) {
                this.finalRisk = finalRisk;
            }
            public String getFinalRiskLongText() {
                return finalRiskLongText;
            }
            public void setFinalRiskLongText(String finalRiskLongText) {
                this.finalRiskLongText = finalRiskLongText;
            }
            public String getFinalRiskShortText() {
                return finalRiskShortText;
            }
            public void setFinalRiskShortText(String finalRiskShortText) {
                this.finalRiskShortText = finalRiskShortText;
            }
            public Boolean getFinalRiskSpecified() {
                return finalRiskSpecified;
            }
            public void setFinalRiskSpecified(Boolean finalRiskSpecified) {
                this.finalRiskSpecified = finalRiskSpecified;
            }
            public Integer getInheritedRisk() {
                return inheritedRisk;
            }
            public void setInheritedRisk(Integer inheritedRisk) {
                this.inheritedRisk = inheritedRisk;
            }
            public String getInheritedRiskLongText() {
                return inheritedRiskLongText;
            }
            public void setInheritedRiskLongText(String inheritedRiskLongText) {
                this.inheritedRiskLongText = inheritedRiskLongText;
            }
            public String getInheritedRiskShortText() {
                return inheritedRiskShortText;
            }
            public void setInheritedRiskShortText(String inheritedRiskShortText) {
                this.inheritedRiskShortText = inheritedRiskShortText;
            }
            public Boolean getInheritedRiskSpecified() {
                return inheritedRiskSpecified;
            }
            public void setInheritedRiskSpecified(Boolean inheritedRiskSpecified) {
                this.inheritedRiskSpecified = inheritedRiskSpecified;
            }
            public Integer getManualRisk() {
                return manualRisk;
            }
            public void setManualRisk(Integer manualRisk) {
                this.manualRisk = manualRisk;
            }

            public String getManualRiskLongText() {
                return manualRiskLongText;
            }

            public void setManualRiskLongText(String manualRiskLongText) {
                this.manualRiskLongText = manualRiskLongText;
            }

            public String getManualRiskShortText() {
                return manualRiskShortText;
            }

            public void setManualRiskShortText(String manualRiskShortText) {
                this.manualRiskShortText = manualRiskShortText;
            }

            public Boolean getManualRiskSpecified() {
                return manualRiskSpecified;
            }

            public void setManualRiskSpecified(Boolean manualRiskSpecified) {
                this.manualRiskSpecified = manualRiskSpecified;
            }

            public Integer getMaschineRisk() {
                return maschineRisk;
            }

            public void setMaschineRisk(Integer maschineRisk) {
                this.maschineRisk = maschineRisk;
            }

            public String getMaschineRiskLongText() {
                return maschineRiskLongText;
            }

            public void setMaschineRiskLongText(String maschineRiskLongText) {
                this.maschineRiskLongText = maschineRiskLongText;
            }

            public String getMaschineRiskShortText() {
                return maschineRiskShortText;
            }

            public void setMaschineRiskShortText(String maschineRiskShortText) {
                this.maschineRiskShortText = maschineRiskShortText;
            }

            public Boolean getMaschineRiskSpecified() {
                return maschineRiskSpecified;
            }

            public void setMaschineRiskSpecified(Boolean maschineRiskSpecified) {
                this.maschineRiskSpecified = maschineRiskSpecified;
            }
        }
    }

    public static class AMLRequest {
        private String sCode;
        private String type;
        private String userName;
        private List<Person> persons;

        public String getsCode() {
            return sCode;
        }

        public void setsCode(String sCode) {
            this.sCode = sCode;
        }

        public String getType() {
            return type;
        }

        public void setType(String type) {
            this.type = type;
        }

        public String getUserName() {
            return userName;
        }

        public void setUserName(String userName) {
            this.userName = userName;
        }

        public List<Person> getPersons() {
            return persons;
        }

        public void setPersons(List<Person> persons) {
            this.persons = persons;
        }


        public static class Person {
            private String personID;
            private String lastName;
            private String zipCode;
            private Dictionary<String, String> customFields;

            public String getPersonID() {
                return personID;
            }

            public void setPersonID(String personID) {
                this.personID = personID;
            }

            public String getLastName() {
                return lastName;
            }

            public void setLastName(String lastName) {
                this.lastName = lastName;
            }

            public String getZipCode() {
                return zipCode;
            }

            public void setZipCode(String zipCode) {
                this.zipCode = zipCode;
            }

            public Dictionary<String, String> getCustomFields() {
                return customFields;
            }

            public void setCustomFields(Dictionary<String, String> customFields) {
                this.customFields = customFields;
            }

        }

        public static class LoginData {
            private String userName;
            private String Scode;
            private String type;

            public String getUserName() {
                return userName;
            }

            public void setUserName(String userName) {
                this.userName = userName;
            }

            public String getScode() {
                return Scode;
            }

            public void setScode(String scode) {
                this.Scode = scode;
            }

            public String getType() {
                return type;
            }

            public void setType(String type) {
                this.type = type;
            }
        }
    }

}
