package com.library.config;

import com.library.model.Book;
import com.library.model.Member;
import com.library.model.Role;
import com.library.repository.BookRepository;
import com.library.repository.MemberRepository;
import org.mindrot.jbcrypt.BCrypt;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;

@Component
public class DataInitializer implements CommandLineRunner {

    private final MemberRepository memberRepository;
    private final BookRepository bookRepository;

    public DataInitializer(MemberRepository memberRepository, BookRepository bookRepository) {
        this.memberRepository = memberRepository;
        this.bookRepository = bookRepository;
    }

    @Override
    public void run(String... args) {
        if (memberRepository.count() == 0) {
            memberRepository.save(new Member("Library Admin", "admin@library.com",
                    BCrypt.hashpw("admin123", BCrypt.gensalt()), Role.ADMIN));
            memberRepository.save(new Member("Demo User", "user@library.com",
                    BCrypt.hashpw("user123", BCrypt.gensalt()), Role.USER));
        }

        if (bookRepository.count() == 0) {
            bookRepository.save(new Book("Clean Code", "Robert C. Martin", "9780132350884", "Programming", 3));
            bookRepository.save(new Book("Effective Java", "Joshua Bloch", "9780134685991", "Programming", 2));
            bookRepository.save(new Book("Design Patterns", "Gang of Four", "9780201633610", "Programming", 1));
            bookRepository.save(new Book("The Pragmatic Programmer", "Andrew Hunt", "9780135957059", "Programming", 2));
            bookRepository.save(new Book("A Brief History of Time", "Stephen Hawking", "9780553380163", "Science", 2));
            bookRepository.save(new Book("Sapiens", "Yuval Noah Harari", "9780062316097", "History", 3));
            bookRepository.save(new Book("1984", "George Orwell", "9780451524935", "Fiction", 2));
            bookRepository.save(new Book("To Kill a Mockingbird", "Harper Lee", "9780061120084", "Fiction", 1));
            bookRepository.save(new Book("The Alchemist", "Paulo Coelho", "9780061122415", "Fiction", 2));
            bookRepository.save(new Book("Atomic Habits", "James Clear", "9780735211292", "Self-help", 3));
        }
    }
}
