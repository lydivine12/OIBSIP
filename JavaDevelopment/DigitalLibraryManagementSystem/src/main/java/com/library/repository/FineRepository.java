package com.library.repository;

import com.library.model.Fine;
import com.library.model.IssueRecord;
import com.library.model.Member;
import org.springframework.data.jpa.repository.JpaRepository;

import java.util.List;
import java.util.Optional;

public interface FineRepository extends JpaRepository<Fine, Long> {

    Optional<Fine> findByIssueRecord(IssueRecord issueRecord);

    List<Fine> findByIssueRecord_MemberOrderByIdDesc(Member member);

    List<Fine> findAllByOrderByPaidAscIdDesc();
}
