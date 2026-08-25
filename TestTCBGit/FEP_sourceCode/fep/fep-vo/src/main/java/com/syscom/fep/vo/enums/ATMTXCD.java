package com.syscom.fep.vo.enums;

import org.apache.commons.lang3.StringUtils;

/**
 * ATM的交易代號
 * 編號原則:
 * 1-10:控制類交易
 * 11-30:裝鈔及結帳類
 * 31-:交易類
 */
public enum ATMTXCD {
	Unknown(0),
	RQK(1),
	R3K(2),
	OEX(3),
	AEX(4),
	DEX(5),
	TMO(6),
	WEX(7),
	CEX(8),
	SNS(9),
	SMS(10),

	RWT(11),
	RWF(12),
	RWS(13),
	TTI(14),
	TTF(15),
	TTS(16),
	RRT(17),
	RRL(18),
	CSH(19),
	BAK(20),
	BOX(21),
	BOK(22),
	COH(23),
	COX(24),
	TTC(25),

	IIQ(31),
	IQ2(32),
	IFE(33),
	IFW(34),
	IFT(35),
	PNP(36),
	IFC(37),
	FWF(38),
	IFF(39),
	CWF(40),
	IWF(41),
	IWD(42),
	IPY(43),
	IPA(44),
	EFT(45),
	IDR(46),
	CDR(47),
	GIQ(48),
	AIN(49),
	AAC(50),
	ACW(51),
	ATF(52),
	BFT(53),
	CAV(54),
	CAM(55),
	CAA(56),
	CAJ(57),
	CWV(58),
	CWM(59),
	B05(60),
	B15(61),
	CDF(62),
	CFV(63),
	CFM(64),
	CFA(65),
	CFJ(66),
	PNM(68),
	ACF(69),
	AFF(70),
	BFF(71),
	PNC(72),
	CWD(73),
	FWD(74),
	FAW(75), // 2015/02/13 Modify by Ruling for 此代號改由跨行提領外幣使用
	DEP(76),
	IDP(77),
	TFR(78),
	PAU(79),
	INQ(80),
	APP(81),
	AVB(82),
	PAY(83),
	APY(84),
	FAF(85),
	INF(86),
	DPF(87),
	IDC(88),
	CDX(89),
	TFF(90),
	PAF(91),
	APF(92),
	IAF(93),
	IPF(94),
	EFF(95),
	NDF(96),
	NPF(97),
	CFP(98),
	CFC(99),
	G51(100),
	PN3(101),
	PNB(102),
	IAC(103),
	CUP(104),
	IQC(105),
	CFU(106),
	AP1(107),
	PN0(108),
	PNX(109),
	ANB(110),
	IQV(111),
	IQM(112),
	INM(113),
	INI(114),
	BDR(115),
	ICR(116),
	CCR(117),
	BDF(118),
	ICW(119),
	BCD(120),
	CPN(121), // 2014/11/14 Modify by Ruling for ATM優惠Coupon兌換券統計
	FAC(122), // 2015/02/13 Modify by Ruling for 跨行提領外幣
	FAE(123), // 2015/02/13 Modify by Ruling for 跨行提領外幣

	INA(124), // Fly 2015/09/04 EMV 拒絕磁條卡交易

	EFU(125),
	EUP(126),
	EQC(127),
	EFP(128),
	EWV(129),
	EQP(130),
	EFC(131),
	EWM(132),
	EQU(133),
	EAM(134),
	EAV(135),
	EFM(136),
	EFV(137),

