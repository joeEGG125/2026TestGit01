package com.syscom.fep.vo.text.atm.request;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class AccInfoRequest {
	private String IdNo;
	private String QueryType;
	private String Account;
	private String BankCode;

	@Override
	public String toString() {
		return "{" +
				", IdNo='" + IdNo + '\'' +
				", QueryType='" + QueryType + '\'' +
				", Account='" + Account + '\'' +
				", BankCode='" + BankCode + '\'' +
				'}';
	}
}
