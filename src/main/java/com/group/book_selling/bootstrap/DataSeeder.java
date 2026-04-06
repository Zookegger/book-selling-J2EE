package com.group.book_selling.bootstrap;

import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.boot.CommandLineRunner;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

import com.group.book_selling.controllers.BookRequest;
import com.group.book_selling.models.Author;
import com.group.book_selling.models.Book;
import com.group.book_selling.models.BookFormat;
import com.group.book_selling.models.BookFormatType;
import com.group.book_selling.models.Category;
import com.group.book_selling.models.DigitalFileFormat;
import com.group.book_selling.models.Order;
import com.group.book_selling.models.OrderItem;
import com.group.book_selling.models.OrderStatus;
import com.group.book_selling.models.Payment;
import com.group.book_selling.models.PaymentMethod;
import com.group.book_selling.models.PaymentStatus;
import com.group.book_selling.models.Publisher;
import com.group.book_selling.models.PublisherLocation;
import com.group.book_selling.models.User;
import com.group.book_selling.models.User.Address;
import com.group.book_selling.models.UserRole;
import com.group.book_selling.repositories.IAuthorRepository;
import com.group.book_selling.repositories.IBookRepository;
import com.group.book_selling.repositories.ICategoryRepository;
import com.group.book_selling.repositories.IOrderRepository;
import com.group.book_selling.repositories.IPublisherRepository;
import com.group.book_selling.services.AuthorService;
import com.group.book_selling.services.BookService;
import com.group.book_selling.services.CategoryService;
import com.group.book_selling.services.PublisherService;
import com.group.book_selling.services.UserServices;
import com.group.book_selling.utils.BookVatPolicy;

import lombok.RequiredArgsConstructor;

/**
 * Startup seeder that syncs realistic sample data into an empty or partially
 * populated database.
 */
@Component
@RequiredArgsConstructor
public class DataSeeder implements CommandLineRunner {

	private static final Logger log = LoggerFactory.getLogger(DataSeeder.class);

	private final AuthorService authorService;
	private final CategoryService categoryService;
	private final PublisherService publisherService;
	private final BookService bookService;
	private final UserServices userService;
	private final IPublisherRepository publisherRepository;
	private final IAuthorRepository authorRepository;
	private final ICategoryRepository categoryRepository;
	private final IBookRepository bookRepository;
	private final IOrderRepository orderRepository;

	@Override
	@Transactional
	public void run(String... args) {
		if (isDatabasePopulated()) {
			log.info("DataSeeder: data already exists, skipping seed.");
			return;
		}

		ensureAdminUser();
		ensureSampleCustomerUser();

		log.info("DataSeeder: syncing catalog sample data...");
		SeedContext ctx = seedCatalog();
		seedBooks(ctx);
		seedSampleOrders();
		log.info("DataSeeder: done.");
	}

	/**
	 * Returns {@code true} if the database already contains seed data,
	 * using the presence of any book as the indicator.
	 */
	private boolean isDatabasePopulated() {
		return bookRepository.count() > 0;
	}

	private void ensureAdminUser() {
		final String adminEmail = "admin@example.com";
		final String adminPassword = "admin";

		try {
			if (userService.findByEmail(adminEmail) != null) {
				log.info("DataSeeder: admin user already exists: {}", adminEmail);
				return;
			}

			User admin = User.builder()
					.email(adminEmail)
					.firstName("Admin")
					.lastName("User")
					.phoneNumber("0900000000")
					.password(adminPassword)
					.role(UserRole.ADMIN)
					.addresses(new ArrayList<>(List.of(address(
							"Admin User",
							"0900000000",
							"Ha Noi",
							"Ba Dinh",
							"Phuc Xa",
							"1 Example Street",
							true))))
					.build();

			admin.setEmailVerified(true);
			userService.save(admin);
			log.info("DataSeeder: created admin user {}", adminEmail);
		} catch (Exception ex) {
			log.error("DataSeeder: failed to create admin user", ex);
		}
	}

