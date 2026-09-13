package com.library.service;

import com.library.model.*;
import com.library.repository.BookRepository;
import com.library.repository.FineRepository;
import com.library.repository.IssueRecordRepository;
import com.library.repository.ReservationRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.temporal.ChronoUnit;
import java.util.List;

@Service
public class IssueService {

    /** Standard loan period, in days. */
    public static final int LOAN_PERIOD_DAYS = 14;

    /** Fine per day overdue. */
    public static final BigDecimal FINE_PER_DAY = new BigDecimal("5.00");

    private final BookRepository bookRepository;
    private final IssueRecordRepository issueRecordRepository;
    private final FineRepository fineRepository;
    private final ReservationRepository reservationRepository;

    public IssueService(BookRepository bookRepository,
                         IssueRecordRepository issueRecordRepository,
                         FineRepository fineRepository,
                         ReservationRepository reservationRepository) {
        this.bookRepository = bookRepository;
        this.issueRecordRepository = issueRecordRepository;
        this.fineRepository = fineRepository;
        this.reservationRepository = reservationRepository;
    }

    @Transactional
    public IssueRecord issueBook(Book book, Member member) {
        if (book.getAvailableQuantity() <= 0) {
            throw new IllegalStateException(
                    "\"" + book.getTitle() + "\" has no available copies right now. You can place an advance booking instead.");
        }
        book.setAvailableQuantity(book.getAvailableQuantity() - 1);
        bookRepository.save(book);

        LocalDate today = LocalDate.now();
        IssueRecord record = new IssueRecord(book, member, today, today.plusDays(LOAN_PERIOD_DAYS));
        return issueRecordRepository.save(record);
    }

    /**
     * Returns a book. Calculates and stores an overdue fine if applicable,
     * then automatically issues the freed copy to the next pending
     * reservation for that book, if one exists.
     */
    @Transactional
    public Fine returnBook(IssueRecord record) {
        if (record.isReturned()) {
            throw new IllegalStateException("This book has already been returned.");
        }
        LocalDate today = LocalDate.now();
        record.setReturnDate(today);
        issueRecordRepository.save(record);

        Fine fine = null;
        if (today.isAfter(record.getDueDate())) {
            long daysLate = ChronoUnit.DAYS.between(record.getDueDate(), today);
            BigDecimal amount = FINE_PER_DAY.multiply(BigDecimal.valueOf(daysLate));
            fine = new Fine(record, amount);
            fineRepository.save(fine);
        }

        Book book = record.getBook();
        // Try to satisfy the oldest pending reservation for this book first.
        reservationRepository.findFirstByBookAndStatusOrderByReservationDateAsc(book, ReservationStatus.PENDING)
                .ifPresentOrElse(reservation -> {
                    reservation.setStatus(ReservationStatus.FULFILLED);
                    reservationRepository.save(reservation);

                    LocalDate issueDate = LocalDate.now();
                    IssueRecord newRecord = new IssueRecord(book, reservation.getMember(), issueDate,
                            issueDate.plusDays(LOAN_PERIOD_DAYS));
                    issueRecordRepository.save(newRecord);
                    // Available quantity stays the same: the copy moves straight
                    // from "returned" to "issued to the reservation holder".
                }, () -> {
                    book.setAvailableQuantity(book.getAvailableQuantity() + 1);
                    bookRepository.save(book);
                });

        return fine;
    }

    public List<IssueRecord> findActiveForMember(Member member) {
        return issueRecordRepository.findByMemberOrderByIssueDateDesc(member);
    }

    public List<IssueRecord> findAllCurrentlyIssued() {
        return issueRecordRepository.findByReturnDateIsNullOrderByDueDateAsc();
    }

    public IssueRecord getById(Long id) {
        return issueRecordRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Issue record not found: " + id));
    }

    public boolean isOverdue(IssueRecord record) {
        return !record.isReturned() && LocalDate.now().isAfter(record.getDueDate());
    }
}
