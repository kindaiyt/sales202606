package com.sakufukai.sales202606.repository;

import com.sakufukai.sales202606.entity.Store;
import org.springframework.data.jpa.repository.EntityGraph;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

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

}