	private void ensureSampleCustomerUser() {
		final String customerEmail = "reader@example.com";

		try {
			if (userService.findByEmail(customerEmail) != null) {
				log.info("DataSeeder: sample customer already exists: {}", customerEmail);
				return;
			}

			User reader = User.builder()
					.email(customerEmail)
					.firstName("Sample")
					.lastName("Reader")
					.phoneNumber("0912345678")
					.password("reader123")
					.role(UserRole.USER)
					.addresses(new ArrayList<>(List.of(address(
							"Sample Reader",
							"0912345678",
							"Ho Chi Minh City",
							"District 1",
							"Ben Nghe",
							"12 Nguyen Hue",
							true))))
					.build();

			reader.setEmailVerified(true);
			userService.save(reader);
			log.info("DataSeeder: created sample customer {}", customerEmail);
		} catch (Exception ex) {
			log.error("DataSeeder: failed to create sample customer", ex);
		}
	}

	private SeedContext seedCatalog() {
		Publisher nxbTre = upsertPublisher(Publisher.builder()
				.name("NXB Trẻ")
				.description("Một trong những nhà xuất bản lớn nhất dành cho văn học đương đại và sách thiếu nhi.")
				.location(PublisherLocation.builder()
						.address("161B Lý Chính Thắng")
						.city("Ho Chi Minh City")
						.country("Vietnam")
						.build())
				.contactEmail("contact@nxbtre.vn")
				.website("https://nxbtre.com.vn")
				.logo("/images/publishers/nxb-tre.png")
				.build());

		Publisher nxbKimDong = upsertPublisher(Publisher.builder()
				.name("NXB Kim Đồng")
				.description("Nhà xuất bản quen thuộc với độc giả nhỏ tuổi và các tác phẩm kinh điển.")
				.location(PublisherLocation.builder()
						.address("55 Quang Trung")
						.city("Ha Noi")
						.country("Vietnam")
						.build())
				.contactEmail("contact@kimdong.vn")
				.website("https://nxbkimdong.com.vn")
				.logo("/images/publishers/nxb-kim-dong.png")
				.build());

		Publisher nxbHoiNhaVan = upsertPublisher(Publisher.builder()
				.name("NXB Hội Nhà Văn")
				.description("Nhà xuất bản tập trung vào văn học, thơ ca và các ấn phẩm nghiên cứu.")
				.location(PublisherLocation.builder()
						.address("65 Nguyễn Du")
						.city("Ha Noi")
						.country("Vietnam")
						.build())
				.contactEmail("contact@hoinhavan.vn")
				.website("https://nhaxuatbanhoinhavan.vn")
				.logo("/images/publishers/nxb-hoi-nha-van.png")
				.build());

		Author nguyenNhatAnh = upsertAuthor(Author.builder()
				.name("Nguyễn Nhật Ánh")
				.email("nna@example.com")
				.bio("Tác giả nổi tiếng với những tác phẩm về tuổi thơ, học trò và tình cảm trong trẻo.")
				.birthDate(LocalDate.of(1955, 5, 7))
				.website("https://nguyennhatanh.vn")
				.build());

		Author toHoai = upsertAuthor(Author.builder()
				.name("Tô Hoài")
				.email("tohoai@example.com")
				.bio("Nhà văn gắn với văn học thiếu nhi và các tác phẩm giàu chất quan sát đời sống.")
				.birthDate(LocalDate.of(1920, 9, 27))
				.website("https://vi.wikipedia.org/wiki/T%C3%B4_Ho%C3%A0i")
				.build());

		Author nguyenDu = upsertAuthor(Author.builder()
				.name("Nguyễn Du")
				.email("nguyendu@example.com")
				.bio("Đại thi hào dân tộc, tác giả của Truyện Kiều.")
				.birthDate(LocalDate.of(1765, 1, 1))
				.website("https://vi.wikipedia.org/wiki/Nguy%E1%BB%85n_Du")
				.build());

		Author voQuang = upsertAuthor(Author.builder()
				.name("Võ Quảng")
				.email("voquang@example.com")
				.bio("Tác giả Việt Nam nổi tiếng với những trang viết trong trẻo về làng quê và thiếu nhi.")
				.birthDate(LocalDate.of(1920, 1, 1))
				.website("https://vi.wikipedia.org/wiki/V%C3%B5_Qu%E1%BA%A3ng")
				.build());

		Author nguyenHong = upsertAuthor(Author.builder()
				.name("Nguyễn Hồng")
				.email("nguyenhong@example.com")
				.bio("Nhà văn hiện thực với nhiều tác phẩm về đời sống và con người Việt Nam.")
				.birthDate(LocalDate.of(1918, 1, 1))
				.website("https://vi.wikipedia.org/wiki/Nguy%E1%BB%85n_H%E1%BB%93ng")
				.build());

		Category vanHoc = upsertCategory(Category.builder()
				.name("Văn học")
				.description("Các tác phẩm văn học Việt Nam và nước ngoài.")
				.orderIndex(1)
				.build());

		Category thieuNhi = upsertCategory(Category.builder()
				.name("Thiếu nhi")
				.description("Sách dành cho độc giả nhỏ tuổi.")
				.orderIndex(2)
				.build());

		Category coTich = upsertCategory(Category.builder()
				.name("Cổ tích - Dân gian")
				.description("Truyện cổ tích, truyền thuyết và truyện dân gian.")
				.parent(thieuNhi)
				.ancestors(List.of(thieuNhi))
				.orderIndex(3)
				.build());

		Category tinhCam = upsertCategory(Category.builder()
				.name("Tình cảm - Lãng mạn")
				.description("Các câu chuyện về tình cảm gia đình, tình yêu và tuổi trẻ.")
				.parent(vanHoc)
				.ancestors(List.of(vanHoc))
				.orderIndex(4)
				.build());

		log.info("DataSeeder: synced sample publishers, authors, and categories.");

		return new SeedContext(
				List.of(nxbTre, nxbKimDong, nxbHoiNhaVan),
				List.of(nguyenNhatAnh, toHoai, nguyenDu, voQuang, nguyenHong),
				List.of(vanHoc, thieuNhi, coTich, tinhCam));
	}

