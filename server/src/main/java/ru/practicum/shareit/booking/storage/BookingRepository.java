package ru.practicum.shareit.booking.storage;

import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;

import java.time.LocalDateTime;
import java.util.Collection;
import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.booker.id = ?1")
    List<Booking> findByBookerId(Integer id, Pageable page);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.endDate > CURRENT_TIMESTAMP")
    List<Booking> findCurrentBookingsByBookerId(Integer id, Pageable page);

    @Query("select b from Booking b where b.booker.id = ?1 and b.endDate < CURRENT_TIMESTAMP")
    List<Booking> findPastBookingsByBookerId(Integer id, Pageable page);

    @Query("select b from Booking b where b.booker.id = ?1 and b.startDate > CURRENT_TIMESTAMP")
    List<Booking> findFutureBookingsByBookerId(Integer userId, Pageable page);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2")
    List<Booking> findBookingsByBookerIdAndBookingStatus(Integer id, Booking.BookingStatus status, Pageable page);

    @Query("select b from Booking b where b.item.owner.id = ?1")
    List<Booking> findByOwnerId(Integer id, Pageable page);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.endDate > CURRENT_TIMESTAMP")
    List<Booking> findCurrentBookingsByOwnerId(Integer id, Pageable page);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.endDate < CURRENT_TIMESTAMP")
    List<Booking> findPastBookingsByOwnerId(Integer id, Pageable page);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.startDate > CURRENT_TIMESTAMP")
    List<Booking> findFutureBookingsByOwnerId(Integer userId, Pageable page);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2")
    List<Booking> findBookingsByOwnerIdAndBookingStatus(Integer id, Booking.BookingStatus status, Pageable page);

    @Query("select b from Booking b where b.item.id in ?1 and b.status = 'APPROVED'")
    List<Booking> findApprovedBookings(Collection<Long> ids, Sort sort);

    @Query("select b from Booking b where b.item.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.status = 'APPROVED'")
    List<Booking> findOwnerLastBooking(Long id, Sort sort);

    @Query("select b from Booking b where b.item.id = ?1 and b.startDate > CURRENT_TIMESTAMP and b.status = 'APPROVED'")
    List<Booking> findOwnerNextBooking(Long id, Sort sort);

    @Query("select b from Booking b where b.booker.id = ?1 and b.item.id = ?2 and b.endDate < CURRENT_TIMESTAMP")
    List<Booking> findExpiredByBookerIdAndItemId(int userId, long itemId);

    @Query("select (count(b) > 0) from Booking b " +
            "where b.item.id = ?1 and b.status = 'APPROVED' and (b.startDate between ?2 and ?3 or b.endDate between ?2 and ?3)")
    boolean hasApprovedBookingInPeriod(Long id, LocalDateTime startDate, LocalDateTime endDate);

}
