package com.library.repository;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface ReservationRepository extends JpaRepository<Reservation, Long> {

    List<Reservation> findByMemberOrderByReservationDateDesc(Member member);

    List<Reservation> findByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);

    Optional<Reservation> findFirstByBookAndStatusOrderByReservationDateAsc(Book book, ReservationStatus status);

    boolean existsByBookAndMemberAndStatus(Book book, Member member, ReservationStatus status);
}