	private void seedBooks(SeedContext ctx) {
		Publisher nxbTre = ctx.publishers().get(0);
		Publisher nxbKimDong = ctx.publishers().get(1);
		Publisher nxbHoiNhaVan = ctx.publishers().get(2);

		Author nguyenNhatAnh = ctx.authors().get(0);
		Author toHoai = ctx.authors().get(1);
		Author nguyenDu = ctx.authors().get(2);
		Author voQuang = ctx.authors().get(3);
		Author nguyenHong = ctx.authors().get(4);

		Category vanHoc = ctx.categories().get(0);
		Category thieuNhi = ctx.categories().get(1);
		Category coTich = ctx.categories().get(2);
		Category tinhCam = ctx.categories().get(3);

		List<BookRequest> books = List.of(
				book(
						"Cho tôi xin một vé đi tuổi thơ",
						"Tác phẩm nổi tiếng về ký ức tuổi thơ trong sáng và hồn nhiên.",
						"9781234567897",
						LocalDate.of(2008, 1, 1),
						222,
						nxbTre,
						List.of(nguyenNhatAnh),
						List.of(thieuNhi, tinhCam),
						"/images/books/cho-toi-xin-mot-ve-di-tuoi-tho.jpg",
						List.of(
								physicalFormat("9781234567897-P", "9781234567897-01",
										money(120000), money(99000),
										LocalDate.of(2008, 1, 1), 140,
										new BigDecimal("0.320"), "13 x 20 cm"),
								digitalFormat("9781234567897-E", "9781234567897-02",
										money(78000), money(59000),
										LocalDate.of(2008, 1, 1),
										"/files/ebooks/cho-toi-xin-mot-ve-di-tuoi-tho.pdf",
										DigitalFileFormat.PDF, 6_291_456L, 5,
										"/files/samples/cho-toi-xin-mot-ve-di-tuoi-tho-sample.pdf"),
								audiobookFormat("9781234567897-A", "9781234567897-03",
										money(89000), null,
										LocalDate.of(2008, 1, 1),
										"/files/audio/cho-toi-xin-mot-ve-di-tuoi-tho.mp3",
										48_234_112L, 3,
										"/files/samples/cho-toi-xin-mot-ve-di-tuoi-tho-preview.mp3"))),

				book(
						"Tôi thấy hoa vàng trên cỏ xanh",
						"Câu chuyện cảm động về tình anh em và tuổi thơ miền quê.",
						"9789876543210",
						LocalDate.of(2010, 6, 1),
						220,
						nxbTre,
						List.of(nguyenNhatAnh),
						List.of(vanHoc, thieuNhi),
						"/images/books/toi-thay-hoa-vang-tren-co-xanh.jpg",
						List.of(
								physicalFormat("9789876543210-P", "9789876543210-01",
										money(145000), money(119000),
										LocalDate.of(2010, 6, 1), 90,
										new BigDecimal("0.450"), "16 x 24 cm"),
								digitalFormat("9789876543210-E", "9789876543210-02",
										money(86000), null,
										LocalDate.of(2010, 6, 1),
										"/files/ebooks/toi-thay-hoa-vang-tren-co-xanh.epub",
										DigitalFileFormat.EPUB, 5_742_080L, 5,
										"/files/samples/toi-thay-hoa-vang-tren-co-xanh-sample.epub"),
								audiobookFormat("9789876543210-A", "9789876543210-03",
										money(95000), money(79000),
										LocalDate.of(2010, 6, 1),
										"/files/audio/toi-thay-hoa-vang-tren-co-xanh.m4a",
										52_109_312L, 4,
										"/files/samples/toi-thay-hoa-vang-tren-co-xanh-preview.mp3"))),

				book(
						"Mắt biếc",
						"Chuyện tình buồn đẹp của Ngạn và Hà Lan qua bao năm tháng.",
						"9781111111111",
						LocalDate.of(1990, 3, 15),
						268,
						nxbTre,
						List.of(nguyenNhatAnh),
						List.of(vanHoc, tinhCam),
						"/images/books/mat-biec.jpg",
						List.of(
								physicalFormat("9781111111111-P", "9781111111111-01",
										money(130000), null,
										LocalDate.of(1990, 3, 15), 110,
										new BigDecimal("0.360"), "14 x 21 cm"),
								digitalFormat("9781111111111-E", "9781111111111-02",
										money(82000), money(69000),
										LocalDate.of(1990, 3, 15),
										"/files/ebooks/mat-biec.mobi",
										DigitalFileFormat.MOBI, 6_102_016L, 5,
										"/files/samples/mat-biec-sample.mobi"),
								audiobookFormat("9781111111111-A", "9781111111111-03",
										money(90000), null,
										LocalDate.of(1990, 3, 15),
										"/files/audio/mat-biec.mp3",
										49_884_160L, 3,
										"/files/samples/mat-biec-preview.mp3"))),

				book(
						"Dế Mèn phiêu lưu ký",
						"Hành trình phiêu lưu của Dế Mèn - kinh điển văn học thiếu nhi Việt Nam.",
						"9782222222222",
						LocalDate.of(1941, 1, 1),
						176,
						nxbKimDong,
						List.of(toHoai),
						List.of(thieuNhi, coTich),
						"/images/books/de-men-phieu-luu-ky.jpg",
						List.of(
								physicalFormat("9782222222222-P", "9782222222222-01",
										money(98000), money(79000),
										LocalDate.of(1941, 1, 1), 180,
										new BigDecimal("0.250"), "13 x 19 cm"),
								digitalFormat("9782222222222-E", "9782222222222-02",
										money(68000), null,
										LocalDate.of(1941, 1, 1),
										"/files/ebooks/de-men-phieu-luu-ky.pdf",
										DigitalFileFormat.PDF, 4_931_584L, 5,
										"/files/samples/de-men-phieu-luu-ky-sample.pdf"),
								audiobookFormat("9782222222222-A", "9782222222222-03",
										money(85000), money(71000),
										LocalDate.of(1941, 1, 1),
										"/files/audio/de-men-phieu-luu-ky.mp3",
										41_115_648L, 4,
										"/files/samples/de-men-phieu-luu-ky-preview.mp3"))),

				book(
						"Truyện Kiều",
						"Kiệt tác thơ Nôm của đại thi hào Nguyễn Du, kể về số phận Thuý Kiều.",
						"9783333333333",
						LocalDate.of(1820, 1, 1),
						320,
						nxbHoiNhaVan,
						List.of(nguyenDu),
						List.of(vanHoc, coTich),
						"/images/books/truyen-kieu.jpg",
						List.of(
								physicalFormat("9783333333333-P", "9783333333333-01",
										money(160000), money(135000),
										LocalDate.of(1820, 1, 1), 60,
										new BigDecimal("0.520"), "16 x 24 cm"),
								digitalFormat("9783333333333-E", "9783333333333-02",
										money(98000), money(88000),
										LocalDate.of(1820, 1, 1),
										"/files/ebooks/truyen-kieu.epub",
										DigitalFileFormat.EPUB, 7_004_160L, 5,
										"/files/samples/truyen-kieu-sample.epub"),
								audiobookFormat("9783333333333-A", "9783333333333-03",
										money(110000), null,
										LocalDate.of(1820, 1, 1),
										"/files/audio/truyen-kieu.m4a",
										61_776_384L, 3,
										"/files/samples/truyen-kieu-preview.mp3"))),

				book(
						"Quê nội",
						"Ký ức làng quê miền Trung qua ngòi bút của Võ Quảng.",
						"9784444444444",
						LocalDate.of(1974, 5, 1),
						196,
						nxbKimDong,
						List.of(voQuang),
						List.of(vanHoc, thieuNhi),
						"/images/books/que-noi.jpg",
						List.of(
								physicalFormat("9784444444444-P", "9784444444444-01",
										money(99000), null,
										LocalDate.of(1974, 5, 1), 120,
										new BigDecimal("0.290"), "13 x 20 cm"),
								digitalFormat("9784444444444-E", "9784444444444-02",
										money(75000), money(59000),
										LocalDate.of(1974, 5, 1),
										"/files/ebooks/que-noi.mobi",
										DigitalFileFormat.MOBI, 5_365_760L, 5,
										"/files/samples/que-noi-sample.mobi"),
								audiobookFormat("9784444444444-A", "9784444444444-03",
										money(88000), money(72000),
										LocalDate.of(1974, 5, 1),
										"/files/audio/que-noi.mp3", 44_302_336L,
										4,
										"/files/samples/que-noi-preview.mp3"))),

				book(
						"Mùa gặt trên cánh đồng cũ",
						"Một câu chuyện làng quê khác để làm phong phú bộ sưu tập mẫu.",
						"9785555555555",
						LocalDate.of(1988, 8, 1),
						248,
						nxbHoiNhaVan,
						List.of(nguyenHong),
						List.of(vanHoc, tinhCam),
						"/images/books/mua-gat-tren-canh-dong-cu.jpg",
						List.of(
								physicalFormat("9785555555555-P", "9785555555555-01",
										money(115000), money(95000),
										LocalDate.of(1988, 8, 1), 75,
										new BigDecimal("0.340"), "14 x 21 cm"),
								digitalFormat("9785555555555-E", "9785555555555-02",
										money(79000), null,
										LocalDate.of(1988, 8, 1),
										"/files/ebooks/mua-gat-tren-canh-dong-cu.pdf",
										DigitalFileFormat.PDF, 5_918_720L, 5,
										"/files/samples/mua-gat-tren-canh-dong-cu-sample.pdf"),
								audiobookFormat("9785555555555-A", "9785555555555-03",
										money(93000), null,
										LocalDate.of(1988, 8, 1),
										"/files/audio/mua-gat-tren-canh-dong-cu.mp3",
										46_514_176L, 3,
										"/files/samples/mua-gat-tren-canh-dong-cu-preview.mp3"))));

		books.forEach(this::upsertBook);
		log.info("DataSeeder: synced {} books with formats.", books.size());
	}

