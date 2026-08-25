package com.syscom.fep.server.aa.inbk;

import com.syscom.fep.base.aa.MFTData;
import com.syscom.fep.base.cnst.rcode.CommonReturnCode;
import com.syscom.fep.base.enums.FEPReturnCode;
import com.syscom.fep.base.enums.MessageFlow;
import com.syscom.fep.base.enums.ProgramFlow;
import com.syscom.fep.batch.base.library.BatchJobLibrary;
import com.syscom.fep.common.util.DbHelper;
import com.syscom.fep.configuration.SysStatus;
import com.syscom.fep.frmcommon.util.SpringBeanFactoryUtil;
import com.syscom.fep.mybatis.dao.FeptxnDao;
import com.syscom.fep.mybatis.ext.mapper.BatchExtMapper;
import com.syscom.fep.mybatis.ext.mapper.TaskExtMapper;
import com.syscom.fep.mybatis.ext.mapper.ZoneExtMapper;
import com.syscom.fep.mybatis.model.Batch;
import com.syscom.fep.mybatis.model.Task;
import com.syscom.fep.mybatis.model.Zone;
import com.syscom.fep.vo.enums.IOReturnCode;
import com.syscom.fep.vo.text.mft.MFTGeneralRequest;
import org.apache.commons.lang3.StringUtils;
import org.slf4j.event.Level;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 處理主機通知FEP電文
 */
public class MFTNotice extends INBKAABase {
	private String MSGID;
	private MFTGeneralRequest mftGeneralRequest = getFiscBusiness().getMftData().getTxMFTObject().getRequest();

	private FEPReturnCode _rtnCode = CommonReturnCode.Normal;
	private ZoneExtMapper zoneExtMapper = SpringBeanFactoryUtil.getBean(ZoneExtMapper.class);
	private BatchExtMapper batchExtMapper = SpringBeanFactoryUtil.getBean(BatchExtMapper.class);
	private TaskExtMapper taskExtMapper = SpringBeanFactoryUtil.getBean(TaskExtMapper.class);

	private FeptxnDao oriDBFEPTXN = SpringBeanFactoryUtil.getBean("feptxnDao");
	private boolean isEC = false;

	public MFTNotice(MFTData txnData) throws Exception {
		super(txnData);
	}

