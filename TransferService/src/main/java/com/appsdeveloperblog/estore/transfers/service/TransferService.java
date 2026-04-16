package com.appsdeveloperblog.estore.transfers.service;

import com.appsdeveloperblog.estore.transfers.dto.TransferRequest;

public interface TransferService {
    public boolean transfer(TransferRequest productPaymentRestModel);
}