	private void seedSampleOrders() {
		User customer = userService.findByEmail("reader@example.com");
		if (customer == null || customer.getAddresses() == null || customer.getAddresses().isEmpty()) {
			log.warn("DataSeeder: skipping sample orders because customer seed is missing.");
			return;
		}

		if (orderRepository.existsByOrderNumber("ORD-202604050001")) {
			log.info("DataSeeder: sample orders already exist, skipping.");
			return;
		}

		Address address = customer.getAddresses().get(0);

		Book oneBook = requireBook("9781234567897");
		Book twoBook = requireBook("9789876543210");
		Book threeBook = requireBook("9782222222222");
		Book fourBook = requireBook("9783333333333");

		Order completedOrder = buildOrder(
				customer,
				address,
				"ORD-202604050001",
				OrderStatus.COMPLETED,
				List.of(
						orderItem(oneBook, BookFormatType.PHYSICAL, 1),
						orderItem(twoBook, BookFormatType.DIGITAL, 1)),
				PaymentMethod.CREDIT_CARD,
				PaymentStatus.SUCCESS,
				"SIM-0001",
				"Completed seed order",
				LocalDateTime.now().minusDays(5));

		Order processingOrder = buildOrder(
				customer,
				address,
				"ORD-202604050002",
				OrderStatus.PROCESSING,
				List.of(
						orderItem(threeBook, BookFormatType.PHYSICAL, 2)),
				PaymentMethod.BANK_TRANSFER,
				PaymentStatus.INITIATED,
				"SIM-0002",
				"Processing seed order",
				LocalDateTime.now().minusDays(2));

		Order pendingPaymentOrder = buildOrder(
				customer,
				address,
				"ORD-202604050003",
				OrderStatus.PENDING_PAYMENT,
				List.of(
						orderItem(fourBook, BookFormatType.DIGITAL, 1)),
				null,
				null,
				null,
				null,
				LocalDateTime.now().minusDays(1));

		Order cancelledOrder = buildOrder(
				customer,
				address,
				"ORD-202604050004",
				OrderStatus.CANCELLED,
				List.of(
						orderItem(oneBook, BookFormatType.AUDIOBOOK, 1),
						orderItem(fourBook, BookFormatType.PHYSICAL, 1)),
				PaymentMethod.E_WALLET,
				PaymentStatus.FAILED,
				"SIM-0004",
				"Cancelled seed order",
				LocalDateTime.now().minusDays(7));

		orderRepository.save(completedOrder);
		orderRepository.save(processingOrder);
		orderRepository.save(pendingPaymentOrder);
		orderRepository.save(cancelledOrder);

		log.info("DataSeeder: created 4 sample orders for admin views.");
	}

