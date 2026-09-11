package com.myproject.payment.service;

import org.springframework.stereotype.Service;

import com.myproject.payment.entity.Wallet;
import com.myproject.payment.repository.WalletRepository;

import jakarta.transaction.Transactional;

@Service
public class WalletService {
    private final WalletRepository walletRepository;

    public WalletService(WalletRepository walletRepository) {
        this.walletRepository = walletRepository;
    }

    // Search for wallet by userID
    @Transactional
    public Wallet getWalletByUserId(String userId) {
        return walletRepository.findByUserIdForUpdate(userId)
                .orElseThrow(() -> new RuntimeException("Wallet not found for user ID: " + userId));
    }
}
