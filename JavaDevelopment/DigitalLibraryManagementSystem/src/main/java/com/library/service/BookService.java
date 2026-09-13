package com.library.service;

import com.library.model.Book;
import com.library.repository.BookRepository;
import org.springframework.stereotype.Service;

import java.util.List;

@Service
public class BookService {

    private final BookRepository bookRepository;

    public BookService(BookRepository bookRepository) {
        this.bookRepository = bookRepository;
    }

    public List<Book> findAll() {
        return bookRepository.findAllByOrderByCategoryAscTitleAsc();
    }

    public List<Book> findByCategory(String category) {
        return bookRepository.findByCategoryIgnoreCase(category);
    }

    public List<Book> search(String query) {
        return bookRepository.findByTitleContainingIgnoreCaseOrAuthorContainingIgnoreCase(query, query);
    }

    public Book getById(Long id) {
        return bookRepository.findById(id)
                .orElseThrow(() -> new IllegalArgumentException("Book not found: " + id));
    }

    public Book addBook(Book book) {
        book.setAvailableQuantity(book.getQuantity());
        return bookRepository.save(book);
    }

    /**
     * Edits a book's details. If the total quantity is increased, the extra
     * copies are added to the available shelf count; if decreased, the
     * available count is reduced but never below zero.
     */
    public Book updateBook(Long id, String title, String author, String isbn, String category, int newQuantity) {
        Book book = getById(id);
        int delta = newQuantity - book.getQuantity();
        book.setTitle(title);
        book.setAuthor(author);
        book.setIsbn(isbn);
        book.setCategory(category);
        book.setQuantity(newQuantity);
        int newAvailable = book.getAvailableQuantity() + delta;
        book.setAvailableQuantity(Math.max(0, newAvailable));
        return bookRepository.save(book);
    }

    public void deleteBook(Long id) {
        bookRepository.deleteById(id);
    }

    public List<String> distinctCategories() {
        return bookRepository.findAll().stream()
                .map(Book::getCategory)
                .distinct()
                .sorted()
                .toList();
    }
}
