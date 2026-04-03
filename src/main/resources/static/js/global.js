
/**
 * Hàm debounce giúp hạn chế tần suất gọi một hàm trong một khoảng thời gian nhất định. Điều này rất hữu ích khi bạn muốn tối ưu hiệu suất của các sự kiện như nhập liệu, cuộn trang, hoặc thay đổi kích thước cửa sổ.
 * @param {Function} func - Hàm cần được debounce.
 * @param {number} wait - Khoảng thời gian chờ (tính bằng milliseconds) trước khi gọi hàm sau lần cuối cùng được kích hoạt.
 * @returns {Function} - Một hàm mới đã được debounce.
 */
function debounce(func, wait) {
    let timeout;
    return function executedFunction(...args) {
        const later = () => {
            clearTimeout(timeout);
            func(...args);
        };
        clearTimeout(timeout);
        timeout = setTimeout(later, wait);
    };
}

/**
 * Định dạng giá tiền theo chuẩn Việt Nam (VND) sử dụng Intl.NumberFormat. 
 */
const priceFormatter = new Intl.NumberFormat('vi-VN', {
    style: 'currency',
    currency: 'VND',
});

/**
 * Hàm formatPrice sẽ nhận một giá trị và trả về chuỗi đã được định dạng. Hàm applyPriceFormatting sẽ tìm tất cả các phần tử có thuộc tính data-price-value và áp dụng định dạng cho chúng, nhân với quantity nếu có.
 * @param {string|number} value - Giá trị cần định dạng.
 * @returns {string} - Chuỗi đã được định dạng hoặc rỗng nếu giá trị không hợp lệ.
 */
function formatPrice(value) {
    const amount = Number(value);
    if (!Number.isFinite(amount)) {
        return '';
    }

    return priceFormatter.format(amount);
}

/**
 * Áp dụng định dạng giá tiền cho các phần tử trong cây DOM.
 * @param {HTMLElement} root - Phần tử gốc để tìm kiếm các phần tử có thuộc tính data-price-value.
 */
function applyPriceFormatting(root = document) {
    root.querySelectorAll('[data-price-value]').forEach(element => {
        const rawValue = element.dataset.priceValue;
        const quantity = element.dataset.priceQuantity ? Number(element.dataset.priceQuantity) : 1;
        const amount = Number(rawValue);

        if (!Number.isFinite(amount)) {
            return;
        }

        element.textContent = formatPrice(amount * quantity);
    });
}

window.formatPrice = formatPrice;
window.applyPriceFormatting = applyPriceFormatting;

/**
 * Khởi tạo observer để theo dõi các thay đổi trong DOM và áp dụng định dạng giá tiền cho các phần tử mới được thêm vào.
 * @returns {MutationObserver} - Observer đã được khởi tạo.
 */
function initPriceObserver() {
    if (typeof MutationObserver === 'undefined') return;
    try {
        const observer = new MutationObserver(mutations => {
            for (const m of mutations) {
                if (m.type === 'childList') {
                    m.addedNodes.forEach(node => {
                        if (!(node instanceof Element)) return;
                        if (node.matches && node.matches('[data-price-value]')) {
                            applyPriceFormatting(node);
                        }
                        if (node.querySelectorAll) {
                            node.querySelectorAll('[data-price-value]').forEach(el => applyPriceFormatting(el));
                        }
                    });
                } else if (m.type === 'attributes' && m.target && m.target.matches && m.target.matches('[data-price-value]')) {
                    applyPriceFormatting(m.target);
                }
            }
        });

        observer.observe(document.body, { childList: true, subtree: true, attributes: true, attributeFilter: ['data-price-value', 'data-price-quantity'] });
        window.priceObserver = observer;
    } catch (e) {
        // ignore observer errors on older browsers
    }
}

if (document.readyState === 'loading') {
    document.addEventListener('DOMContentLoaded', () => {
        applyPriceFormatting();
        initPriceObserver();
    });
} else {
    applyPriceFormatting();
    initPriceObserver();
}