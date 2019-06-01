package com.n26.services.impl;

import com.n26.aggregators.cache.TransactionStatisticsCache;
import com.n26.models.Transaction;
import com.n26.services.TransactionService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.stereotype.Component;

/**
 * Transaction service implementation.
 * The service holds a shared cache that stores the
 * statistics of past transactions.
 */
@Component
public class TransactionServiceImpl implements TransactionService {

    @Autowired
    @Qualifier("transactionStatisticsMapCache")
    private TransactionStatisticsCache transactionStatisticsCache;

    @Override
    public void add(Transaction transaction) {
        transactionStatisticsCache.add(transaction);
    }

    @Override
    public void delete() {
        transactionStatisticsCache.clear();
    }
}
