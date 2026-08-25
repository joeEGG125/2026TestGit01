package com.syscom.fep.common.converter;

import java.io.BufferedReader;
import java.io.ByteArrayInputStream;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.HashMap;
import java.util.Map;
import java.util.StringTokenizer;

import org.apache.commons.codec.binary.Hex;
import org.apache.commons.lang3.StringUtils;

import com.syscom.fep.common.log.LogHelperFactory;
import com.syscom.fep.common.util.PolyfillUtil;
import com.syscom.fep.frmcommon.log.LogHelper;
import com.syscom.fep.frmcommon.util.ConvertUtil;
import com.syscom.fep.frmcommon.util.ExceptionUtil;

public class SyscomConverter {
	private static final LogHelper TRACELogger = LogHelperFactory.getTraceLogger();

	private SyscomConverter() {}

	private static final Map<String, String> hSinopacToCNS11643 = new HashMap<>();
	private static final Map<String, String> hCns11643ToSinopac = new HashMap<>();
	private static final Map<String, String> hCns11643ToUnicode = new HashMap<>();
	private static final Map<String, String> hUnicodeToCns11643 = new HashMap<>();
	private static final Map<String, String> hUnisysToUnicode = new HashMap<>();
	private static final Map<String, String> hUnicodeToUnisys = new HashMap<>();
	private static final Map<String, String> hUnicodeToSinopac = new HashMap<>();
	private static final String[] controlCode = new String[] { "0F", "0E", "1B6E", "1B6F", "1B4E", "1B4F", "1B242B" };
	private static final Map<Integer, String> WordPage = new HashMap<Integer, String>();
	private static final Map<String, Integer> WordPageByCode = new HashMap<String, Integer>();
	private static final String AlternateCnsCode = "01-2177";
	private static final String AlternateUnicodeCode = "0626";
	private static final String AlternateBigCode = "A1B8";
	private static String codeMap = "codemap.txt";

	static {
		TRACELogger.trace(SyscomConverter.class.getSimpleName(), " codemap file path " + codeMap);
		loadCNSUnicodeTable();
		WordPage.put(1, "30");
		WordPage.put(2, "31");
		WordPage.put(3, "32");
		WordPage.put(4, "33");
		WordPage.put(5, "34");
		WordPage.put(6, "35");
		WordPage.put(7, "36");
		WordPage.put(8, "37");
		WordPage.put(9, "38");
		WordPage.put(10, "39");
		WordPage.put(11, "3A");
		WordPage.put(12, "3B");
		WordPage.put(13, "3C");
		WordPage.put(14, "3D");
		WordPage.put(15, "3E");
		WordPage.put(0x10, "3F");
		WordPage.forEach((key, value) -> {
			WordPageByCode.put(value, key);
		});
	}


