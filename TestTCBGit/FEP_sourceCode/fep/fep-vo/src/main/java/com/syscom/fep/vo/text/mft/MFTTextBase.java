package com.syscom.fep.vo.text.mft;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.parse.MftStringToFieldAnnotationParser;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.TextBase;

public abstract class MFTTextBase extends TextBase<MFTGeneral> {

	public abstract MFTGeneral parseFlatfile(String flatfile) throws Exception;

	protected <T extends MFTTextBase> MFTGeneral parseFlatfile(Class<T> genericType, String flatfile) throws Exception {
		if (flatfile.length() < this.getTotalLength()) {
			flatfile = StringUtils.rightPad(flatfile, this.getTotalLength(), StringUtil.toHex(" "));
		}
		T request = new MftStringToFieldAnnotationParser<T>(genericType).readIn(flatfile);
		MFTGeneral general = new MFTGeneral();
		request.toGeneral(general);
		return general;
	}
}