	private Order buildOrder(
			User user,
			Address address,
			String orderNumber,
			OrderStatus status,
			List<OrderItem> items,
			PaymentMethod paymentMethod,
			PaymentStatus paymentStatus,
			String providerReference,
			String paymentMessage,
			LocalDateTime placedAt) {

		BigDecimal subtotal = items.stream()
				.map(OrderItem::getSubTotal)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal tax = items.stream()
				.map(OrderItem::getTaxAmount)
				.reduce(BigDecimal.ZERO, BigDecimal::add);
		BigDecimal discount = BigDecimal.ZERO;
		BigDecimal grandTotal = subtotal.add(tax).subtract(discount).max(BigDecimal.ZERO);

		Order order = Order.builder()
				.orderNumber(orderNumber)
				.user(user)
				.customerEmail(user.getEmail())
				.customerPhone(address.getPhoneNumber())
				.recipientName(address.getRecipientName())
				.shippingStreetDetails(address.getStreetDetails())
				.shippingWard(address.getWard())
				.shippingDistrict(address.getDistrict())
				.shippingProvinceOrCity(address.getProvinceOrCity())
				.items(items)
				.subtotal(subtotal)
				.tax(tax)
				.discount(discount)
				.grandTotal(grandTotal)
				.currency(resolveCurrency(items))
				.orderStatus(status)
				.placedAt(placedAt)
				.build();

		if (paymentStatus != null && paymentMethod != null) {
			Payment payment = Payment.builder()
					.paymentMethod(paymentMethod)
					.paymentStatus(paymentStatus)
					.amount(grandTotal)
					.currency(order.getCurrency())
					.providerReference(providerReference)
					.message(paymentMessage)
					.paidAt(paymentStatus == PaymentStatus.SUCCESS ? placedAt : null)
					.build();
			order.attachPayment(payment);
		}

		return order;
	}