	private static String cns11643ToOthers(byte[] cnsByte, EncodingType targetEncoding) {
		int intIndex = 0;
		byte cnsWordPage = 0; //目前字面
//		byte changeWordPage = 0; //轉換之指定字面
		byte currentWordPage = 0; //非鎖定轉換前字面
		byte bt = 0;
		byte btTemp = 0;
		byte[] bts = new byte[1];
		ByteArrayInputStream stream = new ByteArrayInputStream(cnsByte);
		StringBuilder sb = new StringBuilder();
		String str = null;
		boolean changePage = false;
		Map<String, String> mapTable = null;

		if (targetEncoding == EncodingType.Unicode) {
			mapTable = hCns11643ToUnicode;
		} else if (targetEncoding == EncodingType.BSP) {
			mapTable = hCns11643ToSinopac;
		}
		stream.mark(0);
		int cnsByteLength = stream.available();
		int tempVar = (cnsByteLength - 1);
		for (intIndex = 0; intIndex <= tempVar; intIndex++) {
			bt = (byte)stream.read();
			if (bt >= 0x0 && bt <= 0x20) {//, &H70
				if (bt == 0xA) {
					sb.append('\n');
				} else if (bt == 0xD) {
					sb.append('\r');
				} else if (bt == 0xE) {
					bts = new byte[2];
					cnsWordPage = 0x1;
					currentWordPage = 0x1;
				} else if (bt == 0xF) {
					bts = new byte[1];
					cnsWordPage = 0x1;
					currentWordPage = 0x1;
				} else if (bt == 0x1B) {
					intIndex += 1;
					bts = new byte[2];
					btTemp = (byte)stream.read();
					if (btTemp == 0x24) {
						intIndex += 2;
						stream.read(bts, 0, 2);
						switch (bts[0]) {
							case 0x29:
								if (bts[1] == 0x47) {
									cnsWordPage = 0x1;
								}
								break;
							case 0x2A:
								if (bts[1] == 0x48) {
									cnsWordPage = 0x2;
								}
								break;
							case 0x2B:
								switch (bts[1]) {
									case 0x30:
										cnsWordPage = 1;
										break;
									case 0x31:
										cnsWordPage = 2;
										break;
									case 0x32:
										cnsWordPage = 3;
										break;
									case 0x33:
										cnsWordPage = 4;
										break;
									case 0x34:
										cnsWordPage = 5;
										break;
									case 0x35:
										cnsWordPage = 6;
										break;
									case 0x36:
										cnsWordPage = 7;
										break;
									case 0x37:
										cnsWordPage = 8;
										break;
									case 0x38:
										cnsWordPage = 9;
										break;
									case 0x39:
										cnsWordPage = 10;
										break;
									case 0x3A:
										cnsWordPage = 11;
										break;
									case 0x3B:
										cnsWordPage = 12;
										break;
									case 0x3C:
										cnsWordPage = 13;
										break;
									case 0x3D:
										cnsWordPage = 14;
										break;
									case 0x3E:
										cnsWordPage = 15;
										break;
									case 0x3F:
										cnsWordPage = 16;
										break;
								}
								break;
							default:
								break;
						}
					} else if (btTemp == 0x4E) {//SINGLE-SHIFT TWO
						changePage = true;
//						changeWordPage = 2;
						cnsWordPage = 2;
					} else if (btTemp == 0x4F) {//SINGLE-SHIFT THREE
						changePage = true;
//						changeWordPage = 3;
						//cnsWordPage = 3
					} else if (btTemp == 0x6E) {//LOCKING-SHIFT TWO
						changePage = false;
						cnsWordPage = 2;
						currentWordPage = 2;
					} else if (btTemp == 0x6F) {//LOCKING-SHIFT THREE
						changePage = false;
						currentWordPage = 3;
						//cnsWordPage = 3
					}
				} else {
					if (targetEncoding == EncodingType.Unicode || targetEncoding == EncodingType.BSP) {
						if (bt < 32) {
							sb.append(AlternateUnicodeCode);
						} else {
							sb.append(Integer.toHexString(bt).toUpperCase() + "00");
						}
					} else {
						sb.append(bt);
					}
				}
			} else {
				bts[0] = bt;
				switch (bts.length) {
					case 1:
						if (targetEncoding == EncodingType.Unicode || targetEncoding == EncodingType.BSP) {
							sb.append(Integer.toHexString(bt).toUpperCase() + "00");
						} else {
							sb.append(Integer.toHexString(bt).toUpperCase());
						}

						break;
					case 2:
						if (intIndex + 1 < cnsByteLength) {
							bt = (byte)stream.read();
							bts[1] = bt;
							intIndex += 1;
						} else {
							//如果還沒出現0F前最後一個byte為奇數則捨去
							break;
						}
						str = "";
						for (byte b : bts) {
							str += String.format("%02x", b).toUpperCase();
						}
						if (changePage) {
							//str = changeWordPage.ToString("00") & "-" & str
							str = StringUtils.join(PolyfillUtil.toString(cnsWordPage ,"00") , "-" , str);
							sb.append(getDecodeString(str, mapTable, targetEncoding));
							changePage = false;
							cnsWordPage = currentWordPage;
							//If cnsMap.ContainsKey(str) Then
							//    sb.Append(ChrW(CInt("&H" & CStr(cnsMap(str)))))
							//    changePage = False
							//Else
							//    If changeWordPage = 2 Then
							//        sb.Append("<!--" & str & "-->")
							//    Else
							//        For i As Integer = 4 To 7
							//            If cnsMap.ContainsKey(i & str.Substring(1)) Then
							//                sb.Append(ChrW(CInt("&H" & i & str.Substring(1))))
							//                changePage = False
							//                Exit For
							//            End If
							//        Next
							//    End If
							//End If
							//If changePage Then
							//    sb.Append("<!--" & str & "-->")
							//    changePage = False
							//End If
						} else {
							str = StringUtils.join(PolyfillUtil.toString(cnsWordPage,"00") , "-" , str);
							sb.append(getDecodeString(str, mapTable, targetEncoding));
							//If cnsWordPage > 3 Then cnsWordPage = currentWordPage
							//If cnsMap.ContainsKey(str) Then
							//    sb.Append(ChrW(CInt("&H" & CStr(cnsMap(str)))))
							//Else
							//    sb.Append("<!--" & str & "-->")
							//End If
						}
						break;
				}
			}
		}

		return sb.toString();
	}

