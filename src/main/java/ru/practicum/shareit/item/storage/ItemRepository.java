package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Item;

import java.util.Collection;
import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    List<Item> findByOwnerIdIs(Integer id, Pageable pageable);

    @Query("select i " +
            "from Item i " +
            "where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1, '%')) or lower(i.description) like lower(concat('%', ?1, '%')))")
    List<Item> findItemsByText(String text, Pageable pageable);

    @Query("select i from Item i where i.itemRequest.id in ?1")
    List<Item> findItemsByRequest(Collection<Long> ids, Sort sort);

    List<Item> findByItemRequestIdOrderByIdAsc(Long id);

}