	private OrderItem orderItem(Book book, BookFormatType formatType, int quantity) {
		BookFormat format = book.getFormats().stream()
				.filter(candidate -> candidate != null && candidate.getFormatType() == formatType)
				.findFirst()
				.orElseThrow(() -> new IllegalStateException(
						"Không tìm thấy format " + formatType + " cho sách "
								+ book.getTitle()));

		BigDecimal unitPrice = resolvePrice(format);
		BigDecimal vatRate = BookVatPolicy.resolveVatRate(book);

		return OrderItem.builder()
				.bookId(book.getId())
				.sku(format.getSku())
				.title(book.getTitle())
				.coverImage(book.getCoverImage())
				.formatType(format.getFormatType())
				.unitPrice(unitPrice)
				.vatRate(vatRate)
				.quantity(quantity)
				.currency(format.getCurrency())
				.build();
	}

	private Book requireBook(String isbn) {
		Book book = findBookByIsbn(isbn);
		if (book == null) {
			throw new IllegalStateException("Không tìm thấy sách mẫu với ISBN: " + isbn);
		}
		return book;
	}

	private String resolveCurrency(List<OrderItem> orderItems) {
		return orderItems.stream()
				.map(OrderItem::getCurrency)
				.filter(value -> value != null && !value.isBlank())
				.findFirst()
				.orElse("VND");
	}

