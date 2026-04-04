
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
    const elements = [];

    // Nếu phần tử gốc có thuộc tính data-price-value, thêm nó vào danh sách cần định dạng.
    if (root instanceof Element && root.hasAttribute('data-price-value')) {
        elements.push(root);
    }

    // Tìm tất cả phần tử con có thuộc tính data-price-value và thêm chúng vào danh sách cần định dạng.
    root.querySelectorAll('[data-price-value]').forEach(el => elements.push(el));

    // Áp dụng định dạng giá tiền cho tất cả phần tử trong danh sách.
    elements.forEach(element => {
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
 * Hàm initFormatSelectionModals sẽ khởi tạo các modal chọn định dạng sản phẩm. Nó sẽ đồng bộ hóa lựa chọn giữa các radio button và các trường input trong modal, đảm bảo rằng khi người dùng chọn một định dạng, thông tin SKU và số lượng sẽ được cập nhật tương ứng.
 * Ngoài ra, nó cũng sẽ thiết lập sự kiện để kiểm tra trước khi gửi form, đảm bảo rằng người dùng đã chọn một định dạng hợp lệ trước khi thêm sản phẩm vào giỏ hàng.
 * @returns {void}
 */
function initFormatSelectionModals() {
    const syncSelection = modal => {
        // Tìm form, input SKU, input số lượng và các radio button trong modal
        const form = modal.querySelector('form[data-format-selection-form="true"]');
        const skuInput = form ? form.querySelector('input[name="sku"]') : null;
        const quantityInput = modal.querySelector('input[name="qty"]');
        const radios = modal.querySelectorAll('input[name="formatChoice"]');

        if (!form || !skuInput || !quantityInput || radios.length === 0) {
            return null;
        }

        const selected = Array.from(radios).find(radio => radio.checked);
        if (!selected) {
            skuInput.value = '';
            quantityInput.value = 1;
            quantityInput.readOnly = true;
            return null;
        }

        skuInput.value = selected.dataset.sku || '';
        const isPhysical = selected.dataset.formatName === 'PHYSICAL';
        quantityInput.readOnly = !isPhysical;
        quantityInput.max = isPhysical ? '' : '1';
        quantityInput.value = isPhysical ? quantityInput.value || 1 : 1;
        return selected;
    };

    // Hàm getModal sẽ tìm phần tử modal chứa phần tử được truyền vào, nếu có.
    const getModal = element => element ? element.closest('.modal[data-format-selection="true"]') : null;

    document.addEventListener('change', event => {
        const radio = event.target.closest ? event.target.closest('input[name="formatChoice"]') : null;
        if (!radio) return;

        const modal = getModal(radio);
        if (!modal) return;

        syncSelection(modal);
    });

    document.addEventListener('submit', event => {
        const form = event.target instanceof HTMLFormElement && event.target.matches('form[data-format-selection-form="true"]')
            ? event.target
            : null;
        if (!form) return;

        const modal = getModal(form);
        if (!modal) return;

        event.preventDefault();
        syncSelection(modal);

        const skuInput = form.querySelector('input[name="sku"]');
        if (!skuInput || !skuInput.value || !skuInput.value.trim()) {
            alert('Vui lòng chọn phiên bản trước khi thêm vào giỏ.');
            return;
        }

        form.submit();
    });

    // Đồng bộ hóa lựa chọn khi modal được hiển thị và ngay cả khi nó đã được hiển thị (để xử lý trường hợp modal được mở lại).
    document.querySelectorAll('.modal[data-format-selection="true"]').forEach(modal => {
        modal.addEventListener('shown.bs.modal', () => syncSelection(modal));
        syncSelection(modal);
    });
}

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
        initFormatSelectionModals();
    });
} else {
    applyPriceFormatting();
    initPriceObserver();
    initFormatSelectionModals();
}