package com.library.repository;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface IssueRecordRepository extends JpaRepository<IssueRecord, Long> {

    List<IssueRecord> findByMemberOrderByIssueDateDesc(Member member);

    List<IssueRecord> findByReturnDateIsNull();

    List<IssueRecord> findByReturnDateIsNullOrderByDueDateAsc();

    Optional<IssueRecord> findFirstByBookAndReturnDateIsNull(Book book);

    long countByBookAndReturnDateIsNull(Book book);
}
