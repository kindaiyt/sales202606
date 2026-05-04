package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Store;
import com.sakufukai.sales202606.entity.StoreType;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;

import java.util.List;
import java.util.Optional;

public interface StoreRepository extends JpaRepository<Store, Long> {
    Optional<Store> findByUrl(String url);

    @EntityGraph(attributePaths = {"userStores", "userStores.user"})
    Optional<Store> findWithUsersById(Long id);

    @Query("""
  select distinct s
  from Store s
  left join fetch s.userStores us
  left join fetch us.user u
  order by s.id desc
""")
    List<Store> findAllWithUsers();

    @Query("""
        SELECT s
        FROM Store s
        LEFT JOIN FETCH s.userStores us
        LEFT JOIN FETCH us.user
        WHERE s.url = :url
    """)
    Optional<Store> findByUrlWithUsers(@Param("url") String url);

    boolean existsByUrl(String url);

    List<Store> findByStoreType(StoreType storeType);
}
