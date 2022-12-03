package com.final_project.addonis.repositories.contracts;

import com.final_project.addonis.models.BinaryContent;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;

@Repository
public interface BinaryContentRepository extends JpaRepository<BinaryContent, Integer> {

    Optional<BinaryContent> findByData(byte[] data);
}
