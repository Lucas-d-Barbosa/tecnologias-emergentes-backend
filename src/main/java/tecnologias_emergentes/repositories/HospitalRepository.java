package tecnologias_emergentes.repositories;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;
import tecnologias_emergentes.models.Hospital;

import java.util.Optional;

@Repository
public interface HospitalRepository extends JpaRepository<Hospital, Long> {

    @Query("""
	    select h
	    from Hospital h
	    where h.categoryName = :categoryName
	      and h.categoryType = :categoryType
	      and h.address.id = :addressId
	    """)
    Optional<Hospital> findByCategoryNameAndCategoryTypeAndAddressId(
	    @Param("categoryName") String categoryName,
	    @Param("categoryType") String categoryType,
	    @Param("addressId") Long addressId);
}