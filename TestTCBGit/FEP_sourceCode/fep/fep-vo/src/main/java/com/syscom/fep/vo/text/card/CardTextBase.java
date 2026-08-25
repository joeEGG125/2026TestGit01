package com.syscom.fep.vo.text.card;

import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.frmcommon.parse.StringToFieldAnnotationParser;
import com.syscom.fep.frmcommon.util.StringUtil;
import com.syscom.fep.vo.text.TextBase;

public abstract class CardTextBase extends TextBase<CardGeneral> {
	
	public abstract CardGeneral parseFlatfile(String flatfile) throws Exception;

	protected <T extends CardTextBase> CardGeneral parseFlatfile(Class<T> genericType, String flatfile) throws Exception {
		// 位數不夠右邊自動補齊16進制的空白符
		if (flatfile.length() < this.getTotalLength() * 2) {
			flatfile = StringUtils.rightPad(flatfile, this.getTotalLength() * 2, StringUtil.toHex(" "));
		}
		T request = new StringToFieldAnnotationParser<T>(genericType).readIn(flatfile);
		CardGeneral general = new CardGeneral();
		request.toGeneral(general);
		return general;
	}
}
