package com.shutterflow.core.client.event;

import com.shutterflow.core.client.Client;
import com.shutterflow.core.client.ClientRepository;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;

@Component
@RequiredArgsConstructor
@Slf4j
public class ClientSpendAggregator {

    private final ClientRepository clientRepository;

    /**
     * Listens for payment confirmations and recalculates client lifetime spend transactionally.
     */
    @EventListener
    @Transactional
    public void handlePaymentCompleted(PaymentCompletedEvent event) {
        log.info("Interception: Received PaymentCompletedEvent for client: {} with amount: {}", 
                event.getClientId(), event.getAmount());

        Client client = clientRepository.findById(event.getClientId()).orElse(null);
        if (client != null) {
            BigDecimal currentSpend = client.getTotalSpend() != null ? client.getTotalSpend() : BigDecimal.ZERO;
            BigDecimal newSpend = currentSpend.add(event.getAmount());
            client.setTotalSpend(newSpend);
            clientRepository.save(client);
            log.info("Successfully updated client {} lifetime spend from {} to {}", 
                    client.getEmail(), currentSpend, newSpend);
        } else {
            log.warn("Aggregator warning: Client ID {} not found for payment update.", event.getClientId());
        }
    }
}