	G50(138), // 2016/06/15 Modify by Ruling for COMBO開卡作業優化
	CHK(139), // 2016/06/22 Modify by Ruling for 繳費網PHASE2
	MAK(140), // 2016/06/22 Modify by Ruling for 繳費網PHASE2
	ODE(141), // 2016/07/15 Modify by Ruling for ATM新功能-跨行存款
	ODR(142), // 2016/07/15 Modify by Ruling for ATM新功能-跨行存款
	ODF(143), // 2016/07/15 Modify by Ruling for ATM新功能-跨行存款
	ODT(144), // 2016/07/15 Modify by Ruling for ATM新功能-晶片卡轉入交易(跨行存款)
	OFT(145), // 2016/07/15 Modify by Ruling for ATM新功能-繳汽燃費
	OFF(146), // 2016/07/15 Modify by Ruling for ATM新功能-繳汽燃費
	DDR(147), // 2016/07/15 Modify by Ruling for ATM新功能-現金捐款
	DDF(148), // 2016/07/15 Modify by Ruling for ATM新功能-現金捐款
	NCS(149), // 2017/01/26 Modify by Ruling for 無卡提款
	NWD(150), // 2017/01/26 Modify by Ruling for 無卡提款
	NWF(151), // 2017/01/26 Modify by Ruling for 無卡提款
	NWQ(152), // 2017/02/24 Modify by Fly for 無卡提款
	NWS(153),
	NWC(154),
	NWR(155),
	CFT(156), // 2018/10/18 Modify by Ruling for OKI硬幣機功能
	CCF(157),
	CHG(158),
	NFE(159), // 2018/10/18 Modify by Ruling for 外幣無卡提款
	NFW(160),
	NFF(161),
	VAA(162), // 2018/10/18 Modify by Fly for 2566約定及核驗服務類別10
	NWA(163), // 2019/01/29 Modify by Ruling for 企業戶無卡提款
	ENC(164), // Fly 2019/02/12 繳費網信用卡資訊加密需求
	TRE(165),
	API(166), // 2019/03/26 Modify by Ruling for 豐錢包APP綁定本行帳號安控機制
	IQQ(167), // 2019/05/02 Modify by Ruling for OKI硬幣機功能第二階段
	PIQ(168), // 2020/09/18 Modify by Ruling for ATM提升分行離櫃率_全自助：掌靜脈
	PAC(169),
	PWD(170),
	PWF(171),
	PFT(172),
	PFF(173),
	PDR(174),
	PDF(175),
	PCR(176),
	PCF(177),
	MTQ(178), //2020/12/30 Modify by Ruling for 手機門號跨行轉帳(第二階段) han add
	US(179),
	JP(180),
	WF(181),	//指靜脈提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	FV(182),	//指靜脈建置	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	AW(183),	//無卡提款申請	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	DA(184),	//跨行存款交易 ICMARK不轉ASCII 20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	DC(185),	//跨行存款交易 ICMARK不轉ASCII 20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	T5(186),	//15類，自繳稅	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	T7(187),	//15類，自繳稅	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	T6(188),	//非15類，核定稅	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	T8(189),	//非15類，核定稅	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	T(190),		//轉帳交易		20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	E(191),		//全國性繳費3	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	SW(192),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	PC(193),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	VD(194),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	CC(195),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	MC(196),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	JC(197),	//他行信用卡預借現金/國際卡提款/銀聯卡、PLUS提款	20221004 配合SPEC新增 FEP10-000-SPC_ATM_PrepareFEPTxn(收到ATM電文寫入 FEPTXN)
	W2(198),	//合庫無卡提款
	TD(199),	//跨行轉帳小額交易手續費
	T4(200),	//跨行轉帳小額交易手續費
	TW(201),	//跨行轉帳小額交易手續費
	TA(202),	//跨行轉帳小額交易手續費
	TR(203),	//跨行轉帳小額交易手續費
	D8(204),	//企業存款
	I5(205),	//企業存款
	I4(206),	//自行帳號檢核
	;
	
	private int code;

	private ATMTXCD(int code) {
		this.code = code;
	}

	public int getCode() {
		return code;
	}

	public static ATMTXCD fromCode(int code) {
		for (ATMTXCD e : values()) {
			if (e.getCode() == code) {
				return e;
			}
		}
		throw new IllegalArgumentException("Invalid code = [" + code + "]!!!");
	}

	public static ATMTXCD parse(Object nameOrCode) {
		if (nameOrCode instanceof Number) {
			return fromCode(((Number) nameOrCode).intValue());
		} else if (nameOrCode instanceof String) {
			String nameOrCodeStr = (String) nameOrCode;
			if (StringUtils.isNumeric(nameOrCodeStr)) {
				return fromCode(Integer.parseInt(nameOrCodeStr));
			}
			for (ATMTXCD e : values()) {
				if (e.name().equalsIgnoreCase(nameOrCodeStr)) {
					return e;
				}
			}
		}
		throw new IllegalArgumentException("Invalid name or code = [" + nameOrCode + "]!!!");
	}
}
