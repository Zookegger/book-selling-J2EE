
/**
 * Hàm debounce giúp hạn chế tần suất gọi một hàm trong một khoảng thời gian nhất định. Điều này rất hữu ích khi bạn muốn tối ưu hiệu suất của các sự kiện như nhập liệu, cuộn trang, hoặc thay đổi kích thước cửa sổ.
 * @param {Function} func - Hàm cần được debounce.
 * @param {number} wait - Khoảng thời gian chờ (tính bằng milliseconds) trước khi gọi hàm sau lần cuối cùng được kích hoạt.
 * @returns {Function} - Một hàm mới đã được debounce.
 * @example
 * const onSearch = debounce((keyword) => {
 *   console.log('Searching:', keyword);
 * }, 300);
 *
 * onSearch('ha');
 * onSearch('har');
 * onSearch('harry');
 * // Chỉ lần gọi cuối cùng được chạy sau 300ms.
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
 * @example
 * formatPrice(120000);
 * // => "120.000 ₫"
 * @example
 * formatPrice('abc');
 * // => ""
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
 * @example
 * // HTML: <span data-price-value="150000" data-price-quantity="2"></span>
 * applyPriceFormatting();
 * // Kết quả: "300.000 ₫"
 * @example
 * const linePrice = document.querySelector('#line-price');
 * applyPriceFormatting(linePrice);
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

const locationApiBase = 'https://provinces.open-api.vn/api';
const provinceCache = new Map();
const districtCache = new Map();
const wardCache = new Map();

/**
 * Kiểm tra một giá trị có phải mã địa giới dạng số hay không.
 * @param {unknown} value - Giá trị cần kiểm tra.
 * @returns {boolean} `true` nếu là chuỗi toàn số.
 * @example
 * isLocationCode('79');
 * // => true
 * @example
 * isLocationCode('Ho Chi Minh');
 * // => false
 */
function isLocationCode(value) {
    return typeof value === 'string' && /^\d+$/.test(value.trim());
}

/**
 * Lấy tên địa giới theo loại và mã từ open-api.vn, có cache theo từng loại.
 * @param {'province'|'district'|'ward'} type - Loại địa giới.
 * @param {string} code - Mã địa giới cần resolve.
 * @returns {Promise<string>} Tên địa giới hoặc giá trị gốc nếu không resolve được.
 * @example
 * const provinceName = await fetchLocationName('province', '79');
 * // => 'Thành phố Hồ Chí Minh'
 * @example
 * const wardName = await fetchLocationName('ward', '27448');
 * // => 'Phường ...'
 */
async function fetchLocationName(type, code) {
    if (!isLocationCode(code)) {
        return code || '';
    }

    const normalizedCode = code.trim();
    const cache = type === 'province' ? provinceCache : type === 'district' ? districtCache : wardCache;
    const endpoint = type === 'province' ? 'p' : type === 'district' ? 'd' : 'w';

    if (cache.has(normalizedCode)) {
        return cache.get(normalizedCode);
    }

    try {
        const response = await fetch(`${locationApiBase}/${endpoint}/${normalizedCode}`);
        const data = await response.json();
        const name = data && data.name ? data.name : normalizedCode;
        cache.set(normalizedCode, name);
        return name;
    } catch (_) {
        return normalizedCode;
    }
}

/**
 * Nạp danh sách tỉnh/thành phố vào một thẻ `select`.
 * @param {string} selectId - Id của phần tử `select`.
 * @param {string|null} [selectedValue=null] - Mã đang được chọn trước đó.
 * @returns {Promise<void>}
 * @example
 * await loadProvinces('newProvinceOrCity');
 * @example
 * await loadProvinces('editProvinceOrCity', '79');
 */
async function loadProvinces(selectId, selectedValue = null) {
    const select = document.getElementById(selectId);
    if (!select) {
        return;
    }

    const response = await fetch(`${locationApiBase}/p/`);
    const provinces = await response.json();

    select.innerHTML = '<option value="">-- Chọn tỉnh/thành phố --</option>';
    provinces.forEach(province => {
        const option = document.createElement('option');
        option.value = String(province.code);
        option.textContent = province.name;
        if (selectedValue !== null && String(selectedValue) === String(province.code)) {
            option.selected = true;
        }
        select.appendChild(option);
    });
}