	private BigDecimal resolvePrice(BookFormat format) {
		BigDecimal discounted = format.getDiscountedPrice();
		return (discounted != null && discounted.compareTo(BigDecimal.ZERO) > 0)
				? discounted
				: format.getPrice();
	}

	private Publisher upsertPublisher(Publisher seed) {
		Publisher existing = findPublisherByName(seed.getName());
		return existing == null ? publisherService.create(seed)
				: publisherService.update(existing.getId(), seed);
	}

	private Author upsertAuthor(Author seed) {
		Author existing = findAuthorByName(seed.getName());
		return existing == null ? authorService.create(seed) : authorService.update(existing.getId(), seed);
	}

	private Category upsertCategory(Category seed) {
		Category existing = findCategoryByName(seed.getName());
		return existing == null ? categoryService.create(seed) : categoryService.update(existing.getId(), seed);
	}

	private Book upsertBook(BookRequest request) {
		Book existing = findBookByIsbn(request.isbn());
		return existing == null
				? bookService.createBook(request, request.coverImage())
				: bookService.updateBook(existing.getSlug(), request, request.coverImage());
	}

	private Publisher findPublisherByName(String name) {
		return publisherRepository.findByName(name).orElse(null);
	}

	private Author findAuthorByName(String name) {
		return authorRepository.findByName(name).orElse(null);
	}

