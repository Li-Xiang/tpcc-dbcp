package org.littlestar.tpcc;

import java.util.List;

public class ReportThread implements Runnable {
	private long previousNoTotalRuntime = 0L;
	private long previousNoSucceedCount = 0L;

	private long previousPyTotalRuntime = 0L;
	private long previousPySucceedCount = 0L;

	private long previousOsTotalRuntime = 0L;
	private long previousOsSucceedCount = 0L;

	private long previousDlTotalRuntime = 0L;
	private long previousDlSucceedCount = 0L;

	private long previousSlTotalRuntime = 0L;
	private long previousSlSucceedCount = 0L;

	final List<TpccThread> tpccThreads;
	final int period;

	public ReportThread(List<TpccThread> tpccThreads, int period) {
		this.tpccThreads = tpccThreads;
		this.period = period;
	}

	@Override
	public void run() {
		long currentNoTotalRuntime = 0L;
		long currentNoMaxRuntime   = 0L;
		long currentNoSucceedCount = 0L;
		
		long currentPyTotalRuntime = 0L;
		long currentPyMaxRuntime   = 0L;
		long currentPySucceedCount = 0L;
		
		long currentOsTotalRuntime = 0L;
		long currentOsMaxRuntime   = 0L;
		long currentOsSucceedCount = 0L;
		
		long currentDlTotalRuntime = 0L;
		long currentDlMaxRuntime   = 0L;
		long currentDlSucceedCount = 0L;
		
		long currentSlTotalRuntime = 0L;
		long currentSlMaxRuntime   = 0L;
		long currentSlSucceedCount = 0L;
		
		for (TpccThread tpccThread : tpccThreads) {
			
			long[] noStatistics = tpccThread.getNewOrderTransactionStatistics();
			long[] pyStatistics = tpccThread.getPlaymentTransactionStatistics();
			long[] osStatistics = tpccThread.getOrderStatusTransactionStatistics();
			long[] dlStatistics = tpccThread.getDeliveryTransactionStatistics();
			long[] slStatistics = tpccThread.getStockLevelTranscationStatistics();
			
			currentNoTotalRuntime += noStatistics[0];
			currentNoMaxRuntime   = Math.max(currentNoMaxRuntime, tpccThread.getNoMaxRuntime2());
			currentNoSucceedCount += noStatistics[2];
			
			currentPyTotalRuntime += pyStatistics[0];
			currentPyMaxRuntime   = Math.max(currentPyMaxRuntime, tpccThread.getPyMaxRuntime2());
			currentPySucceedCount += pyStatistics[2];
			
			currentOsTotalRuntime += osStatistics[0];
			currentOsMaxRuntime   = Math.max(currentOsMaxRuntime, tpccThread.getOsMaxRuntime2());
			currentOsSucceedCount += osStatistics[2];
			
			currentDlTotalRuntime += dlStatistics[0];
			currentDlMaxRuntime   = Math.max(currentDlMaxRuntime, tpccThread.getDlMaxRuntime2());
			currentDlSucceedCount += dlStatistics[2];
			
			currentSlTotalRuntime += slStatistics[0];
			currentSlMaxRuntime   = Math.max(currentSlMaxRuntime, tpccThread.getSlMaxRuntime2());
			currentSlSucceedCount += slStatistics[2];
		}
		
		long deltaNoTotalRuntime = currentNoTotalRuntime - previousNoTotalRuntime;
		long deltaPyTotalRuntime = currentPyTotalRuntime - previousPyTotalRuntime;
		long deltaOsTotalRuntime = currentOsTotalRuntime - previousOsTotalRuntime;
		long deltaDlTotalRuntime = currentDlTotalRuntime - previousDlTotalRuntime;
		long deltaSlTotalRuntime = currentSlTotalRuntime - previousSlTotalRuntime;
		
		long deltaNoSucceedCount = currentNoSucceedCount - previousNoSucceedCount;
		long deltaPySucceedCount = currentPySucceedCount - previousPySucceedCount;
		long deltaOsSucceedCount = currentOsSucceedCount - previousOsSucceedCount;
		long deltaDlSucceedCount = currentDlSucceedCount - previousDlSucceedCount;
		long deltaSlSucceedCount = currentSlSucceedCount - previousSlSucceedCount;
		
		long noAvgRt = (deltaNoSucceedCount == 0L) ? 0L : (deltaNoTotalRuntime / deltaNoSucceedCount);
		long pyAvgRt = (deltaPySucceedCount == 0L) ? 0L : (deltaPyTotalRuntime / deltaPySucceedCount);
		long osAvgRt = (deltaOsSucceedCount == 0L) ? 0L : (deltaOsTotalRuntime / deltaOsSucceedCount);
		long dlAvgRt = (deltaDlSucceedCount == 0L) ? 0L : (deltaDlTotalRuntime / deltaDlSucceedCount);
		long slAvgRt = (deltaSlSucceedCount == 0L) ? 0L : (deltaSlTotalRuntime / deltaSlSucceedCount);
		
		previousNoTotalRuntime = currentNoTotalRuntime;
		previousNoSucceedCount = currentNoSucceedCount;
		
		previousPyTotalRuntime = currentPyTotalRuntime;
		previousPySucceedCount = currentPySucceedCount;
		
		previousOsTotalRuntime = currentOsTotalRuntime;
		previousOsSucceedCount = currentOsSucceedCount;
		
		previousDlTotalRuntime = currentDlTotalRuntime;
		previousDlSucceedCount = currentDlSucceedCount;
		
		previousSlTotalRuntime = currentSlTotalRuntime;
		previousSlSucceedCount = currentSlSucceedCount;
		
		long totalTx = deltaNoSucceedCount + deltaPySucceedCount + deltaOsSucceedCount + deltaDlSucceedCount + deltaSlSucceedCount;
		long totalTps = totalTx / period;
		
		long noTps = deltaNoSucceedCount / period;
		long pyTps = deltaPySucceedCount / period;
		long osTps = deltaOsSucceedCount / period;
		long dlTps = deltaDlSucceedCount / period;
		long slTps = deltaSlSucceedCount / period;
		
		TpccHelper.reportRow(totalTx, totalTps, 
				//TXs TPS AvgRt MaxRt
				deltaNoSucceedCount, noTps, noAvgRt, currentNoMaxRuntime, 
				deltaPySucceedCount, pyTps, pyAvgRt, currentPyMaxRuntime, 
				deltaOsSucceedCount, osTps, osAvgRt, currentOsMaxRuntime,
				deltaDlSucceedCount, dlTps, dlAvgRt, currentDlMaxRuntime,
				deltaSlSucceedCount, slTps, slAvgRt, currentSlMaxRuntime
				);
	}

}