	public static byte[] convertToByteArray(EncodingType sourceEncoding, EncodingType targetEncoding, byte[] data) {
		return (sourceEncoding != targetEncoding) ? getBytesFromHexString(convertToHexString(sourceEncoding, targetEncoding, data)) : data;
	}

	public static String convertToHexString(EncodingType sourceEncoding, EncodingType targetEncoding, byte[] data) {
		StringBuilder sb = new StringBuilder();
		if (sourceEncoding != targetEncoding) {
			switch (sourceEncoding) {
				case Big5:
					if (targetEncoding != EncodingType.Unicode) {
						throw ExceptionUtil.createUnsupportedOperationException("targetEncoding = [", targetEncoding, "] is not ", EncodingType.Unicode.name());
					}
					sb.append(getHexStringFromBytes(ConvertUtil.toBytes(data, PolyfillUtil.toCharsetName("950"), PolyfillUtil.toCharsetName("Unicode"))));
					break;
				case Unicode:
					switch (targetEncoding) {
						case Big5:
							sb.append(getHexStringFromBytes(ConvertUtil.toBytes(data, PolyfillUtil.toCharsetName("Unicode"), PolyfillUtil.toCharsetName("950"))));
							break;
						case Cns11643:
							sb.append(getCNSEncodeString(EncodingType.Unicode, hUnicodeToCns11643, ConvertUtil.toHex(data)));
							break;
						case Unisys:
							return getUnicodeToUnisys(data);
						case BSP:
							return getUnicodeToBSP(data);
						default:
							throw ExceptionUtil.createUnsupportedOperationException("targetEncoding = [", targetEncoding, "]");
					}
					break;
				case Cns11643:
					switch (targetEncoding) {
						case Unicode:
						case BSP:
							sb.append(cns11643ToOthers(data, targetEncoding));
							break;
						default:
							throw ExceptionUtil.createUnsupportedOperationException("targetEncoding = [", targetEncoding, "]");
					}
					break;
				case Unisys:
					if (targetEncoding != EncodingType.Unicode) {
						throw ExceptionUtil.createUnsupportedOperationException("targetEncoding = [", targetEncoding, "] is not ", EncodingType.Unicode.name());
					}
					return getUnisysToUnicode(data);
				case BSP:
					if (targetEncoding != EncodingType.Cns11643) {
						throw ExceptionUtil.createUnsupportedOperationException("targetEncoding = [", targetEncoding, "] is not ", EncodingType.Cns11643.name());
					}
					sb.append(getCNSEncodeString(EncodingType.BSP, hSinopacToCNS11643, ConvertUtil.toHex(data)));
					break;
				default:
					break;
			}
			return sb.toString();
		} else {
			return ConvertUtil.toHex(data);
		}
	}

