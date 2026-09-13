package com.library.controller;

import com.library.model.*;
import com.library.service.*;
import jakarta.servlet.http.HttpSession;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

import java.util.List;

import static com.library.config.AuthInterceptor.SESSION_MEMBER_ID;

@Controller
@RequestMapping("/user")
public class UserController {

    private final BookService bookService;
    private final MemberService memberService;
    private final IssueService issueService;
    private final ReservationService reservationService;
    private final FineService fineService;
    private final ContactService contactService;

    public UserController(BookService bookService, MemberService memberService, IssueService issueService,
                           ReservationService reservationService, FineService fineService,
                           ContactService contactService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.issueService = issueService;
        this.reservationService = reservationService;
        this.fineService = fineService;
        this.contactService = contactService;
    }

    private Member currentMember(HttpSession session) {
        Long id = (Long) session.getAttribute(SESSION_MEMBER_ID);
        return memberService.getById(id);
    }

    @GetMapping("/catalogue")
    public String catalogue(@RequestParam(required = false) String category,
                             @RequestParam(required = false) String q,
                             Model model) {
        List<Book> books;
        if (q != null && !q.isBlank()) {
            books = bookService.search(q);
        } else if (category != null && !category.isBlank()) {
            books = bookService.findByCategory(category);
        } else {
            books = bookService.findAll();
        }
        model.addAttribute("books", books);
        model.addAttribute("categories", bookService.distinctCategories());
        model.addAttribute("selectedCategory", category);
        model.addAttribute("query", q);
        return "user/catalogue";
    }

    @PostMapping("/issue/{bookId}")
    public String issue(@PathVariable Long bookId, HttpSession session, Model model) {
        Member member = currentMember(session);
        Book book = bookService.getById(bookId);
        try {
            issueService.issueBook(book, member);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return catalogue(null, null, model);
        }
        return "redirect:/user/my-books";
    }

    @GetMapping("/my-books")
    public String myBooks(HttpSession session, Model model) {
        Member member = currentMember(session);
        List<IssueRecord> records = issueService.findActiveForMember(member);
        model.addAttribute("records", records);
        model.addAttribute("today", java.time.LocalDate.now());
        return "user/my-books";
    }

    @PostMapping("/return/{issueId}")
    public String returnBook(@PathVariable Long issueId, HttpSession session, Model model) {
        IssueRecord record = issueService.getById(issueId);
        Member member = currentMember(session);
        if (!record.getMember().getId().equals(member.getId())) {
            model.addAttribute("error", "You can only return your own books.");
            return myBooks(session, model);
        }
        issueService.returnBook(record);
        return "redirect:/user/my-books";
    }

    @GetMapping("/reservations")
    public String reservations(HttpSession session, Model model) {
        Member member = currentMember(session);
        model.addAttribute("reservations", reservationService.findForMember(member));
        return "user/reservations";
    }

    @PostMapping("/reserve/{bookId}")
    public String reserve(@PathVariable Long bookId, HttpSession session, Model model) {
        Member member = currentMember(session);
        Book book = bookService.getById(bookId);
        try {
            reservationService.reserve(book, member);
        } catch (IllegalStateException e) {
            model.addAttribute("error", e.getMessage());
            return catalogue(null, null, model);
        }
        return "redirect:/user/reservations";
    }

    @PostMapping("/reservations/{id}/cancel")
    public String cancelReservation(@PathVariable Long id, HttpSession session, Model model) {
        Member member = currentMember(session);
        reservationService.cancel(id, member);
        return "redirect:/user/reservations";
    }

    @GetMapping("/fines")
    public String fines(HttpSession session, Model model) {
        Member member = currentMember(session);
        model.addAttribute("fines", fineService.findForMember(member));
        return "user/fines";
    }

    @GetMapping("/contact")
    public String contactForm() {
        return "user/contact";
    }

    @PostMapping("/contact")
    public String contactSubmit(@RequestParam String name, @RequestParam String email,
                                 @RequestParam String message, Model model) {
        contactService.submit(name, email, message);
        model.addAttribute("success", true);
        return "user/contact";
    }
}
