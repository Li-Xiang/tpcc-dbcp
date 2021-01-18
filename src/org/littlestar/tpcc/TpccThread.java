package org.littlestar.tpcc;

import java.time.Duration;
import java.time.LocalDateTime;
import java.util.concurrent.locks.ReentrantLock;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.littlestar.helper.RandomHelper;

public class TpccThread implements Runnable, TpccConstants {
	public static Log logger = LogFactory.getLog(TpccThread.class);
	private int num_ware;
	
	public TpccThread() {
		num_ware = TpccContext.getContext().getWarehouses();
	}
	
	@Override
	public void run() {
		try {
			while (TpccContext.transactionOn.get()) {
				TransactionType tran = RandomHelper.randomTransaction();
				switch (tran) {
				case NewOrder:
					doNewOrder();
					break;
				case Payment:
					doPayment();
					break;
				case OrderStatus:
					doOrdstat();
					break;
				case Delivery:
					doDelivery();
					break;
				case StockLevel:
					doSlev();
					break;
				}
			}
		} catch (Throwable e) {
			logger.error("TPC-C benchmark thread - " + Thread.currentThread().getId() +" aborted. ", e);
		}
	}
	
	private volatile long noTotalRuntime = 0l;
	private volatile long noMaxRuntime   = 0l;
	private volatile long noSucceedCount = 0l;
	private volatile long noFailureCount = 0l;
	private volatile long noRetryCount   = 0l;
	private final ReentrantLock noCounterLock = new ReentrantLock();
	
	private volatile long noMaxRuntime2 = 0l;
	
	/**
	 * Get the noMaxRuntime2 then reset it to 0 (阅后即焚), this method use to report output only.
	 * 
	 */
	public long getNoMaxRuntime2() {
		noCounterLock.lock();
		long retVal = noMaxRuntime2;
		noMaxRuntime2 = 0;
		noCounterLock.unlock();
		return retVal;
	}
	
	/**
	 * Get New-Order Transaction execution statistics.
	 * 
	 * @return statistics;
	 */
	public long[] getNewOrderTransactionStatistics() {
		long[] statistics = new long[10];
		noCounterLock.lock();
		statistics[0] = noTotalRuntime;
		statistics[1] = noMaxRuntime;
		statistics[2] = noSucceedCount;
		statistics[3] = noFailureCount;
		statistics[4] = noRetryCount;
		noCounterLock.unlock();
		return statistics;
	}
	
	/**
	 * 
	 * 2.4	The New-Order Transaction
	 *   -> 2.4.1	Input Data Generation
	 * @throws Throwable
	 */
	private void doNewOrder() throws Throwable {
		int w_id = RandomHelper.randomInt(1, num_ware);
		int d_id = RandomHelper.randomInt(1, DIST_PER_WARE);
		int c_id = RandomHelper.nuRand(1023, 1, CUST_PER_DIST);
		int ol_cnt = RandomHelper.randomInt(5, 15);

		int rbk = RandomHelper.randomInt(1, 100);
		int notfound = MAX_ITEMS + 1;
		int[] itemid = new int[MAX_NUM_ITEMS];
		int[] supware = new int[MAX_NUM_ITEMS];
		int[] qty = new int[MAX_NUM_ITEMS];
		int o_all_local = 1;
		for (int i = 0; i < ol_cnt; i++) {
			itemid[i] = RandomHelper.nuRand(8191, 1, MAX_ITEMS);
			if ((i == (ol_cnt - 1)) && (rbk == 1)) {
				itemid[i] = notfound;
			}
			if (RandomHelper.randomInt(1, 100) != 1) {
				supware[i] = w_id;
			} else {
				supware[i] = TpccTransaction.otherWare(w_id, num_ware);
				o_all_local = 0;
			}
			qty[i] = RandomHelper.randomInt(1, 10);
		}
	    
		final LocalDateTime beginTime = LocalDateTime.now();
		int i = 0;
		for (; i < MAX_RETRY; i++) {
			try {
				boolean success = TpccTransaction.newOrder(w_id, d_id, c_id, ol_cnt, o_all_local, itemid, supware, qty);
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				if(logger.isDebugEnabled()) {
					logger.debug("New-order transaction done ( " + runTime + " ms ), seccess = " + success);
				}
				
				if (TpccContext.CountingOn.get()) {
					if (success) {
						noCounterLock.lock();
						noSucceedCount++;  
						noTotalRuntime += runTime;
						noMaxRuntime2 = Math.max(runTime, noMaxRuntime2);
						noCounterLock.unlock();
						noMaxRuntime = Math.max(runTime, noMaxRuntime);
						return ;  
					}
				}
			} catch (NoDataFoundException e) {
				logger.debug("New-order transaction failed with 'NO_DATA_FOUND'", e);
				break; //不重试, 因为传参不变, 结果肯定还是NO_DATA_FOUND, retry没有意义.
			} catch (Throwable e) {
				logger.debug("New-order transaction failed. Retries (" + i + ")", e);
				if (TpccContext.CountingOn.get()) {
					noRetryCount ++;
					//System.out.println(Thread.currentThread().getId()+"R => " +noFailureCount); 
				}
			}
		}
		
		if (TpccContext.CountingOn.get()) {
			noFailureCount++;
		}
	}
	