	public static byte[] getBytesFromHexString(String hexString) {
		return ConvertUtil.hexToBytes(hexString);
	}

	private static String getCNSEncodeString(EncodingType sourceEncoding, Map<String, String> mapTable, String sourceHexString) {

		String temp = null;
		String tempNext = null;
		//Dim arCns() As String
		StringBuilder cnsCode = new StringBuilder();
		//Dim j As Integer

		int preLockPage = 0; //前一鎖定字面
		int nextLockPage = 0; //下一鎖定字面
		int page = 0; //目前實際字面
		int curLockPage = 0; //目前判斷鎖定用的字面

		//Dim prePage As Integer      '前一字面
		//ReDim arCns(CInt(sourceHexString.Length / 4 - 1))
		String oTemp = "";
		String cnsTemp = "";
		int code = 0;
		//Dim need0F As Boolean
		for (int i = 0; i < sourceHexString.length(); i += 2) {
			String ledCode = "";
			//判斷是否為single byte
			temp = sourceHexString.substring(i, i + 2);
			if (sourceEncoding == EncodingType.Big5) {
				code = PolyfillUtil.hexToInt(temp);
			} else {
				code = (PolyfillUtil.hexToInt(sourceHexString.substring(i + 2, i + 2 + 2)) << 8) + PolyfillUtil.hexToInt(temp);
			}
			//single byte
			if (code < 128) {
				if (preLockPage != 0) {//需要補0F
					cnsCode.append(StringUtils.join(controlCode[0] , temp));
				} else {
					cnsCode.append(temp);
				}
				preLockPage = 0;
				if (sourceEncoding == EncodingType.Unicode || sourceEncoding == EncodingType.BSP) {
					i += 2;
				}
				continue;
			} else {
				//i += 2
				//If sourceEncoding = EncodingType.Unicode Then
				//    oTemp = sourceHexString.Substring(i + 2, 2) & temp 'Unicode改為BigEndian
				//Else
				oTemp = temp + sourceHexString.substring(i + 2, i + 2 + 2);
				//End If

			}

			cnsTemp = getDecodeString(oTemp, mapTable, EncodingType.Cns11643);
			oTemp = "";
			page = Integer.parseInt(cnsTemp.substring(0, 2)); //抓出目前是第幾字面
			String c = cnsTemp.substring(3);
			curLockPage = page;
			//第3字面以上一律視為第3字面
			//If curLockPage > 2 Then curLockPage = 3

			//目前字面是否與前一鎖定字面相同,如果是就不用鎖定
			if (curLockPage == preLockPage) {
				//如果字面與前一字面完全相同或第一字面則直接取碼
				if (curLockPage == preLockPage || curLockPage == 1) {
					cnsCode.append(c);
				} else {
					cnsCode.append(getLeadingCode(page, false) + c);
				}
				preLockPage = curLockPage;
			} else if (curLockPage == 1 && curLockPage != preLockPage) {//與前一字面不同時,如果是第一字面直接鎖定,不需判斷後一字元
				//ledCode = GetLeadingCode(curLockPage, True)
				ledCode = getLeadingCode(page, true);
				preLockPage = curLockPage;
				cnsCode.append(ledCode + c);
			} else {
				//與前一鎖定字面不同時,判斷後一字元是否為相同鎖定字面,若是則鎖定,反之則不鎖定
				boolean locking = false;
				if (sourceHexString.length() > i + 6) {
					//If sourceEncoding = EncodingType.Big5 Then
					tempNext = getDecodeString(sourceHexString.substring(i + 4, i + 4 + 2) + sourceHexString.substring(i + 6, i + 6 + 2), mapTable, EncodingType.Cns11643);
					//Else
					//    tempNext = GetDecodeString(sourceHexString.Substring(i + 6, 2) + sourceHexString.Substring(i + 4, 2), mapTable, EncodingType.Cns11643)
					//End If

					int nextPage = Integer.parseInt(tempNext.substring(0, 2)); //抓出下一字元字面
					//If nextPage > 2 Then nextLockPage = 3 Else nextLockPage = nextPage '第3字面以後均指定於G3字面
					//If curLockPage = nextLockPage Then
					if (page == nextPage) {
						locking = true;
						ledCode = getLeadingCode(page, locking);
						preLockPage = curLockPage;
					} else {
						locking = false;
						ledCode = getLeadingCode(page, locking);
					}
				} else {//最後一個字元與前字元不是同一鎖定字面,以非鎖定方式處理
					locking = false;
					ledCode = getLeadingCode(page, locking);
				}
				cnsCode.append(ledCode + c);
				if (locking) {//如果為鎖定方式則將目前頁面數記錄下來
					preLockPage = curLockPage;
				}
			}
			i += 2; //中文一律為double Byte
		}

		return cnsCode.toString();
	}

