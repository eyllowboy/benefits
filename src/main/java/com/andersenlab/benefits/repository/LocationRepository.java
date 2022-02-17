package com.andersenlab.benefits.repository;

import com.andersenlab.benefits.domain.LocationEntity;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Modifying;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import javax.transaction.Transactional;
import java.util.List;
import java.util.Optional;

@Repository
public interface LocationRepository extends JpaRepository<LocationEntity, Long> {

    @Query("FROM LocationEntity loc WHERE (loc.country = :country)")
    List<Optional<LocationEntity>> findByCountry(@Param(value = "country") final String country);

    @Query("FROM LocationEntity loc WHERE (loc.country = :country) AND (loc.city = :city)")
    Optional<LocationEntity> findByCity(@Param(value = "country") final String country,
                                        @Param(value = "city") final String city);

    @Query("FROM LocationEntity loc WHERE (loc.country = :country) AND (LOWER(loc.city) LIKE LOWER(CONCAT(:filterMask, '%'))) ORDER BY loc.city")
    List<Optional<LocationEntity>> findByFirstLetters(@Param(value = "country") final String country,
                                                      @Param(value = "filterMask") final String filterMask);

    @Modifying
    @Transactional
    @Query("UPDATE LocationEntity loc SET loc.country = :country, loc.city = :city where loc.id = :id")
    void updateLocationEntity(@Param(value = "id") final Long id,
                              @Param(value = "country") final String country,
                              @Param(value = "city") final String city);

}