	private volatile long pyTotalRuntime = 0l;
	private volatile long pyMaxRuntime   = 0l;
	private volatile long pySucceedCount = 0l;
	private volatile long pyFailureCount = 0l;
	private volatile long pyRetryCount   = 0l;
	private final ReentrantLock pyCounterLock = new ReentrantLock();
	private volatile long pyMaxRuntime2 = 0l;
	
	/**
	 * Get the pyMaxRuntime2 then reset it to 0, this method use to report output only.
	 */
	public long getPyMaxRuntime2() {
		pyCounterLock.lock();
		long retVal = pyMaxRuntime2;
		pyMaxRuntime2 = 0;
		pyCounterLock.unlock();
		return retVal;
	}
	
	public long[] getPlaymentTransactionStatistics() {
		long[] statistics = new long[10];
		pyCounterLock.lock();
		statistics[0] = pyTotalRuntime;
		statistics[1] = pyMaxRuntime;
		statistics[2] = pySucceedCount;
		statistics[3] = pyFailureCount;
		statistics[4] = pyRetryCount;
		pyCounterLock.unlock();
		return statistics;
	}
	
	/**
	 * 2.5	The Payment Transaction
	 *   -> 2.5.1	Input Data Generation
	 * @throws Throwable 
	 *   
	 */
	private void doPayment() throws Throwable {
		int w_id = RandomHelper.randomInt(1, num_ware);
		int d_id = RandomHelper.randomInt(1, DIST_PER_WARE);
		int c_id = RandomHelper.nuRand(1023, 1, CUST_PER_DIST);
		String c_last = RandomHelper.lastName(RandomHelper.nuRand(255, 0, 999));
		int h_amount = RandomHelper.randomInt(1, 5000);
		boolean byname;
		if (RandomHelper.randomInt(1, 100) <= 60) {
			byname = true; /* select by last name */
		} else {
			byname = false; /* select by customer id */
		}
		int c_w_id, c_d_id;
		if (RandomHelper.randomInt(1, 100) <= 85) {
			c_w_id = w_id;
			c_d_id = d_id;
		} else {
			c_w_id = TpccTransaction.otherWare(w_id, num_ware);
			c_d_id = RandomHelper.randomInt(1, DIST_PER_WARE);
		}
		
		final LocalDateTime beginTime = LocalDateTime.now();
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				boolean success = TpccTransaction.payment(w_id, d_id, byname, c_w_id, c_d_id, c_id, c_last, h_amount);
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				if(logger.isDebugEnabled()) {
					logger.debug("Payment transaction done ( " + runTime + " ms ), seccess = " + success);
				}
				if (TpccContext.CountingOn.get()) {
					if (success) {
						pyCounterLock.lock();
						pySucceedCount++;
						pyTotalRuntime += runTime;
						pyMaxRuntime2 = Math.max(runTime, pyMaxRuntime2);
						pyCounterLock.unlock();
						pyMaxRuntime = Math.max(runTime, pyMaxRuntime);
						return;
					}
				}
			} catch (NoDataFoundException e) {
				logger.debug("Payment transaction failed with 'NO_DATA_FOUND'", e);
				break;
			} catch (Throwable e) {
				logger.debug("Payment transaction failed. Retries (" + i + ")", e);
				if (TpccContext.CountingOn.get()) {
					pyRetryCount ++;
				}
			}
		}
		if (TpccContext.CountingOn.get()) {
			pyFailureCount++;
		}
	}
	
	private volatile long osTotalRuntime = 0l;
	private volatile long osMaxRuntime   = 0l;
	private volatile long osSucceedCount = 0l;
	private volatile long osFailureCount = 0l;
	private volatile long osRetryCount   = 0l;
	private final ReentrantLock osCounterLock = new ReentrantLock();
	private volatile long osMaxRuntime2 = 0l;
	
	/**
	 * Get the osMaxRuntime2 then reset it to 0, this method use to report output only.
	 */
	public long getOsMaxRuntime2() {
		osCounterLock.lock();
		long retVal = osMaxRuntime2;
		osMaxRuntime2 = 0;
		osCounterLock.unlock();
		return retVal;
	}
	
	public long[] getOrderStatusTransactionStatistics() {
		long[] statistics = new long[10];
		osCounterLock.lock();
		statistics[0] = osTotalRuntime;
		statistics[1] = osMaxRuntime;
		statistics[2] = osSucceedCount;
		statistics[3] = osFailureCount;
		statistics[4] = osRetryCount;
		osCounterLock.unlock();
		return statistics;
	}

	/**
	 * 2.6	The Order-Status Transaction
	 *   --> 2.6.1	Input Data Generation
	 * @return
	 * @throws Throwable
	 */
	private void doOrdstat() throws Throwable {
		int w_id = RandomHelper.randomInt(1, num_ware);
		int d_id = RandomHelper.randomInt(1, DIST_PER_WARE);
		int c_id = RandomHelper.nuRand(1023, 1, CUST_PER_DIST);
		String c_last = RandomHelper.lastName(RandomHelper.nuRand(255, 0, 999));
		boolean byname;
		if (RandomHelper.randomInt(1, 100) <= 60) {
			byname = true; /* select by last name */
		} else {
			byname = false; /* select by customer id */
		}
		
		final LocalDateTime beginTime = LocalDateTime.now();
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				boolean success = TpccTransaction.ordstat(w_id, d_id, byname, c_id, c_last);
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				if(logger.isDebugEnabled()) {
					logger.debug("Order-Status transaction done ( " + runTime + " ms ), seccess = " + success);
				}
				if (TpccContext.CountingOn.get()) {
					if (success) {
						osCounterLock.lock();
						osSucceedCount++;  
						osTotalRuntime += runTime;
						osMaxRuntime2 = Math.max(runTime, osMaxRuntime2);
						osCounterLock.unlock();
						osMaxRuntime = Math.max(runTime, osMaxRuntime);
						return;
					} 
				}
			} catch (NoDataFoundException e) {
				logger.debug("Order-Status transaction failed with 'NO_DATA_FOUND'", e);
				break;
			} catch (Throwable e) {
				logger.debug("Order-Status transaction failed. Retries (" + i + ")", e);
				if (TpccContext.CountingOn.get()) {
					osRetryCount ++;
				}
			}
		}
		if (TpccContext.CountingOn.get()) {
			osFailureCount++;
		}
	}
	
	private volatile long dlTotalRuntime = 0l;
	private volatile long dlMaxRuntime   = 0l;
	private volatile long dlSucceedCount = 0l;
	private volatile long dlFailureCount = 0l;
	private volatile long dlRetryCount   = 0l;
	private final ReentrantLock dlCounterLock = new ReentrantLock();
	private volatile long dlMaxRuntime2 = 0l;
	
	/**
	 * Get the dlMaxRuntime2 then reset it to 0, this method use to report output only.
	 */
	public long getDlMaxRuntime2() {
		dlCounterLock.lock();
		long retVal = dlMaxRuntime2;
		dlMaxRuntime2 = 0;
		dlCounterLock.unlock();
		return retVal;
	}
	
	public long[] getDeliveryTransactionStatistics() {
		long[] statistics = new long[10];
		dlCounterLock.lock();
		statistics[0] = dlTotalRuntime;
		statistics[1] = dlMaxRuntime;
		statistics[2] = dlSucceedCount;
		statistics[3] = dlFailureCount;
		statistics[4] = dlRetryCount;
		dlCounterLock.unlock();
		return statistics;
	}
	
	/**
	 * 
	 * 2.7	The Delivery Transaction
	 *   -> 2.7.1	Input Data Generation
	 * @return
	 * @throws Throwable 
	 */
	private void doDelivery() throws Throwable {
		int w_id = RandomHelper.randomInt(1, num_ware);
		int o_carrier_id = RandomHelper.randomInt(1, 10);
		
		final LocalDateTime beginTime = LocalDateTime.now();
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				boolean success = TpccTransaction.delivery(w_id, o_carrier_id);
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				if(logger.isDebugEnabled()) {
					logger.debug("Delivery transaction done ( " + runTime + " ms ), seccess = " + success);
				}
				if (TpccContext.CountingOn.get()) {
					if (success) {
						dlCounterLock.lock();
						dlSucceedCount++;  
						dlTotalRuntime += runTime;
						dlMaxRuntime2 = Math.max(dlMaxRuntime2, runTime);
						dlCounterLock.unlock();
						dlMaxRuntime = Math.max(runTime, dlMaxRuntime);
						return;
					}
				}
			} catch (NoDataFoundException e) {
				logger.debug("Delivery transaction failed with 'NO_DATA_FOUND'", e);
				break;
			} catch (Throwable e) {
				logger.debug("Delivery transaction failed. Retries (" + i + ")", e);
				if (TpccContext.CountingOn.get()) {
					dlRetryCount ++;
				}
			}
		}
		if (TpccContext.CountingOn.get()) {
			dlFailureCount++;
		}
	}
	
	private volatile long slTotalRuntime = 0l;
	private volatile long slMaxRuntime   = 0l;
	private volatile long slSucceedCount = 0l;
	private volatile long slFailureCount = 0l;
	private volatile long slRetryCount   = 0l;
	private final ReentrantLock slCounterLock = new ReentrantLock();
	private volatile long slMaxRuntime2 = 0l;
	
	/**
	 * Get the slMaxRuntime2 then reset it to 0, this method use to report output only.
	 */
	public long getSlMaxRuntime2() {
		slCounterLock.lock();
		long retVal = slMaxRuntime2;
		slMaxRuntime2 = 0;
		slCounterLock.unlock();
		return retVal;
	}
	
	public long[] getStockLevelTranscationStatistics() {
		long[] statistics = new long[10];
		slCounterLock.lock();
		statistics[0] = slTotalRuntime;
		statistics[1] = slMaxRuntime;
		statistics[2] = slSucceedCount;
		statistics[3] = slFailureCount;
		statistics[4] = slRetryCount;
		slCounterLock.unlock();
		return statistics;
	}
	
	/**
	 * 2.8	The Stock-Level Transaction
	 *   -> 2.8.1	Input Data Generation
	 *   
	 * @throws Throwable 
	 */
	public void doSlev() throws Throwable {
		int w_id, d_id, level;
		w_id = RandomHelper.randomInt(1, num_ware);
		d_id = RandomHelper.randomInt(1, DIST_PER_WARE);
		level = RandomHelper.randomInt(10, 20);

		final LocalDateTime beginTime = LocalDateTime.now();
		for (int i = 0; i < MAX_RETRY; i++) {
			try {
				boolean success = TpccTransaction.slev(w_id, d_id, level);
				final LocalDateTime endTime = LocalDateTime.now();
				final Duration duration = Duration.between(beginTime, endTime);
				final long runTime = duration.toMillis();
				if(logger.isDebugEnabled()) {
					logger.debug("Stock-Level transaction done ( " + runTime + " ms ), seccess = " + success);
				}
				if (TpccContext.CountingOn.get()) {
					if (success) {
						slCounterLock.lock();
						slSucceedCount++;
						slTotalRuntime += runTime;
						slMaxRuntime2 = Math.max(runTime, slMaxRuntime2);
						slCounterLock.unlock();
						slMaxRuntime = Math.max(runTime, slMaxRuntime);
						return;
					}
				}
			} catch (NoDataFoundException e) {
				logger.debug("Stock-Level transaction failed with 'NO_DATA_FOUND'", e);
				break;
			} catch (Throwable e) {
				logger.debug("Stock-Level transaction failed. Retries (" + i + ")", e);
				if (TpccContext.CountingOn.get()) {
					slRetryCount ++;
				}
			}
		}
		if (TpccContext.CountingOn.get()) {
			slFailureCount++;
		}
	}
}
