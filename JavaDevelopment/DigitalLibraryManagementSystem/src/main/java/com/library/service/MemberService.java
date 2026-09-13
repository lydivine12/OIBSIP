package com.library.service;

import com.library.model.Member;
import com.library.model.Role;
import com.library.repository.MemberRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class MemberService {

    private final MemberRepository memberRepository;

    public MemberService(MemberRepository memberRepository) {
        this.memberRepository = memberRepository;
    }

    public Member register(String fullName, String email, String rawPassword) {
        if (memberRepository.existsByEmail(email)) {
            throw new IllegalArgumentException("An account with this email already exists.");
        }
        String hash = BCrypt.hashpw(rawPassword, BCrypt.gensalt());
        Member member = new Member(fullName, email, hash, Role.USER);
        return memberRepository.save(member);
    }

    public Optional<Member> authenticate(String email, String rawPassword) {
        return memberRepository.findByEmail(email)
                .filter(m -> BCrypt.checkpw(rawPassword, m.getPasswordHash()));
    }

    public List<Member> findAllUsers() {
        return memberRepository.findAll().stream()
                .filter(m -> m.getRole() == Role.USER)
                .toList();
    }

    public Member getById(Long id) {
        return memberRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Member not found: " + id));
    }

    public void deleteUser(Long id) {
        memberRepository.deleteById(id);
    }
}