/**
 * Nạp danh sách quận/huyện theo mã tỉnh vào một thẻ `select`.
 * @param {string} selectId - Id của phần tử `select` quận/huyện.
 * @param {string} provinceCode - Mã tỉnh/thành phố.
 * @param {string|null} [selectedDistrict=null] - Mã quận/huyện chọn sẵn.
 * @returns {Promise<void>}
 * @example
 * await loadDistricts('newDistrict', '79');
 * @example
 * await loadDistricts('editDistrict', '79', '777');
 */
async function loadDistricts(selectId, provinceCode, selectedDistrict = null) {
    const districtSelect = document.getElementById(selectId);
    if (!districtSelect) {
        return;
    }

    districtSelect.innerHTML = '<option value="">-- Chọn quận/huyện --</option>';
    if (!provinceCode) {
        return;
    }

    const response = await fetch(`${locationApiBase}/p/${provinceCode}?depth=2`);
    const province = await response.json();
    const districts = province && province.districts ? province.districts : [];

    districts.forEach(district => {
        const option = document.createElement('option');
        option.value = String(district.code);
        option.textContent = district.name;
        if (selectedDistrict !== null && String(selectedDistrict) === String(district.code)) {
            option.selected = true;
        }
        districtSelect.appendChild(option);
    });
}

/**
 * Nạp danh sách phường/xã theo mã quận/huyện vào một thẻ `select`.
 * @param {string} selectId - Id của phần tử `select` phường/xã.
 * @param {string} districtCode - Mã quận/huyện.
 * @param {string|null} [selectedWard=null] - Mã phường/xã chọn sẵn.
 * @returns {Promise<void>}
 * @example
 * await loadWards('newWard', '777');
 * @example
 * await loadWards('editWard', '777', '27448');
 */
async function loadWards(selectId, districtCode, selectedWard = null) {
    const wardSelect = document.getElementById(selectId);
    if (!wardSelect) {
        return;
    }

    wardSelect.innerHTML = '<option value="">-- Chọn phường/xã --</option>';
    if (!districtCode) {
        return;
    }

    const response = await fetch(`${locationApiBase}/d/${districtCode}?depth=2`);
    const district = await response.json();
    const wards = district && district.wards ? district.wards : [];

    wards.forEach(ward => {
        const option = document.createElement('option');
        option.value = String(ward.code);
        option.textContent = ward.name;
        if (selectedWard !== null && String(selectedWard) === String(ward.code)) {
            option.selected = true;
        }
        wardSelect.appendChild(option);
    });
}

/**
 * Resolve và render dòng địa chỉ hiển thị từ các `data-*` chứa mã địa giới.
 * Yêu cầu element có các thuộc tính: `data-street`, `data-ward-code`,
 * `data-district-code`, `data-province-code`.
 * @param {HTMLElement} element - Phần tử dòng địa chỉ cần cập nhật text.
 * @returns {Promise<void>}
 * @example
 * const line = document.querySelector('.address-display');
 * await resolveAddressDisplayLine(line);
 */
async function resolveAddressDisplayLine(element) {
    if (!element) {
        return;
    }

    const street = element.dataset.street || '';
    const wardCode = element.dataset.wardCode || '';
    const districtCode = element.dataset.districtCode || '';
    const provinceCode = element.dataset.provinceCode || '';

    const [wardName, districtName, provinceName] = await Promise.all([
        fetchLocationName('ward', wardCode),
        fetchLocationName('district', districtCode),
        fetchLocationName('province', provinceCode)
    ]);

    element.textContent = [street, wardName, districtName, provinceName]
        .filter(part => part && String(part).trim().length > 0)
        .join(', ');
}

window.locationUtils = {
    loadProvinces,
    loadDistricts,
    loadWards,
    resolveAddressDisplayLine,
    fetchLocationName
};

/**
 * Hàm initFormatSelectionModals sẽ khởi tạo các modal chọn định dạng sản phẩm. Nó sẽ đồng bộ hóa lựa chọn giữa các radio button và các trường input trong modal, đảm bảo rằng khi người dùng chọn một định dạng, thông tin SKU và số lượng sẽ được cập nhật tương ứng.
 * Ngoài ra, nó cũng sẽ thiết lập sự kiện để kiểm tra trước khi gửi form, đảm bảo rằng người dùng đã chọn một định dạng hợp lệ trước khi thêm sản phẩm vào giỏ hàng.
 * @returns {void}
 * @example
 * // Gọi một lần sau khi DOM sẵn sàng
 * initFormatSelectionModals();
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
 * @example
 * initPriceObserver();
 * // Khi có node mới chứa data-price-value, giá sẽ tự được format.
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