	private static String getDecodeString(String key, Map<String, String> mapTable, EncodingType target) {
		String value = mapTable.get(key);
		if (value != null) {
			return value;
		} else {
			if (target == EncodingType.Big5) {
				return AlternateBigCode;
			} else if (target != EncodingType.Unicode && target != EncodingType.BSP) {
				return AlternateCnsCode;
			} else {
				return AlternateUnicodeCode;
			}
		}
	}

	public static String getHexStringFromBytes(byte[] data) {
		return Hex.encodeHexString(data, false);
	}

	public static String getHexStringFromBytes(byte[] data, int offset, int count) {
		return ConvertUtil.toHex(data, offset, count);
	}

	private static String getLeadingCode(int page, boolean locking) {
		String value = StringUtils.EMPTY;
		if (locking) {
			if (page == 1) {
				value = "0E";
			} else if (page == 2) {
				value = "1B6E";
			} else if (page >= 3) {
				value = "1B6F";
			}
		} else {
			if (page == 1) {
				value = "0E";
			} else if (page == 2) {
				value = "1B4E";
			} else if (page >= 3) {
				value = "1B4F";
			}
		}
		return page >= 3 ? StringUtils.join("1B242B", WordPage.get(page), value) : value;
	}

	public static String getUnicodeCustom(String word) {
		String str = null;
		@SuppressWarnings("unused")
		byte[] bytes = word.getBytes(java.nio.charset.StandardCharsets.UTF_16LE);
		return str;
	}

	private static String getUnicodeToBSP(byte[] data) {
		StringBuilder sb = new StringBuilder();
		int tempVar = data.length;
		for (int i = 0; i < tempVar; i += 2) {
			String key = getHexStringFromBytes(data, i, 2);
			String value = hUnicodeToSinopac.get(key);
			if (value != null) {
				sb.append(value);
			} else {
				sb.append(AlternateUnicodeCode);
			}
		}
		return sb.toString();
	}

	private static String getUnicodeToUnisys(byte[] data) {
		ByteArrayInputStream sm = new ByteArrayInputStream(data);
		StringBuilder sb = new StringBuilder();
		int remaining = data.length;
		String lastEnc = "";
		while (remaining > 0) {
			byte[] unicod = new byte[2];
			int read = sm.read(unicod, 0, 2);
			String key = getHexStringFromBytes(unicod);
			if (unicod[1] == 0) {//ascii
				if (lastEnc.equals("c")) {
					sb.append("07");
				}
				sb.append(key.substring(0, 2));
				lastEnc = "e";
			} else {
				if (StringUtils.isBlank(lastEnc) || "e".equals(lastEnc)) {
					sb.append("04");
				}
				String value = hUnisysToUnicode.get(key);
				if (value != null) {
					sb.append(value);
				} else {
					sb.append(AlternateBigCode);
				}
				lastEnc = "c";
			}
			remaining -= read;
		}
		if (lastEnc.equals("c")) {
			sb.append("07");
		}
		return sb.toString();
	}

