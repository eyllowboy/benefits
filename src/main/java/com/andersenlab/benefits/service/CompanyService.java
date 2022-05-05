package com.andersenlab.benefits.service;


import com.andersenlab.benefits.domain.CompanyEntity;
import org.springframework.stereotype.Service;

/**
 * Main interface for performing basic operations on database {@link CompanyEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface CompanyService extends CrudService<CompanyEntity> {
}
