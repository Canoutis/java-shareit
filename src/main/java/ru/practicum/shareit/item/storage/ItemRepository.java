package ru.practicum.shareit.item.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Item;

import java.util.List;

public interface ItemRepository extends JpaRepository<Item, Long> {

    @Query("select i from Item i where i.owner.id = ?1 order by i.id")
    List<Item> findItemsByUserId(Integer id);


    @Query("select i " +
            "from Item i " +
            "where i.available = true and " +
            "(lower(i.name) like lower(concat('%', ?1, '%')) or lower(i.description) like lower(concat('%', ?1, '%')))")
    List<Item> findItemsByText(String text);


}
