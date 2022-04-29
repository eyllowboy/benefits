package com.andersenlab.benefits.service;


import com.andersenlab.benefits.domain.CompanyEntity;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

/**
 * Main interface for performing basic operations on database {@link CompanyEntity}.
 *
 * @author Aleksei Sidorin
 * @version 1.0
 */

@Service
public interface CompanyService extends CrudService<CompanyEntity> {
}
