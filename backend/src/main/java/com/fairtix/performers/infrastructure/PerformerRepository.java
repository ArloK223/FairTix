package com.fairtix.performers.infrastructure;

import com.fairtix.performers.domain.Performer;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.Optional;
import java.util.UUID;

public interface PerformerRepository extends JpaRepository<Performer, UUID> {
  Optional<Performer> findByNameIgnoreCase(String name);
  boolean existsByNameIgnoreCaseAndIdNot(String name, UUID id);
}
