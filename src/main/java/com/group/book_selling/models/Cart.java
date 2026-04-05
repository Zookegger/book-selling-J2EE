package com.group.book_selling.models;

import java.io.Serializable;
import java.math.BigDecimal;
import java.util.Collection;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.group.book_selling.utils.BookVatPolicy;

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
        BigDecimal vatRate = BookVatPolicy.resolveVatRate(book);

        items.compute(format.getSku(), (sku, existing) -> {
            if (existing == null) {
                return new CartItem(
                        book.getId(),
                        format.getSku(),
                        book.getTitle(),
                        book.getSlug(),
                        book.getCoverImage(),
                        format.getFormatType(),
                        effectivePrice,
                        vatRate,
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

    /**
     * Cập nhật số lượng của một mặt hàng trong giỏ hàng. Đối với sách vật lý, số
     * lượng sẽ được giới hạn bởi tồn kho hiện có. Đối với sách điện tử/audiobook,
     * số
     * 
     * @param sku    SKU của mặt hàng cần cập nhật
     * @param newQty số lượng mới mong muốn (bị bỏ qua cho sách điện tử/audiobook)
     * @param format thực thể BookFormat tương ứng, được sử dụng để kiểm tra tồn kho
     *               nếu là sách vật lý
     */
    public void updateQty(String sku, int newQty, BookFormat format) {
        items.computeIfPresent(sku, (k, item) -> {
            if (item.isDigital())
                return item;

            int stock = format.getStockQuantity();
            item.setQty(Math.min(newQty, stock));
            return item;
        });
    }

    /**
     * Xóa tất cả các mặt hàng khỏi giỏ hàng.
     */
    public void clear() {
        items.clear();
    }

    /**
     * Kiểm tra xem giỏ hàng đã chứa một SKU cụ thể hay chưa. Điều này hữu ích để
     * tránh thêm trùng lặp cho sách điện tử/audiobook, hoặc để hiển thị trạng thái
     * "Đã thêm vào giỏ" trên trang chi tiết sản phẩm.
     */
    public boolean contains(String sku) {
        return items.containsKey(sku);
    }

    /**
     * Trả về số mặt hàng phân biệt trong giỏ hàng (số SKU/số loại sản phẩm khác nhau), không phải tổng số lượng của tất cả sản phẩm.
     * 
     * @return Số mặt hàng phân biệt trong giỏ hàng
     */
    public int size() {
        return items.size();
    }

    /**
     * Kiểm tra xem giỏ hàng có trống hay không. Một giỏ hàng được coi là trống nếu
     * không có mặt hàng nào trong đó.
     * 
     * @return true nếu giỏ hàng trống, false nếu có ít nhất một mặt hàng
     */
    public boolean isEmpty() {
        return items.isEmpty();
    }

    /**
     * Trả về danh sách tất cả các mặt hàng trong giỏ hàng.
     * 
     * @return Danh sách các mặt hàng trong giỏ hàng
     */
    public Collection<CartItem> getItems() {
        return items.values();
    }

    /**
     * Trả về danh sách các mặt hàng vật lý trong giỏ hàng. Điều này hữu ích để hiển
     * thị riêng biệt hoặc tính toán phí vận chuyển.
     *
     * @return Danh sách các mặt hàng vật lý trong giỏ hàng
     */
    public Collection<CartItem> getPhysicalItems() {
        return items.values().stream()
                .filter(i -> i.getFormatType() == BookFormatType.PHYSICAL)
                .collect(Collectors.toList());
    }

    /**
     * Trả về danh sách các mặt hàng kỹ thuật số trong giỏ hàng (eBook và
     * Audiobook). Điều này hữu ích để hiển thị riêng biệt hoặc áp dụng các quy tắc
     * kinh doanh khác nhau
     * 
     * @return Danh sách các mặt hàng kỹ thuật số trong giỏ hàng
     */
    public Collection<CartItem> getDigitalItems() {
        return items.values().stream()
                .filter(i -> i.getFormatType() == BookFormatType.DIGITAL
                        || i.getFormatType() == BookFormatType.AUDIOBOOK)
                .collect(Collectors.toList());
    }

    /**
     * Tính tổng số lượng của tất cả các mặt hàng trong giỏ hàng. Điều này hữu ích
     * để hiển thị tổng số mặt hàng hoặc để tính toán tổng giá trị đơn hàng.
     * 
     * @return Tổng số lượng của tất cả các mặt hàng trong giỏ hàng
     */
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

    public BigDecimal getTotalTax(String currency) {
        return items.values().stream()
                .filter(i -> currency.equalsIgnoreCase(i.getCurrency()))
                .map(CartItem::getTaxAmount)
                .reduce(BigDecimal.ZERO, BigDecimal::add);
    }

    public BigDecimal getGrandTotalPrice(String currency) {
        return getTotalPrice(currency).add(getTotalTax(currency));
    }

    /**
     * Xác định giá hiệu quả của một định dạng, ưu tiên giá đã giảm nếu có. Điều này
     * đảm bảo rằng khách hàng luôn được tính giá tốt nhất mà bạn cung cấp, và giúp
     * đơn giản hóa logic trong CartService.
     * 
     * @param format định dạng sách cần tính giá
     * @return Giá hiệu quả của định dạng, đã áp dụng giảm giá nếu có, hoặc giá gốc
     *         nếu không có giảm giá nào được áp dụng
     */
    private BigDecimal resolvePrice(BookFormat format) {
        BigDecimal discounted = format.getDiscountedPrice();
        return (discounted != null && discounted.compareTo(BigDecimal.ZERO) > 0)
                ? discounted
                : format.getPrice();
    }
}