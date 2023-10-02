package ru.practicum.shareit.request.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import ru.practicum.shareit.request.ItemRequest;

import java.util.List;


public interface ItemRequestRepository extends JpaRepository<ItemRequest, Long> {
    List<ItemRequest> findByOwnerIdIs(Integer id, Sort sort);

    List<ItemRequest> findDistinctByOwnerIdNot(Integer id, Pageable page);

    List<ItemRequest> findDistinctByOwnerIdNot(Integer id, Sort sort);

}
