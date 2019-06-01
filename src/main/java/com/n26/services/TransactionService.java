package com.n26.services;

import com.n26.models.Transaction;

/**
 * Interface for transaction service
 */
public interface TransactionService {
    void add(Transaction transaction);

    void delete();
}
