package com.ee.ctp.ftdc.processor;

import com.ee.ctp.dto.RspError;
import com.ee.ctp.dto.RtnTrade;
import com.ee.ctp.enums.ChainType;
import com.ee.ctp.ftdc.FtdcProtocol.Ftdc;
import com.ee.ctp.handler.FtdcTraderSpi;

import io.netty.buffer.ByteBuf;
/**
 * 
 * @author ee
 * 2017年11月12日 下午11:23:20
 *
 */
public class RtnTradeTidProcessor implements FtdcTidProcessor{

	@Override
	public void process(Ftdc ftdc, FtdcTraderSpi spi, RspError error) {
		ByteBuf ftdcBody = ftdc.ftdcBody();
		boolean lastPacket = false;
		if(ChainType.END.chain() == ftdc.getChain()) {
			lastPacket = true;
		}
		for(int i = 0; i < ftdc.getNumData(); i++) {
			if(i != 0) {
				//每个数据域都是以TID开头
				if(ftdcBody.isReadable(4)) {
					ftdcBody.readInt();
				}else {
					break;
				}
			}
			boolean isLast = lastPacket && (i == ftdc.getNumData() - 1);
			RtnTrade rtnTrade = new RtnTrade().parseFrom(ftdcBody.readRetainedSlice(ftdc.getStructOfTidLen()), error);
			int reqId = (int)ftdc.getReqId();
			if(reqId == 0) {
				spi.onRtnTrade(rtnTrade, reqId, isLast);
			}else {
				spi.onRspQryTrade(rtnTrade, reqId, isLast);
			}
		}
	}
}
