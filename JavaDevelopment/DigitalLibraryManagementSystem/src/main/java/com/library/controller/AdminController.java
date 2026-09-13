package com.library.controller;

import com.library.model.Book;
import com.library.model.IssueRecord;
import com.library.service.*;
import org.springframework.stereotype.Controller;
import org.springframework.ui.Model;
import org.springframework.web.bind.annotation.*;

@Controller
@RequestMapping("/admin")
public class AdminController {

    private final BookService bookService;
    private final MemberService memberService;
    private final IssueService issueService;
    private final FineService fineService;
    private final ContactService contactService;

    public AdminController(BookService bookService, MemberService memberService, IssueService issueService,
                            FineService fineService, ContactService contactService) {
        this.bookService = bookService;
        this.memberService = memberService;
        this.issueService = issueService;
        this.fineService = fineService;
        this.contactService = contactService;
    }

    @GetMapping("/dashboard")
    public String dashboard(Model model) {
        model.addAttribute("bookCount", bookService.findAll().size());
        model.addAttribute("issuedCount", issueService.findAllCurrentlyIssued().size());
        model.addAttribute("memberCount", memberService.findAllUsers().size());
        model.addAttribute("unpaidFineCount", fineService.findAll().stream().filter(f -> !f.isPaid()).count());
        model.addAttribute("unresolvedMessages", contactService.findAll().stream().filter(m -> !m.isResolved()).count());
        return "admin/dashboard";
    }

    // ---------- Books ----------

    @GetMapping("/books")
    public String books(Model model) {
        model.addAttribute("books", bookService.findAll());
        model.addAttribute("newBook", new Book());
        return "admin/books";
    }

    @PostMapping("/books/add")
    public String addBook(@RequestParam String title, @RequestParam String author,
                           @RequestParam String isbn, @RequestParam String category,
                           @RequestParam int quantity, Model model) {
        try {
            bookService.addBook(new Book(title, author, isbn, category, quantity));
        } catch (Exception e) {
            model.addAttribute("error", "Could not add book: " + e.getMessage());
            return books(model);
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/edit")
    public String editBook(@PathVariable Long id, @RequestParam String title, @RequestParam String author,
                            @RequestParam String isbn, @RequestParam String category,
                            @RequestParam int quantity, Model model) {
        try {
            bookService.updateBook(id, title, author, isbn, category, quantity);
        } catch (Exception e) {
            model.addAttribute("error", "Could not update book: " + e.getMessage());
            return books(model);
        }
        return "redirect:/admin/books";
    }

    @PostMapping("/books/{id}/delete")
    public String deleteBook(@PathVariable Long id) {
        bookService.deleteBook(id);
        return "redirect:/admin/books";
    }

    // ---------- Issued books ----------

    @GetMapping("/issued")
    public String issued(Model model) {
        model.addAttribute("records", issueService.findAllCurrentlyIssued());
        model.addAttribute("today", java.time.LocalDate.now());
        return "admin/issued";
    }

    // ---------- Members ----------

    @GetMapping("/members")
    public String members(Model model) {
        model.addAttribute("members", memberService.findAllUsers());
        return "admin/members";
    }

    @PostMapping("/members/{id}/delete")
    public String deleteMember(@PathVariable Long id) {
        memberService.deleteUser(id);
        return "redirect:/admin/members";
    }

    // ---------- Fines ----------

    @GetMapping("/fines")
    public String fines(Model model) {
        model.addAttribute("fines", fineService.findAll());
        return "admin/fines";
    }

    @PostMapping("/fines/{id}/mark-paid")
    public String markFinePaid(@PathVariable Long id) {
        fineService.markPaid(id);
        return "redirect:/admin/fines";
    }

    // ---------- Contact messages ----------

    @GetMapping("/messages")
    public String messages(Model model) {
        model.addAttribute("messages", contactService.findAll());
        return "admin/messages";
    }

    @PostMapping("/messages/{id}/resolve")
    public String resolveMessage(@PathVariable Long id) {
        contactService.markResolved(id);
        return "redirect:/admin/messages";
    }
}
