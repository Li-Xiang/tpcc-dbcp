package org.littlestar.tpcc;

import java.util.concurrent.atomic.AtomicLong;
import java.util.concurrent.locks.ReentrantLock;

public class TpccCounter {
	
	/*
	 * 定义不通事务响应时间限制，单位为毫秒.
	 * 5.2.5.7	The following table summarizes the transaction mix, wait times, and response time constraints: 
	 * 。。。
	 */
	public static final int NO_RESPONSE_TIME_LIMIT = 5000; // ms
	public static final int PY_RESPONSE_TIME_LIMIT = 5000; // ms
	public static final int OS_RESPONSE_TIME_LIMIT = 5000; // ms
	public static final int DL_RESPONSE_TIME_LIMIT = 5000; // ms
	public static final int SL_RESPONSE_TIME_LIMIT = 20000; // ms
	
	public static final AtomicLong noTotalRt  = new AtomicLong(0);
	public static final AtomicLong noSuccCnt = new AtomicLong(0); 
	public static AtomicLong noRetryCnt = new AtomicLong(0);
	
	private static volatile long noMaxRt    = 0l;
	private static ReentrantLock noMaxRtLock = new ReentrantLock();
	
	public static void setNoMaxRt(final long newValue) {
		noMaxRtLock.lock();
		if(noMaxRt < newValue) {
			noMaxRt++;
		}
		noMaxRtLock.unlock();
	}
	
	public static long getNoMaxRt() {
		long ret = 0;
		noMaxRtLock.lock();
		ret = noMaxRt;
		noMaxRtLock.unlock();
		return ret;
	}
	
	public static void resetNoMaxRt() {
		noMaxRtLock.lock();
		noMaxRt = 0l;
		noMaxRtLock.unlock();
	}
	
	
	public static final AtomicLong pyTotalRt  = new AtomicLong(0);
	public static final AtomicLong pySuccCnt = new AtomicLong(0); 
	public static AtomicLong pyRetryCnt = new AtomicLong(0);
	
	private static volatile long pyMaxRt    = 0l;
	private static ReentrantLock pyMaxRtLock = new ReentrantLock();
	
	public static void setPyMaxRt(final long newValue) {
		pyMaxRtLock.lock();
		if(pyMaxRt < newValue) {
			pyMaxRt++;
		}
		pyMaxRtLock.unlock();
	}
	
	public static long getPyMaxRt() {
		long ret = 0;
		pyMaxRtLock.lock();
		ret = pyMaxRt;
		pyMaxRtLock.unlock();
		return ret;
	}
	
	public static void resetPyMaxRt() {
		pyMaxRtLock.lock();
		pyMaxRt = 0l;
		pyMaxRtLock.unlock();
	}
	
	
	
}
