package ru.practicum.shareit.booking.storage;

import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import ru.practicum.shareit.booking.Booking;

import java.util.List;

public interface BookingRepository extends JpaRepository<Booking, Long> {

    @Query("select b from Booking b where b.booker.id = ?1 order by b.startDate DESC")
    List<Booking> findByBookerId(Integer id);

    @Query("select b from Booking b " +
            "where b.booker.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.endDate > CURRENT_TIMESTAMP " +
            "order by b.startDate DESC")
    List<Booking> findCurrentBookingsByBookerId(Integer id);

    @Query("select b from Booking b where b.booker.id = ?1 and b.endDate < CURRENT_TIMESTAMP order by b.startDate DESC")
    List<Booking> findPastBookingsByBookerId(Integer id);

    @Query("select b from Booking b where b.booker.id = ?1 and b.startDate > CURRENT_TIMESTAMP order by b.startDate DESC")
    List<Booking> findFutureBookingsByBookerId(Integer userId);

    @Query("select b from Booking b where b.booker.id = ?1 and b.status = ?2 order by b.startDate DESC")
    List<Booking> findBookingsByBookerIdAndBookingStatus(Integer id, Booking.BookingStatus status);

    @Query("select b from Booking b where b.item.owner.id = ?1 order by b.startDate DESC")
    List<Booking> findByOwnerId(Integer id);

    @Query("select b from Booking b " +
            "where b.item.owner.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.endDate > CURRENT_TIMESTAMP " +
            "order by b.startDate DESC")
    List<Booking> findCurrentBookingsByOwnerId(Integer id);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.endDate < CURRENT_TIMESTAMP order by b.startDate DESC")
    List<Booking> findPastBookingsByOwnerId(Integer id);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.startDate > CURRENT_TIMESTAMP order by b.startDate DESC")
    List<Booking> findFutureBookingsByOwnerId(Integer userId);

    @Query("select b from Booking b where b.item.owner.id = ?1 and b.status = ?2 order by b.startDate DESC")
    List<Booking> findBookingsByOwnerIdAndBookingStatus(Integer id, Booking.BookingStatus status);

    @Query("select b from Booking b where b.item.id = ?1 and b.startDate < CURRENT_TIMESTAMP and b.status = 'APPROVED' order by b.startDate DESC")
    List<Booking> findOwnerLastBooking(Long id);

    @Query("select b from Booking b where b.item.id = ?1 and b.startDate > CURRENT_TIMESTAMP and b.status = 'APPROVED' order by b.startDate ASC")
    List<Booking> findOwnerNextBooking(Long id);


    @Query("select b from Booking b where b.booker.id = ?1 and b.item.id = ?2 and b.endDate < CURRENT_TIMESTAMP")
    List<Booking> findExpiredByBookerIdAndItemId(int userId, long itemId);
}
