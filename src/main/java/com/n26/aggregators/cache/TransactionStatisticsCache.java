package com.n26.aggregators.cache;

import com.n26.models.Statistics;
import com.n26.models.Transaction;

/**
 * Interface for cache holding transaction statistics
 */
public interface TransactionStatisticsCache {

    void add(Transaction transaction);

    void clear();

    Statistics getStatistics();
}
