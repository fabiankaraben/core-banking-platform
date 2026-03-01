package com.fabiankaraben.corebanking.transfer.infrastructure.persistence;

import org.springframework.data.jpa.repository.JpaRepository;
import java.util.UUID;

public interface SpringDataTransferRepository extends JpaRepository<TransferEntity, UUID> {}
