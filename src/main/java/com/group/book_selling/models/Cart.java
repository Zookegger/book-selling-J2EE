package com.group.book_selling.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

public class Cart implements Serializable {

    // Khóa: SKU (duy nhất cho mỗi BookFormat), không phải bookId
    private final Map<String, CartItem> items = new ConcurrentHashMap<>();

    /**
     * Thêm một định dạng vào giỏ hàng.
     *
     * @param book   thực thể Book cha
     * @param format định dạng BookFormat cụ thể được thêm
     * @param qty    số lượng mong muốn (bị bỏ qua cho sách điện tử/audiobook)
     */
    public void add(Book book, BookFormat format, int qty) {
        if (!format.isActive()) {
            throw new IllegalArgumentException("This format is currently unavailable.");
        }

        // Định dạng vật lý: kiểm tra tồn kho trước khi thêm
        if (format.getFormatType() == BookFormatType.PHYSICAL) {
            int stock = format.getStockQuantity();
            if (stock < 1) {
                throw new IllegalStateException("'" + book.getTitle() + "' is out of stock.");
            }
        }

        BigDecimal effectivePrice = resolvePrice(format);

        items.compute(format.getSku(), (sku, existing) -> {
            if (existing == null) {
                return new CartItem(
                        book.getId(),
                        format.getSku(),
                        buildDisplayTitle(book, format),
                        book.getCoverImage(),
                        format.getFormatType(),
                        effectivePrice,
                        format.getCurrency(),
                        qty);
            }

            // Kỹ thuật số: idempotent — đã có trong giỏ, không làm gì thêm
            if (existing.isDigital())
                return existing;

            // Vật lý: tăng số lượng, nhưng giới hạn theo tồn kho có sẵn
            int stock = format.getStockQuantity();
            int newQty = Math.min(existing.getQty() + qty, stock);
            existing.setQty(newQty);
            return existing;
        });
    }

    public void remove(String sku) {
        items.remove(sku);
    }

    public void updateQty(String sku, int newQty, BookFormat format) {
        items.computeIfPresent(sku, (k, item) -> {
            if (item.isDigital())
                return item;
            
            int stock = format.getStockQuantity();
            item.setQty(Math.min(newQty, stock));
            return item;
        });
    }

    public void clear() {
        items.clear();
    }

    public Collection<CartItem> getItems() {
        return items.values();
    }

    public Collection<CartItem> getPhysicalItems() {
        return items.values().stream()
                .filter(i -> i.getFormatType() == BookFormatType.PHYSICAL)
                .collect(Collectors.toList());
    }

    public Collection<CartItem> getDigitalItems() {
        return items.values().stream()
                .filter(CartItem::isDigital)
                .collect(Collectors.toList());
    }

    public int getTotalQuantity() {
        return items.values().stream().mapToInt(CartItem::getQty).sum();
    }

    /**
     * Tổng theo từng loại tiền. Với cửa hàng đa tiền tệ thực sự,
     * bạn nên chuẩn hóa về một loại tiền trước khi cộng tổng.
     */
    public BigDecimal getTotalPrice(String currency) {
        return items.values().stream()
                .filter(i -> currency.equalsIgnoreCase(i.getCurrency()))
                .map(CartItem::getSubtotal)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public boolean contains(String sku) {
        return items.containsKey(sku);
    }

    public int size() {
        return items.size();
    }

    private BigDecimal resolvePrice(BookFormat format) {
        BigDecimal discounted = format.getDiscountedPrice();
        return (discounted != null && discounted.compareTo(BigDecimal.ZERO) > 0)
                ? discounted
                : format.getPrice();
    }

    private String buildDisplayTitle(Book book, BookFormat format) {
        String label = switch (format.getFormatType()) {
            case PHYSICAL -> "Paperback";
            case DIGITAL -> "eBook";
            case AUDIOBOOK -> "Audiobook";
        };
        return book.getTitle() + " (" + label + ")";
    }
}