package ru.practicum.shareit.item.storage;

import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.item.Comment;

import java.util.Collection;
import java.util.List;

public interface CommentRepository extends JpaRepository<Comment, Long> {
    List<Comment> findByItem_IdIsOrderByCreatedDesc(Long id);

    @Query("select c from Comment c where c.item.id in ?1")
    List<Comment> findCommentsByItems(Collection<Long> ids, Sort sort);

}
