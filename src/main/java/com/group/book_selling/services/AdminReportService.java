package com.group.book_selling.services;

import java.math.BigDecimal;
import java.time.DayOfWeek;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.YearMonth;
import java.time.format.DateTimeFormatter;
import java.time.temporal.TemporalAdjusters;
import java.util.ArrayList;
import java.util.Comparator;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;

import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import com.group.book_selling.models.Book;
import com.group.book_selling.models.BookFormat;
import com.group.book_selling.models.BookFormatType;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderItem;
import com.group.book_selling.models.OrderStatus;
import com.group.book_selling.repositories.IBookRepository;
import com.group.book_selling.repositories.IOrderRepository;

import lombok.RequiredArgsConstructor;

@Service
@RequiredArgsConstructor
public class AdminReportService {

    private static final int TOP_LIMIT = 5;
    private static final DateTimeFormatter DATE_FMT = DateTimeFormatter.ofPattern("dd/MM/yyyy");
    private static final DateTimeFormatter MONTH_FMT = DateTimeFormatter.ofPattern("MM/yyyy");

    private final IOrderRepository orderRepository;
    private final IBookRepository bookRepository;

    @Transactional(readOnly = true)
    public ReportSnapshot buildReport(LocalDate fromDate, LocalDate toDate) {
        LocalDate from = fromDate == null ? LocalDate.now().minusDays(29) : fromDate;
        LocalDate to = toDate == null ? LocalDate.now() : toDate;

        if (to.isBefore(from)) {
            LocalDate temp = from;
            from = to;
            to = temp;
        }

        LocalDateTime fromDateTime = from.atStartOfDay();
        LocalDateTime toDateTime = to.plusDays(1).atStartOfDay();

        List<Order> completedOrders = orderRepository.findByOrderStatusAndPlacedAtBetween(
                OrderStatus.COMPLETED,
                fromDateTime,
                toDateTime);

        List<Book> allBooks = bookRepository.findAll();
        Map<Long, Book> bookById = allBooks.stream()
                .filter(book -> book.getId() != null)
                .collect(LinkedHashMap::new, (map, book) -> map.put(book.getId(), book), LinkedHashMap::putAll);

        Map<String, Long> categoryQtyMap = new HashMap<>();
        Map<Long, Integer> soldQtyByBookId = new HashMap<>();
        Map<Long, BigDecimal> soldRevenueByBookId = new HashMap<>();

        Map<LocalDate, BigDecimal> dailyRevenueMap = new HashMap<>();
        Map<LocalDate, BigDecimal> weeklyRevenueMap = new HashMap<>();
        Map<YearMonth, BigDecimal> monthlyRevenueMap = new HashMap<>();

        long totalItemsSold = 0;
        BigDecimal totalRevenue = BigDecimal.ZERO;

        for (Order order : completedOrders) {
            if (order.getGrandTotal() != null) {
                totalRevenue = totalRevenue.add(order.getGrandTotal());
            }

            LocalDate orderDate = (order.getPlacedAt() != null ? order.getPlacedAt() : order.getCreatedAt()).toLocalDate();
            BigDecimal orderRevenue = order.getGrandTotal() == null ? BigDecimal.ZERO : order.getGrandTotal();
            dailyRevenueMap.merge(orderDate, orderRevenue, BigDecimal::add);

            LocalDate weekStart = orderDate.with(TemporalAdjusters.previousOrSame(DayOfWeek.MONDAY));
            weeklyRevenueMap.merge(weekStart, orderRevenue, BigDecimal::add);

            YearMonth month = YearMonth.from(orderDate);
            monthlyRevenueMap.merge(month, orderRevenue, BigDecimal::add);

            for (OrderItem item : order.getItems()) {
                totalItemsSold += item.getQuantity();
                if (item.getBookId() != null) {
                    Integer currentQty = soldQtyByBookId.getOrDefault(item.getBookId(), 0);
                    soldQtyByBookId.put(item.getBookId(), currentQty + item.getQuantity());
                }

                if (item.getBookId() != null) {
                    BigDecimal lineRevenue = item.getGrandTotal() == null ? BigDecimal.ZERO : item.getGrandTotal();
                    BigDecimal currentRevenue = soldRevenueByBookId.getOrDefault(item.getBookId(), BigDecimal.ZERO);
                    soldRevenueByBookId.put(item.getBookId(), currentRevenue.add(lineRevenue));
                }

                Book soldBook = bookById.get(item.getBookId());
                if (soldBook != null && soldBook.getCategories() != null && !soldBook.getCategories().isEmpty()) {
                    for (Category category : soldBook.getCategories()) {
                        String categoryName = (category.getName() == null || category.getName().isBlank())
                                ? "Chưa phân loại"
                                : category.getName();
                        long currentQty = categoryQtyMap.getOrDefault(categoryName, 0L);
                        categoryQtyMap.put(categoryName, currentQty + item.getQuantity());
                    }
                } else {
                    long currentQty = categoryQtyMap.getOrDefault("Chưa phân loại", 0L);
                    categoryQtyMap.put("Chưa phân loại", currentQty + item.getQuantity());
                }
            }
        }

        List<CategorySalesRow> categorySales = categoryQtyMap.entrySet().stream()
                .map(entry -> new CategorySalesRow(entry.getKey(), entry.getValue()))
                .sorted(Comparator.comparingLong(CategorySalesRow::quantitySold).reversed())
                .toList();

        List<InventoryRow> inventoryRows = allBooks.stream()
                .map(this::toInventoryRow)
                .sorted(Comparator.comparingInt(InventoryRow::stockQuantity)
                        .thenComparing(InventoryRow::bookTitle, String.CASE_INSENSITIVE_ORDER))
                .toList();

        long outOfStockCount = inventoryRows.stream().filter(row -> row.stockQuantity() == 0).count();
        long lowStockCount = inventoryRows.stream().filter(row -> row.stockQuantity() > 0 && row.stockQuantity() <= 5).count();

        List<BookSalesRow> rankingRows = new ArrayList<>();
        for (Book book : allBooks) {
            int soldQty = soldQtyByBookId.getOrDefault(book.getId(), 0);
            BigDecimal revenue = soldRevenueByBookId.getOrDefault(book.getId(), BigDecimal.ZERO);
            rankingRows.add(new BookSalesRow(book.getId(), book.getTitle(), soldQty, revenue));
        }

        Comparator<BookSalesRow> bySalesDesc = Comparator
                .comparingInt(BookSalesRow::quantitySold)
                .thenComparing(BookSalesRow::revenue)
                .reversed();
        Comparator<BookSalesRow> bySalesAsc = Comparator
                .comparingInt(BookSalesRow::quantitySold)
                .thenComparing(BookSalesRow::revenue)
                .thenComparing(BookSalesRow::bookTitle, String.CASE_INSENSITIVE_ORDER);

        List<BookSalesRow> bestSellers = rankingRows.stream()
                .sorted(bySalesDesc)
                .limit(TOP_LIMIT)
                .toList();

        List<BookSalesRow> lowSellers = rankingRows.stream()
                .sorted(bySalesAsc)
                .limit(TOP_LIMIT)
                .toList();

        List<RevenuePoint> dailyRevenue = dailyRevenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RevenuePoint(DATE_FMT.format(entry.getKey()), entry.getValue()))
                .toList();

