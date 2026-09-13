package com.library.service;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Reservation;
import com.library.model.ReservationStatus;
import com.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;

import java.time.LocalDate;
import java.util.List;

@Service
public class ReservationService {

    private final ReservationRepository reservationRepository;

    public ReservationService(ReservationRepository reservationRepository) {
        this.reservationRepository = reservationRepository;
    }

    public Reservation reserve(Book book, Member member) {
        if (book.getAvailableQuantity() > 0) {
            throw new IllegalStateException(
                    "\"" + book.getTitle() + "\" is currently available on the shelf - issue it directly instead of reserving it.");
        }
        if (reservationRepository.existsByBookAndMemberAndStatus(book, member, ReservationStatus.PENDING)) {
            throw new IllegalStateException("You already have a pending reservation for this book.");
        }
        Reservation reservation = new Reservation(book, member, LocalDate.now());
        return reservationRepository.save(reservation);
    }

    public List<Reservation> findForMember(Member member) {
        return reservationRepository.findByMemberOrderByReservationDateDesc(member);
    }

    public void cancel(Long reservationId, Member requester) {
        Reservation reservation = reservationRepository.findById(reservationId)
                .orElseThrow(() -> new IllegalArgumentException("Reservation not found: " + reservationId));
        if (!reservation.getMember().getId().equals(requester.getId()) && !requester.isAdmin()) {
            throw new IllegalStateException("You can only cancel your own reservations.");
        }
        if (reservation.getStatus() != ReservationStatus.PENDING) {
            throw new IllegalStateException("Only pending reservations can be cancelled.");
        }
        reservation.setStatus(ReservationStatus.CANCELLED);
        reservationRepository.save(reservation);
    }
}