	private Category findCategoryByName(String name) {
		return categoryRepository.findByName(name).orElse(null);
	}

	private Book findBookByIsbn(String isbn) {
		return bookRepository.findByIsbn(isbn).orElse(null);
	}

	private static Address address(
			String recipientName,
			String phoneNumber,
			String provinceOrCity,
			String district,
			String ward,
			String streetDetails,
			boolean isDefault) {

		return Address.builder()
				.recipientName(recipientName)
				.phoneNumber(phoneNumber)
				.provinceOrCity(provinceOrCity)
				.district(district)
				.ward(ward)
				.streetDetails(streetDetails)
				.country("Vietnam")
				.isDefault(isDefault)
				.build();
	}

	private static BookRequest book(
			String title,
			String description,
			String isbn,
			LocalDate publishedDate,
			int pages,
			Publisher publisher,
			List<Author> authors,
			List<Category> categories,
			String coverImage,
			List<BookFormat> formats) {

		return new BookRequest(
				title,
				"",
				description,
				isbn,
				publishedDate,
				"vi",
				pages,
				publisher.getId(),
				authors.stream().map(Author::getId).toList(),
				categories.stream().map(Category::getId).toList(),
				coverImage,
				formats);
	}

	private static BookFormat physicalFormat(
			String sku,
			String isbn,
			BigDecimal price,
			BigDecimal discountedPrice,
			LocalDate releaseDate,
			int stockQuantity,
			BigDecimal weight,
			String dimensions) {

		return BookFormat.builder()
				.formatType(BookFormatType.PHYSICAL)
				.sku(sku)
				.isbn(isbn)
				.price(price)
				.discountedPrice(discountedPrice)
				.currency("VND")
				.releaseDate(releaseDate)
				.stockQuantity(stockQuantity)
				.weight(weight)
				.dimensions(dimensions)
				.build();
	}

	private static BookFormat digitalFormat(
			String sku,
			String isbn,
			BigDecimal price,
			BigDecimal discountedPrice,
			LocalDate releaseDate,
			String filePath,
			DigitalFileFormat fileFormat,
			Long fileSize,
			Integer downloadLimit,
			String sampleFile) {

		return BookFormat.builder()
				.formatType(BookFormatType.DIGITAL)
				.sku(sku)
				.isbn(isbn)
				.price(price)
				.discountedPrice(discountedPrice)
				.currency("VND")
				.releaseDate(releaseDate)
				.file(filePath)
				.fileFormat(fileFormat)
				.fileSize(fileSize)
				.downloadLimit(downloadLimit)
				.sampleFile(sampleFile)
				.build();
	}

	private static BookFormat audiobookFormat(
			String sku,
			String isbn,
			BigDecimal price,
			BigDecimal discountedPrice,
			LocalDate releaseDate,
			String filePath,
			Long fileSize,
			Integer downloadLimit,
			String sampleFile) {

		return BookFormat.builder()
				.formatType(BookFormatType.AUDIOBOOK)
				.sku(sku)
				.isbn(isbn)
				.price(price)
				.discountedPrice(discountedPrice)
				.currency("VND")
				.releaseDate(releaseDate)
				.file(filePath)
				.fileSize(fileSize)
				.downloadLimit(downloadLimit)
				.sampleFile(sampleFile)
				.build();
	}

	private static BigDecimal money(long amount) {
		return BigDecimal.valueOf(amount);
	}

	/**
	 * Carries seeded entities from one phase to the next.
	 */
	private record SeedContext(
			List<Publisher> publishers,
			List<Author> authors,
			List<Category> categories) {
	}
}