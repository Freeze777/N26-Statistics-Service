package com.n26.aggregators.cache.bucket;

import com.n26.models.Statistics;
import com.n26.models.Transaction;
import com.n26.validators.Validator;

import java.math.BigDecimal;
import java.math.RoundingMode;

/**
 *
 * This bucket holds statistics of all transactions that falls in a second.
 * The object of this class is used for Array cache implementation.
 *
 */
public class TransactionStatisticsBucket {

    private Statistics statistics;
    private Validator validator;
    private int decimalScale;

    public TransactionStatisticsBucket(final Validator validator, final int decimalScale) {
        this.statistics = new Statistics();
        this.validator = validator;
        this.decimalScale = decimalScale;
    }

    private boolean isEmpty() {
        return (statistics.getCount() == 0);
    }

    public synchronized boolean isValid() {
        return validator.isValidStatistics(statistics);
    }

    public synchronized void insert(final Transaction transaction) {
        if (isEmpty()) {
            init(transaction);
        } else {
            if (isValid()) {
                merge(transaction);
            } else {
                clear();
                init(transaction);
            }
        }
    }

    private void merge(Transaction transaction) {
        statistics.setSum(statistics.getSum().add(transaction.getAmount()));
        statistics.setCount(statistics.getCount() + 1L);
        statistics.setAvg(statistics.getSum()
                .divide(BigDecimal.valueOf(statistics.getCount()), decimalScale, RoundingMode.HALF_UP));
        statistics.setMin(transaction.getAmount().min(statistics.getMin()));
        statistics.setMax(transaction.getAmount().max(statistics.getMax()));
    }

    private void init(final Transaction transaction) {
        statistics.setMin(transaction.getAmount());
        statistics.setMax(transaction.getAmount());
        statistics.setCount(1L);
        statistics.setAvg(transaction.getAmount());
        statistics.setSum(transaction.getAmount());
        statistics.setTimestamp(transaction.getTimestamp());
    }

    public synchronized void clear() {
        statistics.clear();
    }

    public synchronized Statistics aggregate(final Statistics aggregatedStatistics) {
        aggregatedStatistics.setSum(aggregatedStatistics.getSum().add(statistics.getSum()));
        aggregatedStatistics.setCount(aggregatedStatistics.getCount() + statistics.getCount());
        aggregatedStatistics.setAvg(aggregatedStatistics.getSum()
                .divide(BigDecimal.valueOf(aggregatedStatistics.getCount()), decimalScale, RoundingMode.HALF_UP));
        aggregatedStatistics.setMin(statistics.getMin().min(aggregatedStatistics.getMin()));
        aggregatedStatistics.setMax(statistics.getMax().max(aggregatedStatistics.getMax()));
        return aggregatedStatistics;
    }
}