	/**
	 * <summary>
	 * ''' 程式進入點
	 * ''' </summary>
	 * ''' <returns>Response電文</returns>
	 * ''' <remarks></remarks>
	 */
	@Override
	public String processRequestData() {
		Zone zone =zoneExtMapper.selectByPrimaryKey("TWN");
		BatchJobLibrary batchLib = new BatchJobLibrary();
		String batchName="";
		List<Batch> dt;
		List<Task> list;
		Task task = new Task();
		Batch batch = new Batch();
		String fileName= "";
		Map<String, String> arguments = new HashMap<>();
		try {
			MSGID = mftGeneralRequest.getTRAN_CODE()+mftGeneralRequest.getTD_CODE();
			switch (MSGID){
				case "IBAF5102":  /* 財金結帳通知(5102) */
					if(zone.getZoneTbsdy().compareTo(SysStatus.getPropertyValue().getSysstatTbsdyFisc()) <0
							&& DbHelper.toBoolean(zone.getZoneChgday())){
						_rtnCode = getFiscBusiness().ChangeCBSDate(zone);
					}
					break;
				case "IPUFRCV1":  /* 全國繳費整批轉即時檔案 */
					batchName="NPSBatchInOnlineOut";
					dt = batchExtMapper.queryBatchByName(batchName);
					list = taskExtMapper.getTaskByName("NPSBatchInOnlineOut","ASC");
					task.setTaskId(list.get(0).getTaskId());
					fileName= mftGeneralRequest.getFILENAME();

					task.setTaskCommandargs("/FILEID:"+fileName);
					task.setTaskName(null);
					task.setTaskCommand(null);
					taskExtMapper.updateByPrimaryKeySelective(task);
					arguments.put("FILEID",fileName);
					batchLib.setArguments(arguments);
					batchLib.startBatch(
							dt.get(0).getBatchExecuteHostName(),
							dt.get(0).getBatchBatchid().toString(),
							dt.get(0).getBatchStartjobid().toString());
					break;
				case "IBAFRCV1":
					switch (mftGeneralRequest.getTYPE()){
						case "2":
							batchName="ImportNPSUNIT";
							dt = batchExtMapper.queryBatchByName(batchName);
							list = taskExtMapper.getTaskByName("ImportNPSUNIT","ASC");
							task.setTaskId(list.get(0).getTaskId());
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(null);
							task.setTaskCommand(null);
							taskExtMapper.updateByPrimaryKeySelective(task);
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							batchLib.startBatch(
									dt.get(0).getBatchExecuteHostName(),
									dt.get(0).getBatchBatchid().toString(),
									dt.get(0).getBatchStartjobid().toString());
							break;
						case "3":
							batchName="ImportMERCHANT";
							dt = batchExtMapper.queryBatchByName(batchName);
							list = taskExtMapper.getTaskByName("ImportMERCHANT","ASC");
							task.setTaskId(list.get(0).getTaskId());
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(null);
							task.setTaskCommand(null);
							taskExtMapper.updateByPrimaryKeySelective(task);
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							batchLib.startBatch(
									dt.get(0).getBatchExecuteHostName(),
									dt.get(0).getBatchBatchid().toString(),
									dt.get(0).getBatchStartjobid().toString());
							break;
						case "4":
							batchName="ImportUPBIN";
							task.setTaskName(batchName);
							task = taskExtMapper.selectTaskforName(task);
							fileName= mftGeneralRequest.getFILENAME().trim();

							task.setTaskCommandargs("/FILEID:"+fileName);
							task.setTaskName(batchName);
							task.setTaskCommand("com.syscom.fep.batch.task.inbk.ImportUPBIN");
							int iRes = taskExtMapper.updateByPrimaryKeySelective(task);
							if (iRes != 1) {
								logContext.setTableDescription(task.getTaskDescription());
								logContext.setReturnCode(IOReturnCode.UpdateFail);
								logContext.setRemark("更新TASK:ImportNPSUNIT的參數失敗");
								logMessage(logContext);
								break;
							}
							arguments.put("FILEID",fileName);
							batchLib.setArguments(arguments);
							Batch record = batchExtMapper.getSingleBATCHByDef(batchName);
							if (record == null) {
								logContext.setRemark("查詢不到名稱為" + record.getBatchName() + "的資料列 FROM BATCH Table");
								logContext.setReturnCode(IOReturnCode.QueryNoData);
								logMessage(Level.WARN, logContext);
								break;
							}
							logContext.setRemark("MFTNotice-Start Call BTOutBatch, BATCH_NAME="
									+ batch.getBatchName() + ",TASK_COMMANDARGS=" + task.getTaskCommandargs());
							logContext.setProgramFlowType(ProgramFlow.None);
							logContext.setMessageFlowType(MessageFlow.Request);
							logMessage(Level.DEBUG, logContext);
							logContext.setRemark("MFTNotice record, BATCH_NAME="
									+ record.getBatchName() + ",Batch_Batchid=" + record.getBatchBatchid().toString()
							        + ",Batch_Startjobid="+ record.getBatchStartjobid().toString());
							logContext.setProgramFlowType(ProgramFlow.None);
							logContext.setMessageFlowType(MessageFlow.Request);
							logMessage(Level.DEBUG, logContext);
							batchLib.startBatch(
									record.getBatchExecuteHostName(),
									record.getBatchBatchid().toString(),
									record.getBatchStartjobid().toString());
							break;
					}
			}
		} catch (Exception ex) {
			_rtnCode = CommonReturnCode.ProgramException;
			getLogContext().setProgramException(ex);
			getLogContext().setProgramName(StringUtils.join(ProgramName, ".processRequestData"));
			sendEMS(getLogContext());
		} finally {
			getTxData().getLogContext().setProgramFlowType(ProgramFlow.AAOut);
			getTxData().getLogContext().setMessage(getFiscRes().getFISCMessage());
			getTxData().getLogContext().setProgramName(this.aaName);
			getTxData().getLogContext().setMessageFlowType(MessageFlow.Response);
			logMessage(Level.DEBUG, getLogContext());
		}
		return "";
	}
}
