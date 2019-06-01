package com.n26.aggregators.cache.impl;

import com.n26.aggregators.cache.TransactionStatisticsCache;
import com.n26.aggregators.cache.bucket.TransactionStatisticsBucket;
import com.n26.models.Statistics;
import com.n26.models.Transaction;
import com.n26.validators.Validator;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Component;

import javax.annotation.PostConstruct;
import java.util.Arrays;
import java.util.List;
import java.util.stream.Collectors;

/**
 * Cache implementation using array.
 */
@Component("transactionStatisticsArrayCache")
public class TransactionStatisticsArrayCache implements TransactionStatisticsCache {
    private TransactionStatisticsBucket[] transactionStatisticsBuckets;

    @Value("${aggregator.window.size}")
    private int aggregatorWindowSize;

    @Value("${aggregator.bucket.size}")
    private int aggregatorBucketSize;

    @Value("${statistics.decimal.scale}")
    private int decimalScale;

    @Autowired
    private Validator validator;

    private int numberOfBuckets;

    @PostConstruct
    private void init() {
        numberOfBuckets = aggregatorWindowSize / aggregatorBucketSize;
        transactionStatisticsBuckets = new TransactionStatisticsBucket[numberOfBuckets];
        for (int i = 0; i < numberOfBuckets; i++) {
            transactionStatisticsBuckets[i] = new TransactionStatisticsBucket(validator, decimalScale);
        }
    }

    @Override
    public void add(final Transaction transaction) {
        validator.validateTransaction(transaction);
        insert(transaction);
    }

    @Override
    public void clear() {
        for (int i = 0; i < numberOfBuckets; i++) {
            transactionStatisticsBuckets[i].clear();
        }
    }

    @Override
    public Statistics getStatistics() {
        final List<TransactionStatisticsBucket> validBuckets = Arrays.stream(transactionStatisticsBuckets)
                .filter(bucket -> bucket.isValid())
                .collect(Collectors.toList());
        final Statistics aggregatedStatistics = new Statistics();
        validBuckets.forEach(validBucket -> validBucket.aggregate(aggregatedStatistics));
        aggregatedStatistics.check();
        return aggregatedStatistics;
    }

    private void insert(final Transaction transaction) {
        final TransactionStatisticsBucket bucket = getBucket(transaction);
        bucket.insert(transaction);
    }

    private TransactionStatisticsBucket getBucket(Transaction transaction) {
        final long currentTime = System.currentTimeMillis();
        final int bucketIndex = (int) (((currentTime - transaction.getTimestamp()) / aggregatorBucketSize));
        //mod numberOfBuckets to be on the safe side.
        return transactionStatisticsBuckets[bucketIndex % numberOfBuckets];
    }
}