	private static String getUnisysToUnicode(byte[] data) {
		StringBuilder sb = new StringBuilder();
		int remaining = data.length;
		boolean bIsChinese = false;
		int tempVar = data.length;
		for (int i = 0; i < tempVar; i++) {
			//2011/12/18: 排除除了04 07之外特殊的control code
			if (data[i] < 32 && !(data[i] == 4 || data[i] == 7)) {
				continue;
			}
			if (data[i] == 4) {
				bIsChinese = true;
			} else if (data[i] == 7) {
				bIsChinese = false;
			} else {
				if (bIsChinese) {
					String key = getHexStringFromBytes(data, i, 2);
					String value = hUnisysToUnicode.get(key);
					if (value != null) {
						sb.append(value);
					} else {
						sb.append(AlternateBigCode);
					}
					i += 1;
				} else {
					sb.append(StringUtils.join(getHexStringFromBytes(data, i, 1) , "00"));
				}
			}
		}

		return sb.toString();
	}

	private static void loadCNSUnicodeTable() {
		try (BufferedReader br = new BufferedReader(new InputStreamReader(SyscomConverter.class.getClassLoader().getResourceAsStream(codeMap), StandardCharsets.UTF_8))) {
			String readLine = null;
			while ((readLine = br.readLine()) != null) {
				if (!readLine.startsWith("#")) {
					StringTokenizer token = new StringTokenizer(readLine, ",");
					String cns11643 = token.nextToken();
					String sinopac = token.nextToken();
					sinopac = "NULL".equals(sinopac) ? StringUtils.EMPTY : StringUtils.join(sinopac.substring(2, 4), sinopac.substring(0, 2));
					String unicode = token.nextToken();
					unicode = "NULL".equals(unicode) ? StringUtils.EMPTY : StringUtils.join(unicode.substring(2, 4), unicode.substring(0, 2));
					String unisys = token.nextToken();
					unisys = "NULL".equals(unisys) ? StringUtils.EMPTY : unisys;
					if (!hCns11643ToUnicode.containsKey(cns11643) && StringUtils.isNotBlank(unicode)) {
						hCns11643ToUnicode.put(cns11643, unicode);
					}
					if (!hUnicodeToCns11643.containsKey(unicode)) {
						hUnicodeToCns11643.put(unicode, cns11643);
					}
					if (!hUnicodeToUnisys.containsKey(unicode) && StringUtils.isNotBlank(unicode) && StringUtils.isNotBlank(unisys)) {
						hUnicodeToUnisys.put(unicode, unisys);
					}
					if (!hUnicodeToSinopac.containsKey(unicode) && StringUtils.isNotBlank(unicode) && StringUtils.isNotBlank(sinopac)) {
						hUnicodeToSinopac.put(unicode, sinopac);
					}
					if (!hUnisysToUnicode.containsKey(unisys) && StringUtils.isNotBlank(unisys) && StringUtils.isNotBlank(unicode)) {
						hUnisysToUnicode.put(unisys, unicode);
					}
					if (!hCns11643ToSinopac.containsKey(cns11643) && StringUtils.isNotBlank(cns11643) && StringUtils.isNotBlank(sinopac)) {
						hCns11643ToSinopac.put(cns11643, sinopac);
					}
					if (!hSinopacToCNS11643.containsKey(sinopac) && StringUtils.isNotBlank(sinopac) && StringUtils.isNotBlank(cns11643)) {
						hSinopacToCNS11643.put(sinopac, cns11643);
					}
				}
			}
		} catch (Exception e) {
			TRACELogger.exceptionMsg(e, "Cannot load file = [" + codeMap, "]");
		}
	}

	public enum EncodingType {
		Big5,
		Unicode,
		Cns11643,
		Unisys,
		BSP;
	}
}
