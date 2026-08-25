package com.syscom.fep.base.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * 寫Log時所記錄的程式位置
 * 
 * @author Richard
 *
 */
public enum ProgramFlow {
	None(0),
	ATMGatewayIn(1),
	ATMGatewayOut(2),
	FISCGatewayIn(3),
	FISCGatewayOut(4),
	MsgHandlerIn(5),
	MsgHandlerOut(6),
	AAIn(7),
	AAOut(8),
	AdapterIn(9),
	AdapterOut(10),
	ENCIn(11),
	ENCOut(12),
	AAServiceIn(13),
	AAServiceOut(14),
	UnisysGWIn(15),
	UnisysGWOut(16),
	CreditGWIn(17),
	CreditGWOut(18),
	MsgIn1411(19),
	MsgOut1411(20),
	UnisysIn(21),
	UnisysOut(22),
	FCSServiceIn(23),
	FCSServiceOut(24),
	FCSGWIn(25),
	FCSGWOut(26),
	T24GWIn(27),
	T24GWOut(28),
	ChannelGWIn(29),
	ChannelGWOut(30),
	LateResServIn(31),
	LateResServOut(32),
	LateResAAIn(33),
	LateResAAOut(34),
	SVCSGWIn(35),
	SVCSGWOut(36),
	WCFServiceIn(37),
	WCFServiceOut(38),
	RESTFulIn(39),
	RESTFulOut(40),
	CBSGatewayIn(41),
	CBSGatewayOut(42),
	SocketIn(43),
	SocketOut(44),
	MFTGWIn(45),
	MFTGWOut(46),
	MFTHandlerIn(47),
	MFTHandlerOut(48),
	TWMPGWIn(49),
	TWMPGWOut(50),
	TWMPHandlerIn(51),
	TWMPHandlerOut(52),

	CBSIn(99),
	CBSOut(100),
	CBSAscii(101),
	CBSTimeOutRerunIn(102),
	CBSTimeOutRerunOut(103),
	IMSGWIn(104),
	IMSGWOut(105),
	PosGateWayIn(106),
	PosGateWayOut(107);

	private int value;

	private ProgramFlow(int value) {
		this.value = value;
	}

	public int getValue() {
		return value;
	}

	public static ProgramFlow fromValue(int value) {
		for (ProgramFlow e : values()) {
			if (e.getValue() == value) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid value = [" + value + "]!!!");
	}

	public static ProgramFlow parse(Object nameOrValue) {
		if (nameOrValue instanceof Number) {
			return fromValue(((Number) nameOrValue).intValue());
		} else if (nameOrValue instanceof String) {
			String nameOrValueStr = (String) nameOrValue;
			if (StringUtils.isNumeric(nameOrValueStr)) {
				return fromValue(Integer.parseInt(nameOrValueStr));
			}
			for (ProgramFlow e : values()) {
				if (e.name().equalsIgnoreCase(nameOrValueStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or value = [" + nameOrValue + "]!!!");
	}
}