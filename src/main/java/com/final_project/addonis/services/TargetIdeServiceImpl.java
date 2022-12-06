package com.final_project.addonis.services;

import com.final_project.addonis.models.TargetIde;
import com.final_project.addonis.repositories.contracts.TargetIdeRepository;
import com.final_project.addonis.services.contracts.TargetIdeService;
import com.final_project.addonis.utils.exceptions.DuplicateEntityException;
import com.final_project.addonis.utils.exceptions.EntityNotFoundException;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class TargetIdeServiceImpl implements TargetIdeService {

    private final TargetIdeRepository repository;

    public TargetIdeServiceImpl(TargetIdeRepository repository) {
        this.repository = repository;
    }

    @Override
    public List<TargetIde> getAll() {
        return repository.findAll();
    }

    @Override
    public TargetIde getById(int id) {
        return repository.findById(id)
                .orElseThrow(() -> new EntityNotFoundException("TargetIde", id));
    }

    @Override
    public TargetIde create(TargetIde targetIde) {
        verifyTargetIdeNameIsUnique(targetIde.getName());
        return repository.save(targetIde);
    }

    @Override
    public TargetIde update(TargetIde targetIde) {
        verifyTargetIdeNameIsUnique(targetIde.getName());
        return repository.saveAndFlush(targetIde);
    }

    @Override
    public void delete(TargetIde targetIde) {
        repository.delete(targetIde);
    }

    @Override
    public boolean existByName(String name) {
        return repository.existsByName(name);
    }

    private void verifyTargetIdeNameIsUnique(String targetIdeName) {
        if (existByName(targetIdeName)) {
            throw new DuplicateEntityException("Target ide", "name", targetIdeName);
        }
    }

    @Override
    public TargetIde getByName(String name) {
        return repository.findByNameIgnoreCase(name)
                .orElseThrow(() -> new EntityNotFoundException("TargetIde", "name", name));

    }
}