        List<RevenuePoint> weeklyRevenue = weeklyRevenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> {
                    LocalDate weekStart = entry.getKey();
                    LocalDate weekEnd = weekStart.plusDays(6);
                    String label = DATE_FMT.format(weekStart) + " - " + DATE_FMT.format(weekEnd);
                    return new RevenuePoint(label, entry.getValue());
                })
                .toList();

        List<RevenuePoint> monthlyRevenue = monthlyRevenueMap.entrySet().stream()
                .sorted(Map.Entry.comparingByKey())
                .map(entry -> new RevenuePoint(MONTH_FMT.format(entry.getKey()), entry.getValue()))
                .toList();

        Summary summary = new Summary(
                completedOrders.size(),
                totalItemsSold,
                totalRevenue,
                allBooks.size(),
                outOfStockCount,
                lowStockCount);

        return new ReportSnapshot(
                from,
                to,
                summary,
                categorySales,
                inventoryRows,
                bestSellers,
                lowSellers,
                dailyRevenue,
                weeklyRevenue,
                monthlyRevenue);
    }

    private InventoryRow toInventoryRow(Book book) {
        int stock = 0;
        int physicalFormats = 0;

        for (BookFormat format : book.getFormats()) {
            if (format == null || format.getFormatType() != BookFormatType.PHYSICAL) {
                continue;
            }
            physicalFormats++;
            Integer formatStock = format.getStockQuantity();
            stock += formatStock == null ? 0 : formatStock.intValue();
        }

        String inventoryStatus;
        if (physicalFormats == 0) {
            inventoryStatus = "Không áp dụng";
        } else if (stock <= 0) {
            inventoryStatus = "Hết hàng";
        } else if (stock <= 5) {
            inventoryStatus = "Sắp hết";
        } else {
            inventoryStatus = "Còn hàng";
        }

        String title = (book.getTitle() == null || book.getTitle().isBlank()) ? "(Không tên)" : book.getTitle();
        return new InventoryRow(book.getId(), title, stock, inventoryStatus);
    }

    public record ReportSnapshot(
            LocalDate fromDate,
            LocalDate toDate,
            Summary summary,
            List<CategorySalesRow> categorySales,
            List<InventoryRow> inventory,
            List<BookSalesRow> bestSellers,
            List<BookSalesRow> lowSellers,
            List<RevenuePoint> dailyRevenue,
            List<RevenuePoint> weeklyRevenue,
            List<RevenuePoint> monthlyRevenue) {
    }

    public record Summary(
            int completedOrderCount,
            long totalItemsSold,
            BigDecimal totalRevenue,
            int totalBooks,
            long outOfStockCount,
            long lowStockCount) {
    }

    public record CategorySalesRow(String categoryName, long quantitySold) {
    }

    public record InventoryRow(Long bookId, String bookTitle, int stockQuantity, String inventoryStatus) {
    }

    public record BookSalesRow(Long bookId, String bookTitle, int quantitySold, BigDecimal revenue) {
    }

    public record RevenuePoint(String label, BigDecimal revenue) {
    }
}
