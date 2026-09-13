package com.library.service;

import com.library.model.Fine;
import com.library.model.Member;
import com.library.repository.FineRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class FineService {

    private final FineRepository fineRepository;

    public FineService(FineRepository fineRepository) {
        this.fineRepository = fineRepository;
    }

    public List<Fine> findAll() {
        return fineRepository.findAllByOrderByPaidAscIdDesc();
    }

    public List<Fine> findForMember(Member member) {
        return fineRepository.findByIssueRecord_MemberOrderByIdDesc(member);
    }

    public void markPaid(Long fineId) {
        Fine fine = fineRepository.findById(fineId)
                .orElseThrow(() -> new IllegalArgumentException("Fine not found: " + fineId));
        fine.setPaid(true);
        fineRepository.save(fine);
    }
}
